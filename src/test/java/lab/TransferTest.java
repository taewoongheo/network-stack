package lab;

import java.util.*;

import org.junit.jupiter.api.Test;

/**
 * Checkpoint 3 integration: a virtual clock, one dropped data segment, delayed delivery, no kernel TCP.
 */
class TransferTest extends Fixtures {
    @Test
    void deliverOneMiBAfterLoss() {
        byte[] source = new byte[1024 * 1024];
        new Random(144).nextBytes(source);

        var input = new ByteStream(source.length);
        input.push(source);
        input.close();

        var output = new ByteStream(8192);
        var tx = new TcpSender(input, new Wrap32(-10), 20);
        var rx = new TcpReceiver(new Reassembler(output));

        var queue = new ArrayList<Segment>();
        var result = new java.io.ByteArrayOutputStream();
        boolean dropped = false;

        for (int t = 0; t < 20000; t++) {
            tx.push(queue::add);
            tx.tick(5, queue::add);
            Collections.reverse(queue);

            for (var segment : queue) {
                if (!dropped && segment.payload().length > 0) {
                    dropped = true;
                    continue;
                }

                rx.receive(segment);
                result.writeBytes(output.read(output.bytesBuffered()));
                tx.receive(rx.send());
            }

            queue.clear();
            if (output.isFinished() && tx.sequenceNumbersInFlight() == 0) {
                break;
            }
        }

        assertTrue(dropped && output.isFinished());
        assertEquals(0, tx.sequenceNumbersInFlight());
        assertArrayEquals(source, result.toByteArray());
    }
}
