package lab;


public final class ByteStream {
    private final int capacity;
    private boolean error;

    public ByteStream(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("negative capacity");
        this.capacity = capacity;
    }

    public void setError() {
        error = true;
    }

    public boolean hasError() {
        return error;
    }

    /**
     * Provided reader helper. peek() may return just the first contiguous chunk.
     */
    public byte[] read(int limit) {
        var result = new java.io.ByteArrayOutputStream();
        while (result.size() < limit && bytesBuffered() > 0) {
            byte[] chunk = peek();
            int n = Math.min(chunk.length, limit - result.size());
            if (n == 0) throw new IllegalStateException("buffered data must be readable");
            result.write(chunk, 0, n);
            pop(n);
        }
        return result.toByteArray();
    }

    public void push(byte[] data) {
        throw new UnsupportedOperationException("TODO ByteStream.push");
    }

    public void close() {
        throw new UnsupportedOperationException("TODO ByteStream.close");
    }

    public boolean isClosed() {
        throw new UnsupportedOperationException("TODO ByteStream.isClosed");
    }

    public int availableCapacity() {
        throw new UnsupportedOperationException("TODO ByteStream.availableCapacity");
    }

    public long bytesPushed() {
        throw new UnsupportedOperationException("TODO ByteStream.bytesPushed");
    }

    public byte[] peek() {
        throw new UnsupportedOperationException("TODO ByteStream.peek");
    }

    public void pop(int length) {
        throw new UnsupportedOperationException("TODO ByteStream.pop");
    }

    public boolean isFinished() {
        throw new UnsupportedOperationException("TODO ByteStream.isFinished");
    }

    public int bytesBuffered() {
        throw new UnsupportedOperationException("TODO ByteStream.bytesBuffered");
    }

    public long bytesPopped() {
        throw new UnsupportedOperationException("TODO ByteStream.bytesPopped");
    }
}
