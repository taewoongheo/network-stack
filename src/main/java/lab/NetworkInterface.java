package lab;

import java.util.*;
import java.util.function.Consumer;

public final class NetworkInterface {
    public final long mac;
    public final int ip;
    public final Queue<Wire.Ip> received = new ArrayDeque<>();
    private final Consumer<Wire.Frame> transmit;

    public NetworkInterface(long mac, int ip, Consumer<Wire.Frame> transmit) {
        this.mac = mac;
        this.ip = ip;
        this.transmit = transmit;
    }

    public void sendDatagram(Wire.Ip datagram, int nextHop) {
        throw new UnsupportedOperationException("TODO NetworkInterface.sendDatagram");
    }

    public void receiveFrame(Wire.Frame frame) {
        throw new UnsupportedOperationException("TODO NetworkInterface.receiveFrame");
    }

    public void tick(long elapsedMillis) {
        throw new UnsupportedOperationException("TODO NetworkInterface.tick");
    }
}
