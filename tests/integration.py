"""Real subprocess/UDP integration checks; all mutable data lives in a temp directory."""
import json
import os
from pathlib import Path
import select
import shutil
import socket
import subprocess
import tempfile
import threading
import time

ROOT = Path(__file__).resolve().parents[1]
CLIENT = ROOT / "dist/Lab6-client.jar"
SERVER = ROOT / "dist/Lab6-server.jar"


def free_port():
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def client(port, commands, cwd=ROOT, timeout=30):
    result = subprocess.run(["java", "-Dlab6.timeoutMillis=500", "-Dlab6.attempts=3", "-jar",
                             str(CLIENT), "127.0.0.1", str(port)], input=commands,
                            text=True, capture_output=True, cwd=cwd, timeout=timeout)
    assert result.returncode == 0, result.stderr
    return result.stdout + result.stderr


class Server:
    def __init__(self, directory, data, port):
        self.log_path = directory / f"server-{port}-{time.time_ns()}.txt"
        self.log = self.log_path.open("w")
        self.process = subprocess.Popen(["java", "-jar", str(SERVER), str(data), str(port), "127.0.0.1"],
                                        cwd=directory, stdin=subprocess.PIPE, stdout=self.log,
                                        stderr=subprocess.STDOUT, text=True)
        deadline = time.monotonic() + 10
        while time.monotonic() < deadline:
            if "SERVER_READY" in self.log_path.read_text():
                return
            if self.process.poll() is not None:
                raise AssertionError(self.log_path.read_text())
            time.sleep(0.05)
        self.process.kill()
        raise AssertionError("Server startup timeout")

    def command(self, command):
        self.process.stdin.write(command + "\n")
        self.process.stdin.flush()

    def stop(self, signal=False):
        if self.process.poll() is None:
            if signal:
                self.process.terminate()
            else:
                self.command("exit")
            try:
                self.process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                self.process.kill()
                raise
        self.log.close()


def main():
    with tempfile.TemporaryDirectory(prefix="lab6-tests-") as tmp:
        directory = Path(tmp)
        data = directory / "collection.json"
        shutil.copyfile(ROOT / "lab6_test.json", data)
        port = free_port()
        server = Server(directory, data, port)
        try:
            subprocess.run(["javac", "--release", "17", "-cp", str(CLIENT), "-d", tmp,
                            str(ROOT / "tests/ProtocolChecks.java")], check=True)
            subprocess.run(["java", "-cp", str(CLIENT) + os.pathsep + tmp, "ProtocolChecks", str(port)],
                           check=True, timeout=40)
            print("PASS typed commands, validation, server ids, fragmentation, deduplication, sorting")
            result = client(port, "save\nupdate nope\nfilter_by_weapon_type INVALID\nhelp junk\nexit\n")
            assert "only in the server console" in result and result.count("Input error:") == 3, result
            assert len(json.loads(data.read_text())) == 120
            # A partial console command must not block the server's event loop.
            server.process.stdin.write("in")
            server.process.stdin.flush()
            assert "Elements count: 120" in client(port, "info\nexit\n")
            server.command("fo")
            result = client(port, "execute_script scripts/full_demo.txt\ninfo\nexit\n")
            assert "Unknown command" not in result and "Server unavailable" not in result, result
            assert "Recursive script execution" in result and "Elements count: 2" in result, result
            assert "Input error:" in result, "demo must exercise validation"
            assert len(json.loads(data.read_text())) == 120, "client exit must not save"
            server.command("save")
            deadline = time.monotonic() + 5
            while time.monotonic() < deadline and len(json.loads(data.read_text())) != 2:
                time.sleep(0.05)
            assert len(json.loads(data.read_text())) == 2, "server save"
            assert "Elements count: 2" in client(port, "info\nexit\n"), "client exit must leave server alive"
            print("PASS client validation, script demo, recursion, partial console input, server-only save")
            client(port, "clear\nexit\n")
        finally:
            server.stop()
        assert json.loads(data.read_text()) == [], "save on normal server exit"
        server = Server(directory, data, port)
        try:
            result = client(port, "add\nAfter restart\n1\n2\n3\n\n\n\nChapter\n1\nexit\n")
            assert "Element added" in result, result
        finally:
            server.stop(signal=True)
        assert len(json.loads(data.read_text())) == 1, "save on SIGTERM"
        print("PASS restart, exit persistence, SIGTERM persistence")

        offline = free_port()
        result = client(offline, "info\nexit\n")
        assert "outcome is unknown" in result and "Client terminated" in result, result
        # Server appears while an already running client is retrying.
        process = subprocess.Popen(["java", "-Dlab6.timeoutMillis=700", "-Dlab6.attempts=5", "-jar",
                                    str(CLIENT), "127.0.0.1", str(offline)], cwd=ROOT,
                                   stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        process.stdin.write("info\nexit\n")
        process.stdin.flush()
        time.sleep(0.8)
        server = Server(directory, data, offline)
        try:
            result, _ = process.communicate(timeout=10)
            assert "Elements count: 1" in result, result
        finally:
            server.stop()
            if process.poll() is None:
                process.kill()
        print("PASS temporary server unavailability and recovery during retry")

        # UDP proxy deliberately loses the first add response. Server must not add twice.
        server_port, proxy_port = free_port(), free_port()
        server = Server(directory, data, server_port)
        proxy = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        proxy.bind(("127.0.0.1", proxy_port))
        proxy.settimeout(0.1)
        stopped = threading.Event()
        dropped = []

        def relay():
            peer = None
            while not stopped.is_set():
                try:
                    packet, sender = proxy.recvfrom(65535)
                    if sender[1] == server_port:
                        if not dropped:
                            dropped.append(True)
                            continue
                        if peer:
                            proxy.sendto(packet, peer)
                    else:
                        peer = sender
                        proxy.sendto(packet, ("127.0.0.1", server_port))
                except socket.timeout:
                    continue

        thread = threading.Thread(target=relay)
        thread.start()
        try:
            result = client(proxy_port, "add\nLoss test\n1\n2\n4\n\n\n\nChapter\n1\ninfo\nexit\n")
            assert dropped and "Elements count: 2" in result, result
            assert "Duplicate request" in server.log_path.read_text(), server.log_path.read_text()
        finally:
            stopped.set()
            thread.join(timeout=2)
            proxy.close()
            server.stop()
        print("PASS lost response retry without duplicate mutation")

        large = json.loads((ROOT / "lab6_test.json").read_text())
        large = [dict(large[i % 120], id=i + 1, name=f"BulkMarine-{i + 1}") for i in range(1500)]
        data.write_text(json.dumps(large))
        port = free_port()
        server = Server(directory, data, port)
        try:
            result = client(port, "show\nexit\n", timeout=30)
            assert result.count("SpaceMarine{id=") == 1500, result[-3000:]
            assert len(result.encode()) > 65507, "response must exceed a single UDP datagram"
        finally:
            server.stop()
        print("PASS 1500-object response larger than UDP datagram limit")

        invalid = directory / "invalid.json"
        invalid.write_text("{ broken json")
        result = subprocess.run(["java", "-jar", str(SERVER), str(invalid), str(free_port())],
                                cwd=directory, text=True, capture_output=True, timeout=10)
        assert result.returncode != 0 and invalid.read_text() == "{ broken json"
        print("PASS invalid JSON does not get overwritten")
    print("ALL INTEGRATION CHECKS PASSED")


if __name__ == "__main__":
    main()
