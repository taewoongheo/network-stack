package lab;

import org.junit.jupiter.api.Test;

class Check1Test extends Fixtures {
    @Test void holesOverlapAndFin() {
        var b = new ByteStream(8);
        var r = new Reassembler(b);
        r.insert(3, bytes("de"), true);
        r.insert(2, bytes("cd"), false);
        assertEquals(3, r.bytesPending());
        assertEquals(0, b.bytesBuffered());
        r.insert(0, bytes("abc"), false);
        assertArrayEquals(bytes("abcde"), b.read(7));
        assertTrue(b.isFinished());
    }
    @Test void rightEdgeMovesOnlyAfterRead() {
        var b = new ByteStream(3);
        var r = new Reassembler(b);
        r.insert(2, bytes("cde"), false);
        assertEquals(1, r.bytesPending());
        r.insert(0, bytes("ab"), false);
        assertArrayEquals(bytes("abc"), b.read(10));
        assertFalse(b.isFinished());
        r.insert(2, bytes("cde"), true);
        assertArrayEquals(bytes("de"), b.read(10));
        assertTrue(b.isFinished());
    }
    @Test void emptyFin() {
        var b = new ByteStream(1);
        new Reassembler(b).insert(0, new byte[0], true);
        assertTrue(b.isFinished());
    }
}
