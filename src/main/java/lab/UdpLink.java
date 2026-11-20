package lab;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/** A point-to-point virtual Ethernet cable. One UDP datagram carries one Ethernet frame. */
final class UdpLink implements AutoCloseable {
    final DatagramSocket socket;
    final NetworkInterface nic;
    private final byte[] buffer = new byte[65535];

    UdpLink(int localPort, String peerHost, int peerPort, int ip) throws IOException {
        socket = new DatagramSocket(localPort);
        socket.connect(new InetSocketAddress(peerHost, peerPort));
        socket.setSoTimeout(2);
        nic = new NetworkInterface(0x020000000000L | Integer.toUnsignedLong(ip), ip, frame -> {
            byte[] data = frame.encode();
            try { socket.send(new DatagramPacket(data, data.length)); }
            catch (PortUnreachableException ignored) { /* other endpoint may start later */ }
            catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
    void poll() throws IOException {
        var datagram = new DatagramPacket(buffer, buffer.length);
        try { socket.receive(datagram); }
        catch (SocketTimeoutException | PortUnreachableException e) { return; }
        nic.receiveFrame(Wire.Frame.decode(Arrays.copyOf(buffer, datagram.getLength())));
    }
    public void close() { socket.close(); }
}
