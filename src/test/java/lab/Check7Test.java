package lab;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.DatagramSocket;

class Check7Test extends Fixtures {
    @TempDir Path dir;
    List<Process> processes = new ArrayList<>();
    @AfterEach void stop() throws Exception {
        for (var p : processes) p.destroyForcibly().waitFor();
    }
    private Process run(String name, String... args) throws Exception {
        String cp = Path.of(LabNet.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        var cmd = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", cp, "lab.LabNet"));
        cmd.addAll(List.of(args));
        var p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true)
                .redirectOutput(dir.resolve(name + ".log").toFile()).start();
        processes.add(p);
        return p;
    }
    @Test @Timeout(45) void twoHostsOneRouter() throws Exception {
        var ports = new ArrayList<String>();
        var sockets = new ArrayList<DatagramSocket>();
        try {
            for (int i = 0; i < 4; i++) {
                var s = new DatagramSocket(0);
                sockets.add(s);
                ports.add(Integer.toString(s.getLocalPort()));
            }
        } finally { sockets.forEach(DatagramSocket::close); }
        String host = "127.0.0.1";
        byte[] file = new byte[5000];
        new Random(144).nextBytes(file);
        Files.write(dir.resolve("a.in"), file);
        Files.writeString(dir.resolve("b.in"), "return trip");
        var router = run("router", "router", "50.0.0.1,"+ports.get(0)+","+host+","+ports.get(2),
                "80.0.0.1,"+ports.get(1)+","+host+","+ports.get(3), "50.0.0.0/8,0,direct","80.0.0.0/8,1,direct");
        var server = run("server", "server", ports.get(2), host, ports.get(0), "50.9.8.7","80",
                "80.6.5.4","5000","50.0.0.1","a.in","a.out","15000");
        var client = run("client", "client", ports.get(3), host, ports.get(1), "80.6.5.4","5000",
                "50.9.8.7","80","80.0.0.1","b.in","b.out","15000");
        for (var p : List.of(client,server)) {
            assertTrue(p.waitFor(20, TimeUnit.SECONDS), "host timed out");
            String name = p == client ? "client" : "server";
            assertEquals(0, p.exitValue(), () -> {
                try { return Files.readString(dir.resolve(name + ".log")) + Files.readString(dir.resolve("router.log")); }
                catch (Exception e) { return e.toString(); }
            });
        }
        assertTrue(router.isAlive());
        assertArrayEquals(file, Files.readAllBytes(dir.resolve("b.out")));
        assertEquals("return trip", Files.readString(dir.resolve("a.out")));
    }
}
