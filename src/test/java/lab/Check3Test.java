package lab;

import java.util.*;
import org.junit.jupiter.api.Test;

class Check3Test extends Fixtures {
    final ByteStream b = new ByteStream(5000);
    final TcpSender tx = new TcpSender(b, new Wrap32(100), 10);
    final List<Segment> out = new ArrayList<>();
    void connect(int window) {
        tx.push(out::add);
        assertEquals(1, out.size());
        assertTrue(out.remove(0).syn());
        tx.receive(ack(101, window));
    }
    @Test void retransmitOldestWithBackoff() {
        connect(2);
        b.push(bytes("abc"));
        tx.push(out::add);
        var first = out.remove(0);
        assertArrayEquals(bytes("ab"), first.payload());
        tx.tick(9, out::add);
        assertTrue(out.isEmpty());
        tx.tick(1, out::add);
        assertEquals(first.seq(), out.remove(0).seq());
        tx.tick(19, out::add);
        assertTrue(out.isEmpty());
        tx.tick(1, out::add);
        assertArrayEquals(first.payload(), out.remove(0).payload());
        assertEquals(2, tx.consecutiveRetransmissions());
        tx.receive(ack(120, 2));
        assertEquals(2, tx.sequenceNumbersInFlight());
        tx.receive(ack(103, 2));
        assertEquals(0, tx.consecutiveRetransmissions());
        b.close();
        tx.push(out::add);
        assertEquals(2, out.stream().mapToInt(Segment::length).sum());
        assertTrue(out.get(out.size()-1).fin());
    }
    @Test void zeroWindowProbe() {
        connect(0);
        b.push(bytes("ab"));
        tx.push(out::add);
        assertArrayEquals(bytes("a"), out.remove(0).payload());
        tx.tick(10, out::add);
        tx.tick(10, out::add);
        assertEquals(2, out.size());
        assertEquals(0, tx.consecutiveRetransmissions());
    }
    @Test void segmentLimitAndEmptyMessage() {
        connect(4000);
        b.push(new byte[2401]);
        tx.push(out::add);
        assertEquals(2401, out.stream().mapToInt(Segment::length).sum());
        assertTrue(out.stream().allMatch(s -> s.payload().length <= 1000));
        assertEquals(0, tx.makeEmptyMessage().length());
        assertEquals(new Wrap32(2502), tx.makeEmptyMessage().seq());
    }
}
