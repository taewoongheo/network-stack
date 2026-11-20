package lab;

import java.util.Optional;

public record Ack(Optional<Wrap32> number, int window, boolean rst) {
    public Ack {
        if (window < 0 || window > 65535) throw new IllegalArgumentException("window");
    }
}
