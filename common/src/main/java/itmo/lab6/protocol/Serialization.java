package itmo.lab6.protocol;

import java.io.*;

/** Bounded Java object serialization with an allowlist on both ends. */
public final class Serialization {
    public static final int MAX_BYTES = 4 * 1024 * 1024;
    private Serialization() { }

    public static byte[] encode(Serializable value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(value); }
        if (bytes.size() > MAX_BYTES) throw new IOException("Message exceeds 4 MiB; use a narrower query");
        return bytes.toByteArray();
    }

    public static <T> T decode(byte[] bytes, Class<T> type) throws IOException {
        if (bytes.length > MAX_BYTES) throw new IOException("Message too large");
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            in.setObjectInputFilter(info -> {
                if (info.depth() > 24 || info.references() > 200000 || info.arrayLength() > MAX_BYTES)
                    return ObjectInputFilter.Status.REJECTED;
                Class<?> c = info.serialClass();
                if (c == null) return ObjectInputFilter.Status.UNDECIDED;
                while (c.isArray()) c = c.getComponentType();
                String n = c.getName();
                return c.isPrimitive() || n.startsWith("itmo.lab6.model.") || n.startsWith("itmo.lab6.protocol.")
                        || n.equals("java.lang.String") || n.equals("java.lang.Object") || n.equals("java.lang.Enum")
                        || n.equals("java.lang.Integer") || n.equals("java.lang.Double") || n.equals("java.lang.Float")
                        || n.equals("java.lang.Number") || n.equals("java.util.UUID") || n.equals("java.util.ArrayList")
                        || n.equals("java.time.Ser") || n.equals("java.time.LocalDate")
                        ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
            });
            Object result = in.readObject();
            if (!type.isInstance(result)) throw new IOException("Unexpected object type");
            return type.cast(result);
        } catch (ClassNotFoundException | RuntimeException e) { throw new IOException("Invalid serialized object", e); }
    }
}
