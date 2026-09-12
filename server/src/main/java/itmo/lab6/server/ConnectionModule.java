package itmo.lab6.server;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;

/** Owns the nonblocking UDP channel and selector; UDP has no handshake. */
public final class ConnectionModule implements AutoCloseable {
    private final DatagramChannel channel;
    private final Selector selector;
    public ConnectionModule(String host, int port) throws IOException {
        channel = DatagramChannel.open();
        selector = Selector.open();
        try {
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
            channel.bind(new InetSocketAddress(host, port));
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException | RuntimeException e) { close(); throw e; }
    }
    public DatagramChannel channel() { return channel; }
    public void await(boolean sending) throws IOException {
        selector.select(sending ? 1 : 50);
        selector.selectedKeys().clear();
    }
    public void close() throws IOException { try { channel.close(); } finally { selector.close(); } }
}
