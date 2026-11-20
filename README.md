# CS144 networking labs in Java

Open this directory as a Gradle project and select JDK 17. Use the same project for Checkpoints 0–7. Each week’s Lab directory contains the corresponding English handout, named `Checkpoint N - Java.pdf`.

```sh
cd "$HOME/Desktop/network/Week 01/Lab/imp-java"
./gradlew test --tests lab.SupportTest
./gradlew test --tests lab.Check0Test
```

The first test exercises the supplied support code. The second tests your implementation and will fail until you fill in the relevant methods. To run a single exercise, use `./gradlew test --tests lab.Check0Test.capacityAndEof`. Gradle downloads its dependencies on first use.

| Checkpoint | Implementation or experiment | Tests |
|---|---|---|
| 0 | Webget, ByteStream | Check0Test |
| 1 | Reassembler | Check1Test |
| 2 | Wrap32, TcpReceiver | Check2Test |
| 3 | TcpSender and end-to-end byte delivery | Check3Test, TransferTest |
| 4 | PathStats and measurements of two Internet paths | Check4Test |
| 5 | NetworkInterface | Check5Test |
| 6 | Router | Check6Test |
| 7 | Connect two hosts through a router, extend the topology, build a networking application | Check7Test |

Open `src/main/java/lab` for the starter classes and `src/test/java/lab` for the test cases. Run `./gradlew test` to exercise all checkpoints.

Messages carry bytes in `byte[]` arrays. After passing an array to a message, leave its contents unchanged. `Wrap32.raw()` stores an unsigned 32-bit pattern in an `int`. Absolute positions and cumulative counters use nonnegative `long` values. Each `tick` argument is elapsed time in milliseconds. Packet decoders throw `IllegalArgumentException` on malformed input. Reading and writing share a single `ByteStream` instance.

Checkpoint 3 simulates loss and reordering in memory. Checkpoint 4 uses real measurement data. Checkpoint 7 carries Ethernet frames over ordinary UDP sockets between Java processes. The original raw-IP and Linux TUN/TAP experiments require a separate native adapter.

Based on the Stanford CS144 lab handouts. The original PDFs are beside the Java editions.
