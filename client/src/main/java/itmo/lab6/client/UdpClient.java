package itmo.lab6.client;

import itmo.lab6.protocol.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** Blocking datagram client with timeouts, retransmission and fragment reassembly. */
public final class UdpClient implements AutoCloseable {
    private final DatagramSocket socket;
    private final int timeout;
    private final int attempts;

    public UdpClient(String host, int port) throws IOException {
        timeout = Integer.getInteger("lab6.timeoutMillis", 3000);
        attempts = Integer.getInteger("lab6.attempts", 3);
        if (timeout < 100 || timeout > 60000 || attempts < 1 || attempts > 10)
            throw new IllegalArgumentException("Invalid timeout/attempts settings");
        InetAddress address = InetAddress.getByName(host);
        socket = new DatagramSocket();
        socket.setReceiveBufferSize(1024 * 1024);
        socket.connect(address, port);
    }

    public Response send(Request request) throws IOException {
        List<byte[]> frames = Packets.split(request.id(), Serialization.encode(request));
        Packets.Assembly assembly = null;
        byte[] buffer = new byte[Packets.DATAGRAM + 1];
        IOException last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            long deadline = System.nanoTime() + timeout * 1_000_000L;
            try {
                for (byte[] frame : frames) socket.send(new DatagramPacket(frame, frame.length));
                while (System.nanoTime() < deadline) {
                    socket.setSoTimeout((int) Math.max(1, (deadline - System.nanoTime()) / 1_000_000));
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    Packets.Frame frame;
                    try { frame = Packets.parse(buffer, packet.getLength()); }
                    catch (IOException e) { continue; }
                    if (!frame.id().equals(request.id())) continue;
                    if (assembly == null) assembly = new Packets.Assembly(frame.count());
                    byte[] bytes = assembly.add(frame);
                    if (bytes == null) continue;
                    Response response = Serialization.decode(bytes, Response.class);
                    if (!request.id().equals(response.id())) throw new IOException("Mismatched response id");
                    return response;
                }
            } catch (IOException e) { last = e; }
            System.err.println("Server unavailable or response incomplete (attempt " + attempt + "/" + attempts + ").");
            // ICMP errors can arrive immediately. Pace retries to allow a server restart.
            long remaining = (deadline - System.nanoTime()) / 1_000_000;
            if (attempt < attempts && remaining > 0) {
                try { Thread.sleep(remaining); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Interrupted", e); }
            }
        }
        throw new IOException("No response from server; command outcome is unknown. Use retry or exit.", last);
    }
    public void close() { socket.close(); }
}
