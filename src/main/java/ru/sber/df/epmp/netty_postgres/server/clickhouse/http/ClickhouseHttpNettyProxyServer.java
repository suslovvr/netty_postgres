package ru.sber.df.epmp.netty_postgres.server.clickhouse.http;

import ru.sber.df.epmp.netty_postgres.server.clickhouse.http.handler.ClickhouseHttpDatagateModeSwitchingHandler;
import ru.sber.df.epmp.netty_postgres.server.clickhouse.http.handler.ClickhouseHttpRequestForwardingInitHandler;
import ru.sber.df.epmp.netty_postgres.server.clickhouse.http.handler.ClickhouseHttpSqlProcessingHandler;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ClickhouseHttpNettyProxyServer {

    @Value("${proxy.netty.clickhouse.local.port:8123}")
    private int localPort;

    @Value("${proxy.netty.clickhouse.remote.port:8123}")
    private int remotePort;

    @Value("${proxy.netty.clickhouse.remote.host:localhost}")
    private String remoteHost;

    public final static List<String> NATIVE_MODE_PROHIBITED_HANDLERS = List.of("semantic", "switcher", "aggregator");
    public final static String SERVER_MODE_HEADER_NAME = "datagate-mode";
    public final static String NATIVE_SERVER_MODE_HEADER_VALUE = "native";

    private final SemanticNamesConversion semanticNamesConversion;
    private final List<EventLoopGroup> groups = new ArrayList<>();

    public ClickhouseHttpNettyProxyServer(SemanticNamesConversion semanticNamesConversion) {
        this.semanticNamesConversion = semanticNamesConversion;
    }

    public void start() {
        log.info("Start proxying ClickHouse HTTP traffic from *:{} to {}:{}", localPort, remoteHost, remotePort);

        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("clickhouse-nioEventLoopGroup-main"));
            EventLoopGroup workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("clickhouse-nioEventLoopGroup-worker"));
            groups.addAll(List.of(bossGroup, workerGroup));

            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                                    //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO, ByteBufFormat.SIMPLE));
                                    ch.pipeline().addLast("decoder", new HttpRequestDecoder());
                                    ch.pipeline().addLast("aggregator", new HttpObjectAggregator(8192));
                                    ch.pipeline().addLast("switcher", new ClickhouseHttpDatagateModeSwitchingHandler());
                                    ch.pipeline().addLast("semantic", new ClickhouseHttpSqlProcessingHandler(semanticNamesConversion));
                                    ch.pipeline().addLast(new ClickhouseHttpRequestForwardingInitHandler(remoteHost, remotePort));
                                }
                            }
                    )
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(localPort)
                    .sync().channel().closeFuture();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down Clickhouse Netty server");
        groups.forEach(EventExecutorGroup::shutdownGracefully);
    }
}
