package ru.sber.df.epmp.netty_postgres.server.clickhouse.http.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ClickhouseHttpRequestForwardingHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (!inboundChannel.isActive()) {
            ClickhouseHttpRequestForwardingInitHandler.closeOnFlush(ctx.channel());
        } else {
            ctx.read();
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClickhouseHttpRequestForwardingInitHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ClickhouseHttpRequestForwardingInitHandler.closeOnFlush(ctx.channel());
    }
}
