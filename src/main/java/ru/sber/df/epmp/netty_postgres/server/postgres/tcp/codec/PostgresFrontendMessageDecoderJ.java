package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.codec;

import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain.FrontendBootstrapMessage;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain.FrontendMessageTypeJ;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.utils.ByteBufUtilsJ;
import ru.sber.df.epmp.netty_postgres.utils.sql.SemanticConvertor;
import ru.sber.df.epmp.netty_postgres.utils.sql.SqlCommandUtil;
import ru.sber.df.epmp.netty_postgres.utils.sql.SqlHints;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class PostgresFrontendMessageDecoderJ extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(PostgresFrontendMessageDecoderJ.class);

    private final SemanticNamesConversion semanticNamesConversion;
    private AtomicBoolean startupMessageSeen = new AtomicBoolean(false);

    public PostgresFrontendMessageDecoderJ(SemanticNamesConversion semanticNamesConversion) {
        this.semanticNamesConversion = semanticNamesConversion;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        logger.info("PostgresFrontendMessageDecoderJ decode");
        if (!(msg instanceof ByteBuf buf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        var bufCopy = buf.copy();

        if (bufCopy.readableBytes() < 4) {
            ctx.fireChannelRead(msg);
            return;
        }

        try {
            if (!startupMessageSeen.get()) {
                final int _length = bufCopy.readInt();
                final int protocol = bufCopy.readInt();
                switch (protocol) {
                    case FrontendBootstrapMessage.SSLRequest.ID -> logger.info("SSLRequest");
                    case FrontendBootstrapMessage.Startup.ID -> startupMessageSeen.set(true);
                    case FrontendBootstrapMessage.CancelRequest.ID -> logger.debug("CancelRequest");
                    default -> logger.debug("Unknown message type: {}", protocol);
                }
            } else {
                final char messageId = (char) bufCopy.readByte();
                final int _length = bufCopy.readInt();
                logger.debug("Message id: {}", "" + messageId);

                final FrontendMessageTypeJ messageType = FrontendMessageTypeJ.fromId(messageId);
                logger.debug("Message type: {}", messageType);

                if (messageType == FrontendMessageTypeJ.Query) {
                    String query = ByteBufUtilsJ.readCString(bufCopy);
                    logger.debug("Query: {}", query);

                    if (!SqlHints.isDatagateRawModeEnabled(query)) {
                        if (SqlCommandUtil.containsCommand(query)) {
                            logger.info("Command FOUND.. make a query substitution");
                            bufCopy.clear();
                            new PostgresWireMessageBuilder(messageType.getId(), bufCopy)
                                    .writeString(SqlCommandUtil.findAndReplaceCommandIfNeeded(query))
                                    .writeByte((byte) 0)
                                    .finalizeMessage();

                            ctx.fireChannelRead(bufCopy);
                            return;
                        } else {
                            logger.info("SQL SCHEMA|TABLE|COLUMN substitution..");
                            SemanticConvertor convertor = new SemanticConvertor(query, semanticNamesConversion);
                            String rewrittenSql = convertor.rewrite(convertor.getStatement()).toString();
                            bufCopy.clear();
                            new PostgresWireMessageBuilder(messageType.getId(), bufCopy)
                                    .writeString(rewrittenSql)
                                    .writeByte((byte) 0)
                                    .finalizeMessage();

                            ctx.fireChannelRead(bufCopy);
                            return;
                        }
                    } else {
                        logger.debug("Query is empty");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception happens..", e);
        }

        ctx.fireChannelRead(msg);
    }
}
