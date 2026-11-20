package lab;

import java.util.*;

public final class TcpReceiver {
    public final Reassembler reassembler;

    public TcpReceiver(Reassembler reassembler) {
        this.reassembler = reassembler;
    }

    public void receive(Segment message) {
        throw new UnsupportedOperationException("TODO TcpReceiver.receive");
    }

    public Ack send() {
        throw new UnsupportedOperationException("TODO TcpReceiver.send");
    }
}
