package itmo.lab6.server;

import itmo.lab6.protocol.*;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;

/** Receives datagrams and reconstructs typed requests from bounded assemblies. */
public final class RequestReader {
    private final ByteBuffer buffer = ByteBuffer.allocate(Packets.DATAGRAM + 1);
    private final Map<Key, Packets.Assembly> pending = new HashMap<>();
    private final DatagramChannel channel;
    public RequestReader(DatagramChannel channel) { this.channel = channel; }

    public Received read() throws IOException {
        buffer.clear();
        SocketAddress peer = channel.receive(buffer);
        if (peer == null) return null;
        long now = System.nanoTime();
        pending.entrySet().removeIf(e -> now - e.getValue().created > 15_000_000_000L);
        Packets.Frame frame = Packets.parse(buffer.array(), buffer.position());
        Key key = new Key(peer, frame.id());
        if (!pending.containsKey(key) && pending.size() >= 32) throw new IOException("Too many incomplete requests");
        Packets.Assembly assembly = pending.computeIfAbsent(key, k -> new Packets.Assembly(frame.count()));
        byte[] bytes = assembly.add(frame);
        if (bytes == null) return new Received(key, null);
        pending.remove(key);
        Request request = Serialization.decode(bytes, Request.class);
        if (!frame.id().equals(request.id())) throw new IOException("Request id does not match frame");
        return new Received(key, request);
    }

    /** Peer and request id together identify a logical operation. */
    public record Key(SocketAddress peer, UUID id) { }
    /** A null request means only a fragment has arrived. */
    public record Received(Key key, Request request) { }
}
