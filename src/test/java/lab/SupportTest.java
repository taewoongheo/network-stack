package lab;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SupportTest extends Fixtures {
    @Test void nestedWireRoundTripAndChecksum() {
        var s = data(-3, true, "abc", true);
        var t = new TcpWire(70, 80, s, ack(9, 3));
        var frame = new Wire.Frame(0xf0aabbccdd11L, 7, Wire.IPV4, t.packet(1, 2).encode());
        var decoded = Wire.Frame.decode(frame.encode());
        assertEquals(frame.dst(), decoded.dst());
        var ip = Wire.Ip.decode(decoded.payload());
        var tcp = TcpWire.decode(ip);
        assertArrayEquals(s.payload(), tcp.segment().payload());
        assertEquals(s.seq(), tcp.segment().seq());
        assertTrue(tcp.segment().syn() && tcp.segment().fin());
        assertEquals(t.ack(), tcp.ack());
        ip.payload()[20] ^= 1;
        assertThrows(IllegalArgumentException.class, () -> TcpWire.decode(ip));
    }
    @Test void loopbackCable() throws Exception {
        try (var peer = new java.net.DatagramSocket(0);
             var link = new UdpLink(0, "127.0.0.1", peer.getLocalPort(), 1)) {
            var bytes = new Wire.Frame(1, 2, Wire.ARP, new Wire.Arp(1, 2, 3, 0, 1).encode()).encode();
            peer.send(new java.net.DatagramPacket(bytes, bytes.length,
                    java.net.InetAddress.getByName("127.0.0.1"), link.socket.getLocalPort()));
            byte[] buffer = new byte[100];
            var d = new java.net.DatagramPacket(buffer, buffer.length);
            link.socket.setSoTimeout(2000);
            link.socket.receive(d);
            assertArrayEquals(bytes, Arrays.copyOf(buffer, d.getLength()));
        }
    }
}
