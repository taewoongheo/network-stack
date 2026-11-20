package lab;

import java.util.*;
import java.util.function.Consumer;

public final class TcpSender {
    public static final int MAX_PAYLOAD = 1000;
    public final ByteStream input;
    private final Wrap32 isn;
    private final long initialRto;
    public TcpSender(ByteStream input, Wrap32 isn, long initialRto) {
        this.input = input;
        this.isn = isn;
        this.initialRto = initialRto;
    }

    public Segment makeEmptyMessage() {
        throw new UnsupportedOperationException("TODO TcpSender.makeEmptyMessage");
    }

    public void push(Consumer<Segment> transmit) {
        throw new UnsupportedOperationException("TODO TcpSender.push");
    }

    public void receive(Ack message) {
        throw new UnsupportedOperationException("TODO TcpSender.receive");
    }

    public void tick(long elapsedMillis, Consumer<Segment> transmit) {
        throw new UnsupportedOperationException("TODO TcpSender.tick");
    }

    public long sequenceNumbersInFlight() {
        throw new UnsupportedOperationException("TODO TcpSender.sequenceNumbersInFlight");
    }

    public long consecutiveRetransmissions() {
        throw new UnsupportedOperationException("TODO TcpSender.consecutiveRetransmissions");
    }
}
