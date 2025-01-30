package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler.PostgresProtocolHandler;

@Data
public class PgCommandMessage {
    private static final Logger LOGGER = LogManager.getLogger(PgCommandMessage.class);
    int requestCode=0;
    private byte msgType;
    private ByteBuf playload;
    private byte[] bytes;
    private int length;
    public PgCommandMessage(byte msgType,ByteBuf buffer, int len){
        this.msgType=msgType;
        bytes= new byte[len];
        int index= buffer.readerIndex();
        buffer.resetReaderIndex();
        buffer.readBytes(bytes);
        int index2= buffer.readerIndex();
        this.length=len;
        this.requestCode=0;
        LOGGER.info("msg added.. "+len+" bytes");
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
    public void cleanUp(){
        this.msgType=0;
        this.requestCode=0;
        bytes=null;
    }
}
