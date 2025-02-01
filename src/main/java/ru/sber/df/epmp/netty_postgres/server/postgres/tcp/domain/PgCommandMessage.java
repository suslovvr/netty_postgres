package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.PgDecoder;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler.PostgresProtocolHandler;

import static io.netty.buffer.Unpooled.*;

@Data
public class PgCommandMessage {
    private static final Logger LOGGER = LogManager.getLogger(PgCommandMessage.class);
    int requestCode=0;
    private byte msgType;
    private ByteBuf playload;
    private byte[] bytes;
    private int length;
    private PgDecoder.State state = PgDecoder.State.STARTUP;

    public PgCommandMessage(byte msgType,ByteBuf buffer, int len){
        this.msgType=msgType;
        int arrayLen = buffer.readableBytes();
        bytes= new byte[arrayLen];
        int index= buffer.readerIndex();
        buffer.resetReaderIndex();
        buffer.readBytes(bytes);
        int index2= buffer.readerIndex();
        this.length=len;
        this.requestCode=0;
        LOGGER.info("msg added.. "+arrayLen+" bytes");
    }
    public PgCommandMessage( int len, int requestCode,ByteBuf buffer){
        this.length=len;
        this.requestCode=requestCode;
        this.msgType=0;
        bytes= new byte[len];
        int index= buffer.readerIndex();
        buffer.resetReaderIndex();
        buffer.readBytes(bytes);
        int index2= buffer.readerIndex();
        LOGGER.info("startup added.. "+len+" bytes");
    }
    public PgCommandMessage( PgDecoder.State state,int len, int requestCode, ByteBuf buffer){
        this.state = state;
        this.length=buffer.readableBytes();
//        this.length=buffer.readableBytes();
        this.requestCode=0;
        this.msgType=0;
        buffer.resetReaderIndex();
//        buffer.readerIndex(0);
        bytes = new byte[length];
//        ByteBuf buf=directBuffer(buffer.writerIndex());
//        ByteBuf buf = buffer(buffer.writerIndex());
//        buf.writerIndex(0);
//        buf.writeInt(len).writeInt(requestCode).writeBytes(buffer);
//        buf.writeBytes(buffer);
//        int index= buffer.readerIndex();
//        buffer.retain();
        buffer.readBytes(bytes);
        int index2= buffer.readerIndex();
        LOGGER.info(state.name()+" added.. "+length+" bytes");
    }
    public void cleanUp(){
        this.msgType=0;
        this.requestCode=0;
        bytes=null;
    }
}
