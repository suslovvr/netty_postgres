package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.PgDecoder;

import java.nio.charset.Charset;

public class PostgresProtocolFrontendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PostgresProtocolFrontendHandler.class);

    private final String remoteHost;
    private final int remotePort;
    private Channel outboundChannel;

    private PgDecoder decoder;
    private PostgresProtocolHandler protocolHandler;

    public PostgresProtocolFrontendHandler(String remoteHost, int remotePort, PostgresProtocolHandler protocolHandler) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.protocolHandler = protocolHandler;
        this.decoder = decoder;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new PostgresTcpProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    inboundChannel.read();
                } else {
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            ByteBuf byteBuffer = protocolHandler.containMsg()?protocolHandler.pullMessage():(ByteBuf)msg;
/*
                if (byteBuffer.hasArray()) {
                    Charset charset = Charset.forName("UTF-8");
                    String newContent = new String(byteBuffer.array(), charset);
                    logger.info("sent content " + newContent);
                }

 */
                logger.info("sent "+ byteBuffer.readableBytes()+" bytes");

            outboundChannel.writeAndFlush(byteBuffer).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
