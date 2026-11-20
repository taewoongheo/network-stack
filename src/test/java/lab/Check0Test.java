package lab;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

class Check0Test extends Fixtures {
    @Test void capacityAndEof() {
        var b = new ByteStream(3);
        b.push(bytes("abcd"));
        assertEquals(0, b.availableCapacity());
        assertArrayEquals(bytes("ab"), b.read(2));
        b.push(new byte[] {0, -1});
        b.close();
        assertTrue(b.isClosed());
        assertFalse(b.isFinished());
        assertArrayEquals(new byte[] {'c', 0, -1}, b.read(10));
        assertTrue(b.isFinished());
        assertEquals(5, b.bytesPushed());
        assertEquals(5, b.bytesPopped());
    }
    @Test void emptyStream() {
        var b = new ByteStream(0);
        b.push(bytes("x"));
        assertEquals(0, b.peek().length);
        b.close();
        assertTrue(b.isFinished());
    }
    @Test @org.junit.jupiter.api.Timeout(8) void httpOnLoopback() throws Exception {
        try (var server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            var pool = Executors.newSingleThreadExecutor();
            try {
                var request = pool.submit(() -> {
                    try (var connection = server.accept()) {
                        connection.setSoTimeout(3000);
                        var lines = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        var headers = new java.util.ArrayList<String>();
                        String line;
                        while ((line = lines.readLine()) != null && !line.isEmpty()) headers.add(line);
                        connection.getOutputStream().write(bytes("HTTP/1.1 200 OK\r\nConnection: close\r\n\r\nhi"));
                        return headers;
                    }
                });
                var output = new ByteArrayOutputStream();
                Webget.fetch("127.0.0.1", server.getLocalPort(), "/lab", output);
                var headers = request.get(3, TimeUnit.SECONDS);
                assertEquals("GET /lab HTTP/1.1", headers.get(0));
                assertTrue(headers.stream().anyMatch(h -> h.toLowerCase().startsWith("host: 127.0.0.1")));
                assertTrue(headers.stream().anyMatch(h -> h.equalsIgnoreCase("Connection: close")));
                assertArrayEquals(bytes("HTTP/1.1 200 OK\r\nConnection: close\r\n\r\nhi"), output.toByteArray());
            } finally { pool.shutdownNow(); }
        }
    }
}
