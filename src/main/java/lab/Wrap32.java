package lab;


public record Wrap32(int raw) {


    public static Wrap32 wrap(long absolute, Wrap32 zero) {
        throw new UnsupportedOperationException("TODO Wrap32.wrap");
    }

    public long unwrap(Wrap32 zero, long checkpoint) {
        throw new UnsupportedOperationException("TODO Wrap32.unwrap");
    }
}
