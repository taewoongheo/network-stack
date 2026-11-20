package lab;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Parses Linux iputils ping -D -n output. One run, at most 65535 probes. */
public final class Measure {
    private static final Pattern REPLY = Pattern.compile(
            "^\\[([0-9.]+)\\].*icmp_seq=(\\d+).*time=([0-9.]+)\\s*ms");
    private static final Pattern TOTAL = Pattern.compile("^(\\d+) packets transmitted,.*");

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Measure ping.log new-output-folder");
        var lines = Files.readAllLines(Path.of(args[0]));
        int count = lines.stream().map(TOTAL::matcher).filter(m -> m.matches())
                .mapToInt(m -> Integer.parseInt(m.group(1))).findFirst().orElseThrow(
                        () -> new IllegalArgumentException("need Linux ping final transmitted count"));
        if (count < 1 || count > 65535) throw new IllegalArgumentException("need 1..65535 probes");
        var rtts = new ArrayList<>(Collections.nCopies(count, OptionalDouble.empty()));
        var times = new String[count];
        for (String line : lines) {
            var m = REPLY.matcher(line);
            if (!m.find()) continue;
            int i = Integer.parseInt(m.group(2)) - 1;
            if (i < 0 || i >= count) throw new IllegalArgumentException("invalid sequence number");
            if (rtts.get(i).isEmpty()) {
                times[i] = m.group(1);
                rtts.set(i, OptionalDouble.of(Double.parseDouble(m.group(3))));
            }
        }
        var summary = PathStats.summarize(rtts);
        var dir = Files.createDirectory(Path.of(args[1]));
        try (var probes = Files.newBufferedWriter(dir.resolve("probes.csv"));
             var lag = Files.newBufferedWriter(dir.resolve("lag.csv"));
             var pairs = Files.newBufferedWriter(dir.resolve("adjacent.csv"));
             var cdf = Files.newBufferedWriter(dir.resolve("cdf.csv"))) {
            probes.write("seq,unix_seconds,rtt_ms\n");
            pairs.write("rtt_ms,next_rtt_ms\n");
            for (int i = 0; i < count; i++) {
                var v = rtts.get(i);
                probes.write((i + 1) + "," + (v.isPresent() ? times[i] + "," + v.getAsDouble() : ",") + "\n");
                if (v.isPresent() && i + 1 < count && rtts.get(i + 1).isPresent())
                    pairs.write(v.getAsDouble() + "," + rtts.get(i + 1).getAsDouble() + "\n");
            }
            lag.write("k,success_given_success,loss_given_loss\n");
            for (int k = -10; k <= 10; k++)
                lag.write(k + "," + PathStats.conditional(rtts, k, true) + "," + PathStats.conditional(rtts, k, false) + "\n");
            cdf.write("rtt_ms,cumulative_fraction\n");
            double[] sorted = rtts.stream().flatMapToDouble(OptionalDouble::stream).sorted().toArray();
            for (int i = 0; i < sorted.length; i++) {
                if (i + 1 == sorted.length || sorted[i + 1] != sorted[i])
                    cdf.write(sorted[i] + "," + (i + 1.0) / sorted.length + "\n");
            }
        }
        System.out.println(summary);
    }
}
