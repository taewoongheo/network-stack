package lab;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

/** TCP header subset used by the lab, including the IPv4 pseudo-header checksum. */
public record TcpWire(int sourcePort, int destinationPort, Segment segment, Ack ack) {
    public Wire.Ip packet(int src, int dst) {
        byte[] data = segment.payload();
        var b = ByteBuffer.allocate(20 + data.length);
        int flags = (segment.fin() ? 1 : 0) | (segment.syn() ? 2 : 0)
                | (segment.rst() || ack.rst() ? 4 : 0) | (ack.number().isPresent() ? 16 : 0);
        b.putShort((short) sourcePort).putShort((short) destinationPort).putInt(segment.seq().raw());
        b.putInt(ack.number().map(Wrap32::raw).orElse(0)).put((byte) 0x50).put((byte) flags);
        b.putShort((short) ack.window()).putInt(0).put(data);
        b.putShort(16, (short) checksum(src, dst, b.array()));
        return new Wire.Ip(src, dst, 64, 6, b.array());
    }
    public static TcpWire decode(Wire.Ip ip) {
        byte[] data = ip.payload();
        Wire.require(ip.protocol() == 6 && data.length >= 20 && checksum(ip.src(), ip.dst(), data) == 0);
        var b = ByteBuffer.wrap(data);
        int start = (data[12] >>> 4 & 15) * 4;
        Wire.require(start >= 20 && start <= data.length);
        int flags = data[13];
        boolean rst = (flags & 4) != 0;
        var segment = new Segment(new Wrap32(b.getInt(4)), (flags & 2) != 0,
                Arrays.copyOfRange(data, start, data.length), (flags & 1) != 0, rst);
        var number = (flags & 16) == 0 ? Optional.<Wrap32>empty() : Optional.of(new Wrap32(b.getInt(8)));
        return new TcpWire(b.getShort(0) & 65535, b.getShort(2) & 65535, segment,
                new Ack(number, b.getShort(14) & 65535, rst));
    }
    private static int checksum(int src, int dst, byte[] data) {
        byte[] bytes = ByteBuffer.allocate(data.length + 12).putInt(src).putInt(dst)
                .putShort((short) 6).putShort((short) data.length).put(data).array();
        return Wire.checksum(bytes, bytes.length);
    }
}
