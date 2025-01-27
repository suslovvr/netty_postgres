package ru.sber.df.epmp.netty_postgres.server.postgres.tcp;

import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.codec.PostgresFrontendMessageDecoderJ;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler.PostgresExceptionHandler;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler.PostgresProtocolHandler;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler.PostgresTcpProxyFrontendHandler;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostgresTcpNettyProxyServer {

    private static final Logger logger = LoggerFactory.getLogger(PostgresTcpNettyProxyServer.class);

    @Value("${proxy.netty.postgres.local.port:8123}")
    private int localPort;

    @Value("${proxy.netty.postgres.remote.port:8123}")
    private int remotePort;

    @Value("${proxy.netty.postgres.remote.host:localhost}")
    private String remoteHost;

    private final SemanticNamesConversion semanticNamesConversion;
    private final List<EventLoopGroup> groups = new ArrayList<>();

    public PostgresTcpNettyProxyServer(SemanticNamesConversion semanticNamesConversion) {
        this.semanticNamesConversion = semanticNamesConversion;
    }

    public void start() {
        logger.info("Start proxying Postgres | GreenPlum TCP traffic from *:{} to {}:{}", localPort, remoteHost, remotePort);

        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("postgres-nioEventLoopGroup-main"));
            EventLoopGroup workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("postgres-nioEventLoopGroup-worker"));
            groups.addAll(List.of(bossGroup, workerGroup));

            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
//                                    ch.pipeline().addLast("logger", new LoggingHandler(LogLevel.INFO));
//                                    ch.pipeline().addLast("decoder", new PostgresFrontendMessageDecoderJ(semanticNamesConversion));
                                    ch.pipeline().addLast("decoder", new PostgresProtocolHandler(semanticNamesConversion));
                                    ch.pipeline().addLast("error_handler", new PostgresExceptionHandler());
                                    ch.pipeline().addLast("forwarder", new PostgresTcpProxyFrontendHandler(remoteHost, remotePort));
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
        logger.info("Shutting down Clickhouse Netty server");
        groups.forEach(EventExecutorGroup::shutdownGracefully);
    }
}
