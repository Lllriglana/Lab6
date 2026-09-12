package itmo.lab6.server;

import itmo.lab6.protocol.Packets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Bounded, paced outgoing queue; sending never blocks the command loop. */
public final class ResponseSender {
    private static final Logger LOG = LogManager.getLogger(ResponseSender.class);
    private final DatagramChannel channel;
    private final LinkedHashMap<RequestReader.Key, Deque<byte[]>> queue = new LinkedHashMap<>();
    public ResponseSender(DatagramChannel channel) { this.channel = channel; }
    public void enqueue(RequestReader.Key key, byte[] bytes) {
        if (!queue.containsKey(key) && queue.size() < 16)
            queue.put(key, new ArrayDeque<>(Packets.split(key.id(), bytes)));
    }
    public boolean pending() { return !queue.isEmpty(); }
    public void flush() throws IOException {
        int budget = 16;
        var iterator = queue.entrySet().iterator();
        while (iterator.hasNext() && budget > 0) {
            var entry = iterator.next();
            var packets = entry.getValue();
            while (!packets.isEmpty() && budget-- > 0) {
                if (channel.send(ByteBuffer.wrap(packets.peek()), entry.getKey().peer()) == 0) return;
                packets.remove();
            }
            if (packets.isEmpty()) {
                LOG.info("Response sent: {} to {}", entry.getKey().id(), entry.getKey().peer());
                iterator.remove();
            }
        }
    }
}
