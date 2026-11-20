package lab;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Ethernet II, Ethernet/IPv4 ARP, and unfragmented IPv4 with no options. Network byte order.
 */
public final class Wire {
    public static final int IPV4 = 0x0800, ARP = 0x0806;
    public static final long BROADCAST = 0xffffffffffffL;

    public record Frame(long dst, long src, int type, byte[] payload) {
        public byte[] encode() {
            var b = ByteBuffer.allocate(14 + payload.length);
            putMac(b, dst);
            putMac(b, src);
            return b.putShort((short) type).put(payload).array();
        }

        public static Frame decode(byte[] bytes) {
            require(bytes.length >= 14);
            var b = ByteBuffer.wrap(bytes);
            return new Frame(mac(b), mac(b), Short.toUnsignedInt(b.getShort()), tail(b));
        }
    }

    public record Arp(int op, long senderMac, int senderIp, long targetMac, int targetIp) {
        public byte[] encode() {
            var b = ByteBuffer.allocate(28);
            b.putShort((short) 1).putShort((short) IPV4).put((byte) 6).put((byte) 4).putShort((short) op);
            putMac(b, senderMac);
            b.putInt(senderIp);
            putMac(b, targetMac);
            return b.putInt(targetIp).array();
        }

        public static Arp decode(byte[] bytes) {
            require(bytes.length >= 28);
            var b = ByteBuffer.wrap(bytes);
            require(b.getShort() == 1 && b.getShort() == IPV4 && b.get() == 6 && b.get() == 4);
            int op = b.getShort();
            require(op == 1 || op == 2);
            return new Arp(op, mac(b), b.getInt(), mac(b), b.getInt());
        }
    }

    public record Ip(int src, int dst, int ttl, int protocol, byte[] payload) {
        public byte[] encode() {
            require(payload.length <= 65515 && ttl >= 0 && ttl <= 255);
            var b = ByteBuffer.allocate(20 + payload.length);
            b.put((byte) 0x45).put((byte) 0).putShort((short) b.capacity()).putInt(0);
            b.put((byte) ttl).put((byte) protocol).putShort((short) 0).putInt(src).putInt(dst);
            b.putShort(10, (short) checksum(b.array(), 20));
            return b.put(payload).array();
        }

        public static Ip decode(byte[] bytes) {
            require(bytes.length >= 20 && bytes[0] == 0x45 && checksum(bytes, 20) == 0);
            var b = ByteBuffer.wrap(bytes);
            int length = Short.toUnsignedInt(b.getShort(2));
            require(length >= 20 && length <= bytes.length && (b.getShort(6) & 0x3fff) == 0);
            return new Ip(b.getInt(12), b.getInt(16), bytes[8] & 255, bytes[9] & 255,
                    Arrays.copyOfRange(bytes, 20, length));
        }
    }

    static int checksum(byte[] data, int length) {
        long sum = 0;
        for (int i = 0; i < length; i += 2) {
            sum += (data[i] & 255) << 8;
            if (i + 1 < length) sum += data[i + 1] & 255;
        }
        while ((sum >>> 16) != 0) sum = (sum & 65535) + (sum >>> 16);
        return (int) ~sum & 65535;
    }

    private static void putMac(ByteBuffer b, long addr) {
        for (int shift = 40; shift >= 0; shift -= 8) b.put((byte) (addr >>> shift));
    }

    private static long mac(ByteBuffer b) {
        long addr = 0;
        for (int i = 0; i < 6; i++) addr = (addr << 8) | (b.get() & 255);
        return addr;
    }

    private static byte[] tail(ByteBuffer b) {
        byte[] data = new byte[b.remaining()];
        b.get(data);
        return data;
    }

    static void require(boolean ok) {
        if (!ok) throw new IllegalArgumentException("invalid packet");
    }
}
