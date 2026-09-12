package itmo.lab6.server;

import itmo.lab6.protocol.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Single-threaded UDP server with local administration and shutdown persistence. */
public final class ServerMain {
    private static final Logger LOG = LogManager.getLogger(ServerMain.class);
    private ServerMain() { }
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Usage: java -jar Lab6-server.jar <collection.json> [port=22226] [bind=0.0.0.0]");
            System.exit(1);
        }
        try {
            int port = args.length >= 2 ? Integer.parseInt(args[1]) : 22226;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port must be 1..65535");
            FileManager files = new FileManager(Path.of(args[0]), System.out);
            CollectionManager collection = new CollectionManager(files.load());
            Object stateLock = new Object();
            try (ConnectionModule connection = new ConnectionModule(args.length == 3 ? args[2] : "0.0.0.0", port)) {
                // JVM lifecycle hook only; all normal I/O and command processing use the main thread.
                Thread shutdown = new Thread(() -> {
                    synchronized (stateLock) { save(files, collection); }
                }, "collection-shutdown");
                Runtime.getRuntime().addShutdownHook(shutdown);
                try {
                    run(connection, files, collection, stateLock);
                } finally {
                    synchronized (stateLock) { save(files, collection); }
                    Runtime.getRuntime().removeShutdownHook(shutdown);
                }
            }
        } catch (Exception e) {
            LOG.error("Server stopped: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static void run(ConnectionModule connection, FileManager files, CollectionManager collection,
                            Object stateLock) throws IOException {
        RequestReader reader = new RequestReader(connection.channel());
        ResponseSender sender = new ResponseSender(connection.channel());
        CommandProcessor processor = new CommandProcessor(collection);
        LinkedHashMap<RequestReader.Key, Cached> cache = new LinkedHashMap<>();
        Set<java.net.SocketAddress> peers = new HashSet<>();
        StringBuilder console = new StringBuilder();
        LOG.info("Server started on {}; {}", connection.channel().getLocalAddress(), files.getFilePath());
        System.out.println("SERVER_READY " + connection.channel().getLocalAddress());
        System.out.println("Server console: save, info, exit (saves). Client exit leaves the server running.");
        while (true) {
            // Read only bytes already available: a partial console line cannot block UDP.
            int consoleBudget = 4096;
            while (consoleBudget-- > 0 && System.in.available() > 0) {
                int value = System.in.read();
                if (value == '\n') {
                    String command = console.toString().trim();
                    console.setLength(0);
                    if (command.equals("exit")) return;
                    if (command.equals("save")) save(files, collection);
                    else if (command.equals("info")) System.out.println(collection.info());
                    else if (!command.isEmpty()) System.out.println("Server commands: save, info, exit");
                } else if (value != '\r' && console.length() < 1024) console.append((char) value);
            }
            long now = System.nanoTime();
            cache.entrySet().removeIf(e -> now - e.getValue().created() > 300_000_000_000L);
            for (int i = 0; i < 64; i++) {
                try {
                    RequestReader.Received received = reader.read();
                    if (received == null) break;
                    if (received.request() == null) continue;
                    if (peers.size() > 1024) peers.clear();
                    if (peers.add(received.key().peer())) LOG.info("New UDP client: {}", received.key().peer());
                    Cached cached = cache.get(received.key());
                    if (cached == null) {
                        LOG.info("Request {}: {} from {}", received.request().id(), received.request().command(), received.key().peer());
                        Response response;
                        synchronized (stateLock) { response = processor.execute(received.request()); }
                        byte[] bytes;
                        try { bytes = Serialization.encode(response); }
                        catch (IOException e) { bytes = Serialization.encode(Response.message(received.key().id(), false, e.getMessage())); }
                        cached = new Cached(System.nanoTime(), bytes);
                        cache.put(received.key(), cached);
                        while (cache.size() > 512 || cache.values().stream().mapToLong(c -> c.bytes().length).sum() > 32L * 1024 * 1024)
                            cache.remove(cache.keySet().iterator().next());
                    } else LOG.info("Duplicate request {}; returning cached response", received.key().id());
                    sender.enqueue(received.key(), cached.bytes());
                } catch (IOException | RuntimeException e) {
                    LOG.warn("Rejected datagram: {}", e.getMessage());
                }
            }
            sender.flush();
            connection.await(sender.pending());
        }
    }

    private static void save(FileManager files, CollectionManager collection) {
        try {
            files.save(collection.all());
            LOG.info("Collection saved to {}", files.getFilePath());
            System.out.println("Collection saved.");
        } catch (Exception e) { LOG.error("Cannot save collection: {}", e.getMessage()); }
    }

    /** Recent serialized result for duplicate suppression within this server process. */
    private record Cached(long created, byte[] bytes) { }
}
