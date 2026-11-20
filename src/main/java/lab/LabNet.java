package lab;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

/** Commands are documented in the Checkpoint 7 handout. All elapsed times are milliseconds. */
public final class LabNet {
    static int ip(String address) throws IOException {
        byte[] bytes = InetAddress.getByName(address).getAddress();
        if (bytes.length != 4) throw new IllegalArgumentException("IPv4 address required");
        return ByteBuffer.wrap(bytes).getInt();
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("webget | router | client | server");
        switch (args[0]) {
            case "webget" -> Webget.fetch(args[1], Integer.parseInt(args[2]), args[3], System.out);
            case "router" -> router(Arrays.copyOfRange(args, 1, args.length));
            case "client", "server" -> host(args);
            default -> throw new IllegalArgumentException(args[0]);
        }
    }
    private static long now() { return System.nanoTime() / 1_000_000; }

    // Each link: ip,udpLocal,peerHost,udpPeer. Each route: prefix/length,index,nextHop-or-direct.
    private static void router(String[] specs) throws Exception {
        var router = new Router();
        var links = new ArrayList<UdpLink>();
        try {
            for (String spec : specs) {
                String[] f = spec.split(",");
                if (f[0].contains("/")) {
                    String[] cidr = f[0].split("/");
                    var hop = f[2].equals("direct") ? OptionalInt.empty() : OptionalInt.of(ip(f[2]));
                    router.addRoute(ip(cidr[0]), Integer.parseInt(cidr[1]), hop, Integer.parseInt(f[1]));
                } else {
                    var link = new UdpLink(Integer.parseInt(f[1]), f[2], Integer.parseInt(f[3]), ip(f[0]));
                    links.add(link);
                    router.addInterface(link.nic);
                }
            }
            if (links.isEmpty()) throw new IllegalArgumentException("at least one interface required");
            long last = now();
            while (true) {
                for (var link : links) link.poll();
                long current = now();
                for (var link : links) link.nic.tick(current - last);
                last = current;
                router.route();
            }
        } finally { links.forEach(UdpLink::close); }
    }

    // One full-duplex TCP conversation per process. Both peers stream a file and save their received bytes.
    private static void host(String[] a) throws Exception {
        if (a.length != 12) throw new IllegalArgumentException(
                "client|server udpLocal peerHost udpPeer myIp myTcpPort peerIp peerTcpPort gateway input output idleTimeoutMs");
        int local = ip(a[4]), remote = ip(a[6]), gateway = ip(a[8]);
        int srcPort = Integer.parseInt(a[5]), dstPort = Integer.parseInt(a[7]);
        long idleLimit = Long.parseLong(a[11]);
        var outbound = new ByteStream(64000);
        var inbound = new ByteStream(64000);
        var sender = new TcpSender(outbound, new Wrap32(new Random().nextInt()), 1000);
        var receiver = new TcpReceiver(new Reassembler(inbound));
        try (var link = new UdpLink(Integer.parseInt(a[1]), a[2], Integer.parseInt(a[3]), local);
             var file = Files.newInputStream(Path.of(a[9]));
             var sink = Files.newOutputStream(Path.of(a[10]), StandardOpenOption.CREATE_NEW)) {
            Consumer<Segment> send = segment -> link.nic.sendDatagram(
                    new TcpWire(srcPort, dstPort, segment, receiver.send()).packet(local, remote), gateway);
            boolean active = a[0].equals("client");
            long last = now(), lastPacket = last;
            while (true) {
                long current = now();
                link.nic.tick(current - last);
                if (active) sender.tick(current - last, send);
                last = current;
                if (!outbound.isClosed() && outbound.availableCapacity() > 0) {
                    byte[] next = file.readNBytes(Math.min(8192, outbound.availableCapacity()));
                    if (next.length == 0) outbound.close(); else outbound.push(next);
                }
                if (active) sender.push(send);
                link.poll();
                while (!link.nic.received.isEmpty()) {
                    var packet = link.nic.received.remove();
                    if (packet.src() != remote || packet.dst() != local || packet.protocol() != 6) continue;
                    var tcp = TcpWire.decode(packet);
                    if (tcp.sourcePort() != dstPort || tcp.destinationPort() != srcPort) continue;
                    if (!active && !tcp.segment().syn()) continue;
                    lastPacket = now();
                    active = true;
                    receiver.receive(tcp.segment());
                    sender.receive(tcp.ack());
                    if (inbound.hasError() || outbound.hasError()) throw new IOException("TCP reset");
                    sink.write(inbound.read(inbound.bytesBuffered()));
                    if (tcp.segment().length() > 0) send.accept(sender.makeEmptyMessage());
                }
                if (inbound.isFinished() && outbound.isFinished() && sender.sequenceNumbersInFlight() == 0
                        && now() - lastPacket > 2500) return;
                if (now() - lastPacket > idleLimit) throw new IOException("peer timed out");
            }
        }
    }
}
