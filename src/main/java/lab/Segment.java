package lab;

public record Segment(Wrap32 seq, boolean syn, byte[] payload, boolean fin, boolean rst) {
    public int length() { return payload.length + (syn ? 1 : 0) + (fin ? 1 : 0); }
}
