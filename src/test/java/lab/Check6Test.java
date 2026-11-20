package lab;

import java.util.*;
import org.junit.jupiter.api.Test;

class Check6Test extends Fixtures {
    @Test void routesAndTtl() {
        var a = new ArrayList<Wire.Frame>();
        var b = new ArrayList<Wire.Frame>();
        var left = new NetworkInterface(11, 1, a::add);
        var right = new NetworkInterface(22, 2, b::add);
        var r = new Router();
        int l = r.addInterface(left), h = r.addInterface(right);
        int dst = 0x80000002;
        r.addRoute(0x80000000, 1, OptionalInt.of(3), h);
        r.addRoute(dst, 32, OptionalInt.empty(), l);
        left.received.add(packet(12, 8));
        left.received.add(packet(dst, 1));
        r.route();
        assertTrue(a.isEmpty() && b.isEmpty());
        r.addRoute(0, 0, OptionalInt.of(4), l);
        left.receiveFrame(response(33, dst, 11, 1));
        right.receiveFrame(response(44, 3, 22, 2));
        left.receiveFrame(response(55, 4, 11, 1));
        left.received.add(packet(dst, 8));
        left.received.add(packet(dst + 1, 8));
        left.received.add(packet(12, 8));
        r.route();
        assertEquals(List.of(33L,55L), a.stream().map(Wire.Frame::dst).toList());
        assertEquals(44L, b.get(0).dst());
        assertEquals(7, Wire.Ip.decode(a.get(0).payload()).ttl());
    }
}
