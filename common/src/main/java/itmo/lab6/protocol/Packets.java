package itmo.lab6.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/** Small UDP frames allow serialized collections to exceed a single datagram. */
public final class Packets {
    public static final int PAYLOAD = 1100;
    public static final int HEADER = 28;
    public static final int DATAGRAM = PAYLOAD + HEADER;
    private static final int MAGIC = 0x4c414236;
    private Packets() { }

    public static List<byte[]> split(UUID id, byte[] bytes) {
        int count = (bytes.length + PAYLOAD - 1) / PAYLOAD;
        List<byte[]> packets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int offset = i * PAYLOAD, length = Math.min(PAYLOAD, bytes.length - offset);
            packets.add(ByteBuffer.allocate(HEADER + length).putInt(MAGIC)
                    .putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits())
                    .putInt(i).putInt(count).put(bytes, offset, length).array());
        }
        return packets;
    }

    public static Frame parse(byte[] bytes, int length) throws IOException {
        if (length <= HEADER || length > DATAGRAM) throw new IOException("Invalid datagram length");
        ByteBuffer in = ByteBuffer.wrap(bytes, 0, length);
        if (in.getInt() != MAGIC) throw new IOException("Invalid protocol header");
        UUID id = new UUID(in.getLong(), in.getLong());
        int index = in.getInt(), count = in.getInt();
        if (count < 1 || count > (Serialization.MAX_BYTES + PAYLOAD - 1) / PAYLOAD || index < 0 || index >= count)
            throw new IOException("Invalid fragment index");
        byte[] body = new byte[in.remaining()];
        in.get(body);
        if (index < count - 1 && body.length != PAYLOAD) throw new IOException("Short fragment");
        return new Frame(id, index, count, body);
    }

    /** One fragment of a message. */
    public record Frame(UUID id, int index, int count, byte[] body) { }

    /** Reassembles unordered/duplicate frames; the caller expires incomplete assemblies. */
    public static final class Assembly {
        private final byte[][] parts;
        private int received;
        public final long created = System.nanoTime();
        public Assembly(int count) { parts = new byte[count][]; }
        public byte[] add(Frame frame) throws IOException {
            if (frame.count() != parts.length) throw new IOException("Fragment count changed");
            if (parts[frame.index()] == null) { parts[frame.index()] = frame.body(); received++; }
            if (received != parts.length) return null;
            int size = Arrays.stream(parts).mapToInt(p -> p.length).sum();
            if (size > Serialization.MAX_BYTES) throw new IOException("Message too large");
            ByteBuffer result = ByteBuffer.allocate(size);
            Arrays.stream(parts).forEach(result::put);
            return result.array();
        }
    }
}
