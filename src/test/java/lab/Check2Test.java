package lab;

import org.junit.jupiter.api.Test;

class Check2Test extends Fixtures {
    @Test
    void wrapAndUnwrapAcrossEpoch() {
        var zero = new Wrap32(-2);

        assertEquals(new Wrap32(1), Wrap32.wrap(3, zero));
        assertEquals((1L << 32) + 3, new Wrap32(1).unwrap(zero, (1L << 32) - 2));
        assertEquals(3, new Wrap32(1).unwrap(zero, 0));
    }

    @Test
    void cumulativeAckAndWindow() {
        var b = new ByteStream(3);
        var rx = new TcpReceiver(new Reassembler(b));

        rx.receive(data(18, false, "x", false));
        assertTrue(rx.send().number().isEmpty());

        rx.receive(data(-1, true, "", false));
        assertEquals(ack(0, 3), rx.send());

        rx.receive(data(1, false, "b", true));
        assertEquals(ack(0, 3), rx.send());

        rx.receive(data(0, false, "a", false));
        assertEquals(ack(3, 1), rx.send());

        assertArrayEquals(bytes("ab"), b.read(2));
        assertEquals(3, rx.send().window());
    }

    @Test
    void maximumWindowAndReset() {
        var b = new ByteStream(80000);
        var rx = new TcpReceiver(new Reassembler(b));
        assertEquals(65535, rx.send().window());

        rx.receive(new Segment(new Wrap32(1), true, new byte[0], false, true));
        assertTrue(b.hasError());
        assertTrue(rx.send().rst());
    }
}
