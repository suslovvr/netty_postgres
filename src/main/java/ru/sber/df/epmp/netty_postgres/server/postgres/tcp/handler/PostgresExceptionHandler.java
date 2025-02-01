package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;

@Slf4j
public class PostgresExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        try {
//            ChannelHandler forwarder = ctx.pipeline().remove("forwarder");
            ctx.pipeline().addLast(new HttpServerCodec());
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED,
                    Unpooled.wrappedBuffer(cause.getMessage().getBytes(StandardCharsets.UTF_8)));
            response.headers()
                    .add(CONTENT_TYPE, TEXT_PLAIN)
                    .add(CONTENT_LENGTH, response.content().readableBytes());
            ChannelFuture f = ctx.channel().writeAndFlush(response);
            f.addListener(ChannelFutureListener.CLOSE);
        }catch (Exception e){
            log.error(e.getMessage(), e);

        }
    }
}
