package lab;

import java.util.*;
import org.junit.jupiter.api.Test;

class Check5Test extends Fixtures {
    final List<Wire.Frame> sent = new ArrayList<>();
    final NetworkInterface nic = new NetworkInterface(11, 1, sent::add);
    @Test void resolveQueueExpire() {
        nic.sendDatagram(packet(9, 8), 2);
        nic.tick(4999);
        nic.sendDatagram(packet(8, 9), 2);
        assertEquals(1, sent.size());
        assertEquals(Wire.BROADCAST, sent.get(0).dst());
        assertEquals(2, Wire.Arp.decode(sent.get(0).payload()).targetIp());
        nic.receiveFrame(response(22, 2, 11, 1));
        assertEquals(3, sent.size());
        assertEquals(22, sent.get(1).dst());
        assertEquals(9, Wire.Ip.decode(sent.get(1).payload()).dst());
        nic.tick(30001);
        nic.sendDatagram(packet(9, 1), 2);
        assertEquals(Wire.ARP, sent.get(3).type());
    }
    @Test void requestAndInboundFilter() {
        var arp = new Wire.Arp(1, 22, 2, 0, 1);
        nic.receiveFrame(new Wire.Frame(Wire.BROADCAST, 22, Wire.ARP, arp.encode()));
        assertEquals(2, Wire.Arp.decode(sent.get(0).payload()).op());
        nic.sendDatagram(packet(2, 9), 2);
        assertEquals(Wire.IPV4, sent.get(1).type());
        byte[] p = packet(1, 4).encode();
        nic.receiveFrame(new Wire.Frame(77, 22, Wire.IPV4, p));
        nic.receiveFrame(new Wire.Frame(11, 22, Wire.IPV4, new byte[3]));
        assertTrue(nic.received.isEmpty());
        nic.receiveFrame(new Wire.Frame(11, 22, Wire.IPV4, p));
        assertEquals(4, nic.received.remove().ttl());
    }
}
