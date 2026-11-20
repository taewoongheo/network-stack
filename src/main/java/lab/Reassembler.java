package lab;


public final class Reassembler {
    public final ByteStream output;

    public Reassembler(ByteStream output) {
        this.output = output;
    }

    public void insert(long firstIndex, byte[] data, boolean last) {
        throw new UnsupportedOperationException("TODO Reassembler.insert");
    }

    public long bytesPending() {
        throw new UnsupportedOperationException("TODO Reassembler.bytesPending");
    }
}
