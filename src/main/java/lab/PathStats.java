package lab;

import java.util.*;

public final class PathStats {
    /** Index zero denotes the first probe. Missing replies have an empty RTT. */
    public record Summary(double deliveryRate, int longestSuccess, int longestLoss,
                          double minRtt, double maxRtt) {}

    public static Summary summarize(List<OptionalDouble> rtts) {
        throw new UnsupportedOperationException("TODO PathStats.summarize");
    }

    public static double conditional(List<OptionalDouble> rtts, int lag, boolean success) {
        throw new UnsupportedOperationException("TODO PathStats.conditional");
    }
}
