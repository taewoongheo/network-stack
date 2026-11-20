package lab;

import java.util.*;
import org.junit.jupiter.api.Test;

class Check4Test extends Fixtures {
    final List<OptionalDouble> rtts = List.of(OptionalDouble.of(12), OptionalDouble.of(3),
            OptionalDouble.empty(), OptionalDouble.empty(), OptionalDouble.of(5));
    @Test void statistics() {
        assertEquals(new PathStats.Summary(0.6, 2, 2, 3, 12), PathStats.summarize(rtts));
    }
    @Test void lagUsesOnlyValidPairs() {
        assertEquals(0.5, PathStats.conditional(rtts, 1, true), 1e-9);
        assertEquals(0.5, PathStats.conditional(rtts, -1, false), 1e-9);
        assertEquals(1, PathStats.conditional(rtts, 0, true));
        assertTrue(Double.isNaN(PathStats.conditional(rtts, 8, true)));
    }
}
