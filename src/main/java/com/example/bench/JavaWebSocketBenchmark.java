package com.example.bench;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.openjdk.jmh.annotations.*;

import java.net.URI;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class JavaWebSocketBenchmark {
    private WebSocketServer server;
    private WebSocketClient client;
    private CountDownLatch latch;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        server = new WebSocketServer(new InetSocketAddress("localhost", 0)) {
            @Override
            public void onOpen(WebSocket conn, ServerHandshake handshake) { }
            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) { }
            @Override
            public void onMessage(WebSocket conn, String message) {
                conn.send(message);
            }
            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
                conn.send(message);
            }
            @Override
            public void onError(WebSocket conn, Exception ex) { }
            @Override
            public void onStart() { }
        };
        server.start();
        int port = server.getPort();
        latch = new CountDownLatch(1);
        client = new WebSocketClient(new URI("ws://localhost:" + port)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                latch.countDown();
            }
            @Override
            public void onMessage(String message) { }
            @Override
            public void onMessage(ByteBuffer bytes) { }
            @Override
            public void onClose(int code, String reason, boolean remote) { }
            @Override
            public void onError(Exception ex) { }
        };
        client.connect();
        latch.await(5, TimeUnit.SECONDS);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        client.close();
        server.stop();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    public void throughput() throws Exception {
        client.send("ping");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    public void latency() throws Exception {
        client.send("ping");
    }
}
