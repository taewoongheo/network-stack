package lab;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.jupiter.api.Assertions;

class Fixtures extends Assertions {
    static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    static Segment data(int seq, boolean syn, String text, boolean fin) {
        return new Segment(new Wrap32(seq), syn, bytes(text), fin, false);
    }

    static Ack ack(int seq, int win) {
        return new Ack(Optional.of(new Wrap32(seq)), win, false);
    }

    static Wire.Ip packet(int dst, int ttl) {
        return new Wire.Ip(7, dst, ttl, 17, bytes("hi"));
    }

    static Wire.Frame response(long mac, int ip, long target, int targetIp) {
        return new Wire.Frame(
                target, mac, Wire.ARP,
                new Wire.Arp(2, mac, ip, target, targetIp).encode()
        );
    }
}
