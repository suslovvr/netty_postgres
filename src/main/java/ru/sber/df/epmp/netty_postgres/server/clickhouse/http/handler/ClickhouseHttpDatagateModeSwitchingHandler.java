package ru.sber.df.epmp.netty_postgres.server.clickhouse.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static ru.sber.df.epmp.netty_postgres.server.clickhouse.http.ClickhouseHttpNettyProxyServer.NATIVE_MODE_PROHIBITED_HANDLERS;
import static ru.sber.df.epmp.netty_postgres.server.clickhouse.http.ClickhouseHttpNettyProxyServer.NATIVE_SERVER_MODE_HEADER_VALUE;
import static ru.sber.df.epmp.netty_postgres.server.clickhouse.http.ClickhouseHttpNettyProxyServer.SERVER_MODE_HEADER_NAME;

@Slf4j
public class ClickhouseHttpDatagateModeSwitchingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (request != null && request.headers() != null && request.headers().entries() != null) {
            String agent = request.headers().entries().stream()
                    .filter(h -> "user-agent".equalsIgnoreCase(h.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().orElse("unknown-agent");

            log.info("{} New connection established from user-agent '{}'", ctx, agent);

            request.headers().entries().stream()
                    .filter(h -> SERVER_MODE_HEADER_NAME.equalsIgnoreCase(h.getKey())
                            && NATIVE_SERVER_MODE_HEADER_VALUE.equalsIgnoreCase(h.getValue()))
                    .findFirst().ifPresent(any -> {
                        NATIVE_MODE_PROHIBITED_HANDLERS.forEach(handler -> ctx.channel().pipeline().remove(handler));
                        log.info("{} Switching to NATIVE MODE. Skipping SQL-processing for user-agent '{}'", ctx, agent);
                    });
            if (ctx.channel().pipeline().get(ClickhouseHttpDatagateModeSwitchingHandler.class) != null) {
                ctx.channel().pipeline().remove(this);
            }
        }
        ctx.fireChannelRead(request.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }
}
