package com.example.bench;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import org.openjdk.jmh.annotations.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class NettyWebSocketBenchmark {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private EventLoopGroup clientGroup;
    private Channel clientChannel;
    private int port;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(65536));
                        p.addLast(new WebSocketServerProtocolHandler("/ws"));
                    }
                });
        serverChannel = serverBootstrap.bind(0).sync().channel();
        port = ((InetSocketAddress) serverChannel.localAddress()).getPort();

        clientGroup = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(65536));
                        p.addLast(new WebSocketClientProtocolHandler(
                                URI.create("ws://localhost:" + port + "/ws"),
                                WebSocketVersion.V13,
                                null,
                                false,
                                null,
                                Integer.MAX_VALUE));
                    }
                });
        clientChannel = clientBootstrap.connect("localhost", port).sync().channel();
    }

    @TearDown(Level.Trial)
    public void teardown() throws Exception {
        if (clientChannel != null) {
            clientChannel.close().sync();
        }
        if (serverChannel != null) {
            serverChannel.close().sync();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (clientGroup != null) {
            clientGroup.shutdownGracefully();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void throughput() {
        clientChannel.writeAndFlush(new TextWebSocketFrame("ping"));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void latency() throws Exception {
        ChannelPromise promise = clientChannel.newPromise();
        clientChannel.writeAndFlush(new TextWebSocketFrame("ping")).addListener((ChannelFutureListener) future -> promise.setSuccess());
        promise.sync();
    }
}
