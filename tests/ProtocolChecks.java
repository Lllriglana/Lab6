import itmo.lab6.client.UdpClient;
import itmo.lab6.model.*;
import itmo.lab6.protocol.*;
import java.util.*;
import java.io.*;
import java.net.*;

/** End-to-end assertions using actual typed network requests. */
public final class ProtocolChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static Request request(CommandType command, Integer id, Weapon weapon, MarineData data) {
        return new Request(UUID.randomUUID(), command, id, weapon, data);
    }
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        // Reorder and duplicate fragments of a message larger than UDP's payload limit.
        byte[] serialized = Serialization.encode("x".repeat(100000));
        List<byte[]> fragments = Packets.split(UUID.randomUUID(), serialized);
        Collections.reverse(fragments);
        Packets.Assembly assembly = new Packets.Assembly(fragments.size());
        Packets.Frame duplicate = Packets.parse(fragments.get(0), fragments.get(0).length);
        assembly.add(duplicate);
        byte[] reconstructed = null;
        for (byte[] fragment : fragments) reconstructed = assembly.add(Packets.parse(fragment, fragment.length));
        check(Arrays.equals(serialized, reconstructed), "unordered duplicate fragments");
        try (UdpClient client = new UdpClient("127.0.0.1", port)) {
            Response first = client.send(request(CommandType.SHOW, null, null, null));
            check(first.elements().size() == 120, "large fragmented response");
            for (int i = 1; i < first.elements().size(); i++)
                check(first.elements().get(i - 1).compareTo(first.elements().get(i)) <= 0, "default order");
            MarineData data = new MarineData("Network test", new Coordinates(1.0, 2f), 0.5,
                    null, null, null, new Chapter("Test chapter", 10));
            Request add = request(CommandType.ADD, null, null, data);
            Response once = client.send(add), twice = client.send(add);
            check(once.success() && once.message().equals(twice.message()), "cached add response");
            Response values = client.send(request(CommandType.SHOW, null, null, null));
            check(values.elements().size() == 121, "duplicate add was not executed twice");
            SpaceMarine added = values.elements().get(0);
            check(added.getId() == 121 && added.getCreationDate().equals(java.time.LocalDate.now()), "server-generated fields");
            check(client.send(request(CommandType.UPDATE, added.getId(), null, data)).success(), "update");
            check(!client.send(request(CommandType.ADD, null, null,
                    new MarineData("Bad", null, -2, null, null, null, null))).success(), "server validation");
            check(!client.send(request(CommandType.INFO, 10, null, null)).success(), "argument validation");
            check(client.send(request(CommandType.FILTER_BY_WEAPON_TYPE, null, null, null)).elements()
                    .stream().allMatch(m -> m.getWeaponType() == null), "null filter");
            client.send(request(CommandType.SHUFFLE, null, null, null));
            values = client.send(request(CommandType.SHOW, null, null, null));
            check(values.elements().get(0).getId() == 121, "sorted after shuffle");
            client.send(request(CommandType.REMOVE_BY_ID, 121, null, null));
            check(client.send(request(CommandType.SHOW, null, null, null)).elements().size() == 120, "remove");
            try { CommandType.valueOf("SAVE"); throw new AssertionError("save is exposed"); }
            catch (IllegalArgumentException expected) { }
        }
        // Invalid datagrams and wrong serialized root objects must not terminate the server.
        try (DatagramSocket raw = new DatagramSocket()) {
            raw.connect(InetAddress.getLoopbackAddress(), port);
            raw.send(new DatagramPacket(new byte[]{1, 2, 3}, 3));
            for (byte[] bytes : Packets.split(UUID.randomUUID(), Serialization.encode("save")))
                raw.send(new DatagramPacket(bytes, bytes.length));
        }
        try (UdpClient client = new UdpClient("127.0.0.1", port)) {
            check(client.send(request(CommandType.INFO, null, null, null)).success(), "survives malformed packets");
        }
        System.out.println("ProtocolChecks passed");
    }
}
