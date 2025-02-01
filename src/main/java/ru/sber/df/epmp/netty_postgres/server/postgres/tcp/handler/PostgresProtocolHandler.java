package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslContext;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import ru.sber.df.epmp.netty_postgres.server.postgres.action.sql.DescribeResult;
import ru.sber.df.epmp.netty_postgres.server.postgres.action.sql.Session;
import ru.sber.df.epmp.netty_postgres.server.postgres.action.sql.Sessions;
import ru.sber.df.epmp.netty_postgres.server.postgres.auth.*;
import ru.sber.df.epmp.netty_postgres.server.postgres.common.annotations.VisibleForTesting;
import ru.sber.df.epmp.netty_postgres.server.postgres.expression.symbol.Literal;
import ru.sber.df.epmp.netty_postgres.server.postgres.expression.symbol.Symbol;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.Schemas;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.settings.CoordinatorSessionSettings;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.settings.session.SessionSetting;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.settings.session.SessionSettingRegistry;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.http.Netty4HttpServerTransport;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.*;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.types.PGType;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.types.PGTypes;
import ru.sber.df.epmp.netty_postgres.server.postgres.sql.tree.Statement;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain.PgCommandMessage;
import ru.sber.df.epmp.netty_postgres.server.postgres.types.DataType;
import ru.sber.df.epmp.netty_postgres.server.postgres.user.User;
import ru.sber.df.epmp.netty_postgres.server.postgres.user.UserManagerService;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;

import javax.net.ssl.SSLSession;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.sber.df.epmp.netty_postgres.server.postgres.protocols.SSL.getSession;
import static ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.FormatCodes.getFormatCode;
import static ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.PgDecoder.SSL_REQUEST_CODE;
//import static ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.Messages.sendReadyForQuery;

public class PostgresProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(PostgresProtocolHandler.class);

    private SemanticNamesConversion semanticNamesConversion;
    private AtomicBoolean startupMessageSeen = new AtomicBoolean(false);
//-----------------------------------
    private static final String PASSWORD_AUTH_NAME = "password";
    public static String PG_SERVER_VERSION = "14.0";

    private PgDecoder decoder;
    private Properties properties;
    private Sessions sessions;
    private Session session;
    private boolean ignoreTillSync = false;
    private final Function<CoordinatorSessionSettings, AccessControl> getAccessControl;
    private Authentication authService;
    private AuthenticationContext authContext;
    private SessionSettingRegistry sessionSettingRegistry;
//    final MessageHandler handler;
//    private DelayableWriteChannel channel;
    private List<PgCommandMessage> msgChunks= new LinkedList<>();
    //-----------------------------------
    public PostgresProtocolHandler(SemanticNamesConversion semanticNamesConversion) {
        this.semanticNamesConversion = semanticNamesConversion;
        Supplier<SslContext> getSslContext = () -> null;

        this.decoder = new PgDecoder( getSslContext);
        this.sessions = Sessions.getInstance();
        this.session = sessions.newSession(Schemas.DEFAULT_SCHEMA, User.DEFAULT_USER);
        this.getAccessControl = UserManagerService.getInstance()::getAccessControl;
        this.authService = AlwaysOKAuthentication.getInstance();
        this.sessionSettingRegistry = SessionSettingRegistry.getInstance();
//        this.handler = new MessageHandler();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ByteBuf bufMsg = (ByteBuf)msg;
        ByteBuf buffer = bufMsg.copy();
//        DelayableWriteChannel channel= new DelayableWriteChannel(ctx.channel());
        Channel channel= ctx.channel();
        assert channel != null : "Channel must be initialized";
        try {
            dispatchState(buffer, channel);
        } catch (Throwable t) {
            ignoreTillSync = true;
            try {
                AccessControl accessControl = session == null
                        ? AccessControl.DISABLED
                        : getAccessControl.apply(session.sessionSettings());
//                Messages.sendErrorResponse(channel, accessControl, t);
                LOGGER.error("Error trying to send error to client: {}", t);
            } catch (Throwable ti) {
                LOGGER.error("Error trying to send error to client: {}", t, ti);
            }
        }
        LOGGER.info("CurrentBuf().readableBytes() = "+decoder.getCurrentBuf().readableBytes());
        if(buffer.writerIndex()==decoder.getCurrentBuf().writerIndex() && buffer.readableBytes() == 0 ){
//            decoder.getCurrentBuf().resetReaderIndex();
            ctx.fireChannelRead(buffer);
        } else {
            if (decoder.getCurrentBuf().readableBytes() == 0) {
                ctx.fireChannelRead(msg);
            }
        }
    }


    private void dispatchState(ByteBuf buffer, Channel channel) {
        switch (decoder.state()) {
            case STARTUP:
                LOGGER.info("decoder.state="+decoder.state()+((decoder.getRequestCode() == SSL_REQUEST_CODE)?" SSLRequest":""));
                return;
            case STARTUP_PARAMETERS:
                LOGGER.info("decoder.state="+decoder.state());
                pushStartParamsChunk(  decoder.state(), decoder.payloadLength(), decoder.getRequestCode(), buffer);
//                handleStartupBody(buffer, channel);
                decoder.startupDone();
                return;
            case CANCEL:
                LOGGER.info("decoder.state="+decoder.state());
                handleCancelRequestBody(buffer, channel);
                return;

            case MSG:
                LOGGER.info("msg={} msgLength={} readableBytes={}", ((char) decoder.msgType()), decoder.payloadLength(), buffer.readableBytes());
                pushMsgChunk(decoder.msgType(), decoder.payloadLength(), buffer);
                if (ignoreTillSync && decoder.msgType() != 'S') {
                    buffer.skipBytes(decoder.payloadLength());
                    return;
                }
                if(!decoder.isSslProxyMode()) {
                    dispatchMessage(buffer, channel);
                }
                return;
            default:
                throw new IllegalStateException("Illegal state: " + decoder.state());
        }
    }

    private void dispatchMessage(ByteBuf buffer, Channel channel) {
        switch (decoder.msgType()) {
            case 'Q': // Query (simple)
                handleSimpleQuery(buffer, channel);
                return;
            case 'P':
                handleParseMessage(buffer, channel);
                return;
            case 'p':
                handlePassword(buffer, channel);
                return;
            case 'B':
                handleBindMessage(buffer, channel);
                return;
            case 'D':
                handleDescribeMessage(buffer, channel);
                return;
            case 'E':
                handleExecute(buffer, channel);
                return;
            case 'H':
                handleFlush(channel);
                return;
            case 'S':
                handleSync(channel);
                return;
            case 'C':
                handleClose(buffer, channel);
                return;
            case 'X': // Terminate (called when jdbc connection is closed)
                closeSession();
                channel.close();
                return;
            default:
                Messages.sendErrorResponse(
                        channel,
                        session == null
                                ? AccessControl.DISABLED
                                : getAccessControl.apply(session.sessionSettings()),
                        new UnsupportedOperationException("Unsupported messageType: " + decoder.msgType()));
        }
    }

    /**
     * Parse Message
     * header:
     * | 'P' | int32 len
     * <p>
     * body:
     * | string statementName | string query | int16 numParamTypes |
     * foreach param:
     * | int32 type_oid (zero = unspecified)
     */
    private void handleParseMessage(ByteBuf buffer, final Channel channel) {
        String statementName = readCString(buffer);
        final String query = readCString(buffer);
        short numParams = buffer.readShort();
        List<DataType<?>> paramTypes = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; i++) {
            int oid = buffer.readInt();
            DataType<?> dataType = PGTypes.fromOID(oid);
            if (dataType == null) {
                throw new IllegalArgumentException(
                        String.format(Locale.ENGLISH, "Can't map PGType with oid=%d to Crate type", oid));
            }
            paramTypes.add(dataType);
        }
        LOGGER.info("method=handleParseMessage stmtName={} query={} paramTypes={}", statementName, query, paramTypes);
//        session.parse(statementName, query, paramTypes);
//        Messages.sendParseComplete(channel);
    }

    private void handlePassword(ByteBuf buffer, final Channel channel) {
        char[] passwd = readCharArray(buffer);
        if (passwd != null) {
            authContext.setSecurePassword(passwd);
        }
        finishAuthentication(channel);
    }

    private void finishAuthentication(Channel channel) {
        assert authContext != null : "finishAuthentication() requires an authContext instance";
        try {
            User authenticatedUser = authContext.authenticate();
            String database = properties.getProperty("database");
            session = sessions.newSession(database, authenticatedUser);
            String options = properties.getProperty("options");
            if (options != null) {
                applyOptions(options);
            }
            /*
            Messages.sendAuthenticationOK(channel)
                    .addListener(f -> sendParams(channel, session.sessionSettings()))
                    .addListener(f -> Messages.sendKeyData(channel, session.id(), session.secret()))
                    .addListener(f -> {
                        sendReadyForQuery(channel, TransactionState.IDLE);
                        if (properties.containsKey("CrateDBTransport")) {
                            switchToTransportProtocol(channel);
                        }
                    });

             */
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
//            Messages.sendAuthenticationError(channel, e.getMessage());
        } finally {
            authContext.close();
            authContext = null;
        }
    }

    private void applyOptions(String options) {
        OptionParser parser = new OptionParser();
        var optionC = parser.accepts("c").withRequiredArg().ofType(String.class);
        OptionSet parseResult = parser.parse(options.split(" "));
        List<String> parsedOptions = parseResult.valuesOf(optionC);
        for (String parsedOption : parsedOptions) {
            String[] parts = parsedOption.split("=");
            if (parts.length != 2) {
                continue;
            }
            String key = parts[0].trim();
            String value = parts[1].trim();
            SessionSetting<?> sessionSetting = sessionSettingRegistry.settings().get(key);
            if (sessionSetting == null) {
                continue;
            }
            sessionSetting.apply(session.sessionSettings(), List.of(Literal.of(value)), symbol -> {
                if (symbol instanceof Literal<?> literal) {
                    return literal.value();
                }
                throw new IllegalStateException("Unexpected symbol: " + symbol);
            });
        }
    }


    /**
     * Bind Message
     * Header:
     * | 'B' | int32 len
     * <p>
     * Body:
     * <pre>
     * | string portalName | string statementName
     * | int16 numFormatCodes
     *      foreach
     *      | int16 formatCode
     * | int16 numParams
     *      foreach
     *      | int32 valueLength
     *      | byteN value
     * | int16 numResultColumnFormatCodes
     *      foreach
     *      | int16 formatCode
     * </pre>
     */
    private void handleBindMessage(ByteBuf buffer, Channel channel) {
        String portalName = readCString(buffer);
        String statementName = readCString(buffer);

        FormatCodes.FormatCode[] formatCodes = FormatCodes.fromBuffer(buffer);

        short numParams = buffer.readShort();
        List<Object> params = createList(numParams);
        for (int i = 0; i < numParams; i++) {
            int valueLength = buffer.readInt();
            if (valueLength == -1) {
                params.add(null);
            } else {
                DataType paramType = session.getParamType(statementName, i);
                PGType pgType = PGTypes.get(paramType);
                FormatCodes.FormatCode formatCode = getFormatCode(formatCodes, i);
                switch (formatCode) {
                    case TEXT:
                        params.add(pgType.readTextValue(buffer, valueLength));
                        break;

                    case BINARY:
                        params.add(pgType.readBinaryValue(buffer, valueLength));
                        break;

                    default:
                        /*

                        Messages.sendErrorResponse(
                                channel,
                                getAccessControl.apply(session.sessionSettings()),
                                new UnsupportedOperationException(String.format(
                                        Locale.ENGLISH,
                                        "Unsupported format code '%d' for param '%s'",
                                        formatCode.ordinal(),
                                        paramType.getName())
                                )
                        );

                         */
                        throw new UnsupportedOperationException(String.format(
                                Locale.ENGLISH,
                                "Unsupported format code '%d' for param '%s'",
                                formatCode.ordinal(),
                                paramType.getName()));
                }
            }
        }
/*
        FormatCodes.FormatCode[] resultFormatCodes = FormatCodes.fromBuffer(buffer);
        session.bind(portalName, statementName, params, resultFormatCodes);
        Messages.sendBindComplete(channel);

 */
    }

    private <T> List<T> createList(short size) {
        return size == 0 ? Collections.<T>emptyList() : new ArrayList<T>(size);
    }


    /**
     * Describe Message
     * Header:
     * | 'D' | int32 len
     * <p>
     * Body:
     * | 'S' = prepared statement or 'P' = portal
     * | string nameOfPortalOrStatement
     */
    private void handleDescribeMessage(ByteBuf buffer, Channel channel) {
        byte type = buffer.readByte();
        String portalOrStatement = readCString(buffer);
        DescribeResult describeResult = session.describe((char) type, portalOrStatement);
        Collection<Symbol> fields = describeResult.getFields();
        if (type == 'S') {
//            Messages.sendParameterDescription(channel, describeResult.getParameters());
        }
        if (fields == null) {
//            Messages.sendNoData(channel);
        } else {
            var resultFormatCodes = type == 'P' ? session.getResultFormatCodes(portalOrStatement) : null;
//            Messages.sendRowDescription(channel, fields, resultFormatCodes, describeResult.relation());
        }
    }

    /**
     * Execute Message
     * Header:
     * | 'E' | int32 len
     * <p>
     * Body:
     * | string portalName
     * | int32 maxRows (0 = unlimited)
     */
    private void handleExecute(ByteBuf buffer, Channel channel) {
        /*
        String portalName = readCString(buffer);
        int maxRows = buffer.readInt();
        String query = session.getQuery(portalName);
        if (query.isEmpty()) {
            // remove portal so that it doesn't stick around and no attempt to batch it with follow up statement is made
            session.close((byte) 'P', portalName);
            Messages.sendEmptyQueryResponse(channel);
            return;
        }
        List<? extends DataType> outputTypes = session.getOutputTypes(portalName);

        // .execute is going async and may execute the query in another thread-pool.
        // The results are later sent to the clients via the `ResultReceiver` created
        // above, The `channel.write` calls - which the `ResultReceiver` makes - may
        // happen in a thread which is *not* a netty thread.
        // If that is the case, netty schedules the writes instead of running them
        // immediately. A consequence of that is that *this* thread can continue
        // processing other messages from the client, and if this thread then sends messages to the
        // client, these are sent immediately, overtaking the result messages of the
        // execute that is triggered here.
        //
        // This would lead to out-of-order messages. For example, we could send a
        // `parseComplete` before the `commandComplete` of the previous statement has
        // been transmitted.
        //
        // To ensure clients receive messages in the correct order we delay all writes
        // The "finish" logic of the ResultReceivers writes out all pending writes/unblocks the channel

        DelayableWriteChannel.DelayedWrites delayedWrites = channel.delayWrites();
        ResultReceiver<?> resultReceiver;
        if (outputTypes == null) {
            // this is a DML query
            maxRows = 0;
            resultReceiver = new RowCountReceiver(
                    query,
                    channel,
                    delayedWrites,
                    getAccessControl.apply(session.sessionSettings())
            );
        } else {
            // query with resultSet
            resultReceiver = new ResultSetReceiver(
                    query,
                    channel,
                    delayedWrites,
                    session.transactionState(),
                    getAccessControl.apply(session.sessionSettings()),
                    Lists2.map(outputTypes, PGTypes::get),
                    session.getResultFormatCodes(portalName)
            );
        }
        session.execute(portalName, maxRows, resultReceiver);

         */
    }

    private void handleSync(Channel channel) {
        /*
        if (ignoreTillSync) {
            ignoreTillSync = false;
            // If an error happens all sub-sequent messages can be ignored until the client sends a sync message
            // We need to discard any deferred executions to make sure that the *next* sync isn't executing
            // something we had previously deferred.
            // E.g. JDBC client:
            //  1) `addBatch` -> success (results in bind+execute -> we defer execution)
            //  2) `addBatch` -> failure (ignoreTillSync=true; we stop after bind, no execute, etc..)
            //  3) `sync`     -> sendReadyForQuery (this if branch)
            //  4) p, b, e    -> We've a new query deferred.
            //  5) `sync`     -> We must execute the query from 4, but not 1)
            session.resetDeferredExecutions();
            channel.writePendingMessages();
            sendReadyForQuery(channel, TransactionState.FAILED_TRANSACTION);
            return;
        }
        try {
            ReadyForQueryCallback readyForQueryCallback = new ReadyForQueryCallback(channel, session.transactionState());
            session.sync().whenComplete(readyForQueryCallback);
        } catch (Throwable t) {
            channel.discardDelayedWrites();
            Messages.sendErrorResponse(channel, getAccessControl.apply(session.sessionSettings()), t);
            sendReadyForQuery(channel, TransactionState.FAILED_TRANSACTION);
        }

         */
    }

    /**
     * | 'C' | int32 len | byte portalOrStatement | string portalOrStatementName |
     */
    private void handleClose(ByteBuf buffer, Channel channel) {
        /*
        byte b = buffer.readByte();
        String portalOrStatementName = readCString(buffer);
        session.close(b, portalOrStatementName);
        Messages.sendCloseComplete(channel);

         */
    }

    @VisibleForTesting
    void handleSimpleQuery(ByteBuf buffer, final Channel channel) {
        String queryString = readCString(buffer);
        assert queryString != null : "query must not be nulL";

        if (queryString.isEmpty() || ";".equals(queryString)) {
//            Messages.sendEmptyQueryResponse(channel);
//            sendReadyForQuery(channel, TransactionState.IDLE);
            return;
        }
        LOGGER.info("method=handleSimpleQuery query={}", queryString);
        /*
        List<Statement> statements = select1();

        try {
            statements = SqlParser.createStatementsForSimpleQuery(
                    queryString,
                    str -> PgArrayParser.parse(
                            str,
                            bytes -> new String(bytes, StandardCharsets.UTF_8)
                    )
                );
        } catch (Exception ex) {
            Messages.sendErrorResponse(channel, getAccessControl.apply(session.sessionSettings()), ex);
            sendReadyForQuery(channel, TransactionState.IDLE);
            return;
        }


        CompletableFuture<?> composedFuture = CompletableFuture.completedFuture(null);
        for (var statement : statements) {
            composedFuture = composedFuture.thenCompose(result -> handleSingleQuery(statement, queryString, channel));
        }
        composedFuture.whenComplete(new ReadyForQueryCallback(channel, TransactionState.IDLE));

         */
    }

    private void handleStartupBody(ByteBuf buffer, Channel channel) {
        properties = readStartupMessage(buffer);
//        initAuthentication(channel);
    }

    private void initAuthentication(Channel channel) {
        String userName = properties.getProperty("user");
        InetAddress address = Netty4HttpServerTransport.getRemoteAddress(channel);

        SSLSession sslSession = getSession(channel);
        ConnectionProperties connProperties = new ConnectionProperties(address, Protocol.POSTGRES, sslSession);

        AuthenticationMethod authMethod = authService.resolveAuthenticationType(userName, connProperties);
        if (authMethod == null) {
            String errorMessage = String.format(
                    Locale.ENGLISH,
                    "No valid auth.host_based entry found for host \"%s\", user \"%s\". Did you enable TLS in your client?",
                    address.getHostAddress(), userName
            );
//            Messages.sendAuthenticationError(channel, errorMessage);
        } else {
            authContext = new AuthenticationContext(authMethod, connProperties, userName, LOGGER);
            if (PASSWORD_AUTH_NAME.equals(authMethod.name())) {
//                Messages.sendAuthenticationCleartextPassword(channel);
                return;
            }
            finishAuthentication(channel);
        }
    }

    private void handleSingleQuery(Statement statement, String query, Channel channel) {

        AccessControl accessControl = getAccessControl.apply(session.sessionSettings());
        try {
            session.analyze("", statement, Collections.emptyList(), query);
            session.bind("", "", Collections.emptyList(), null);
            /*
            DescribeResult describeResult = session.describe('P', "");
            List<Symbol> fields = describeResult.getFields();

            if (fields == null) {
                DelayableWriteChannel.DelayedWrites delayedWrites = channel.delayWrites();
                RowCountReceiver rowCountReceiver = new RowCountReceiver(
                        query,
                        channel,
                        delayedWrites,
                        accessControl
                );
                session.execute("", 0, rowCountReceiver);
            } else {
                Messages.sendRowDescription(channel, fields, null, describeResult.relation());
                DelayableWriteChannel.DelayedWrites delayedWrites = channel.delayWrites();
                ResultSetReceiver resultSetReceiver = new ResultSetReceiver(
                        query,
                        channel,
                        delayedWrites,
                        TransactionState.IDLE,
                        accessControl,
                        Lists2.map(fields, x -> PGTypes.get(x.valueType())),
                        null
                );
                session.execute("", 0, resultSetReceiver);
            }
            return session.sync();

             */
        } catch (Throwable t) {
//            Messages.sendErrorResponse(channel, accessControl, t);
            LOGGER.error(t);
        }
    }


    private Properties readStartupMessage(ByteBuf buffer) {
        Properties properties = new Properties();
        while (true) {
            String key = readCString(buffer);
            if (key == null) {
                break;
            }
            String value = readCString(buffer);
            LOGGER.trace("payload: key={} value={}", key, value);
            if (!"".equals(key) && !"".equals(value)) {
                properties.setProperty(key, value);
            }
        }
        return properties;
    }

    private void handleCancelRequestBody(ByteBuf buffer, Channel channel) {
        var keyData = KeyData.of(buffer);

        sessions.cancel(keyData);

        // Cancel request is sent by the client over a new connection.
        // This closes the new connection, not the one running the query.
//        handler.closeSession();
        channel.close();
    }

    @Nullable
    static String readCString(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.bytesBefore((byte) 0) + 1];
        if (bytes.length == 0) {
            return null;
        }
        buffer.readBytes(bytes);
        return new String(bytes, 0, bytes.length - 1, StandardCharsets.UTF_8);
    }

    @Nullable
    private static char[] readCharArray(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.bytesBefore((byte) 0) + 1];
        if (bytes.length == 0) {
            return null;
        }
        buffer.readBytes(bytes);
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).array();
    }

    /**
     * Flush Message
     * | 'H' | int32 len
     * <p>
     * Flush forces the backend to deliver any data pending in it's output buffers.
     */
    private void handleFlush(Channel channel) {
        try {
            // If we have deferred any executions we need to trigger a sync now because the client is expecting data
            // (That we've been holding back, as we don't eager react to `execute` requests. (We do that to optimize batch inserts))
            // The sync will also trigger a flush eventually if there are deferred executions.
            if (session.hasDeferredExecutions()) {
                session.flush();
            } else {
                channel.flush();
            }
        } catch (Throwable t) {
            LOGGER.error(t);
//            Messages.sendErrorResponse(channel, getAccessControl.apply(session.sessionSettings()), t);
        }
    }

    private void closeSession() {
        if (session != null) {
            session.close();
            session = null;
        }
    }
    private static class ReadyForQueryCallback implements BiConsumer<Object, Throwable> {
        private final Channel channel;
        private final TransactionState transactionState;

        private ReadyForQueryCallback(Channel channel, TransactionState transactionState) {
            this.channel = channel;
            this.transactionState = transactionState;
        }

        @Override
        public void accept(Object result, Throwable t) {
//            sendReadyForQuery(channel, transactionState);
            LOGGER.info("called accept..");
        }
    }
    private void switchToTransportProtocol(Channel channel) {
        var pipeline = channel.pipeline();
        pipeline.remove("frame-decoder");
        pipeline.remove("handler");

        // SSL is already done via PostgreSQL handshake/auth
//        addTransportHandler.accept(pipeline);
    }
    private void sendParams(Channel channel, CoordinatorSessionSettings sessionSettings) {
/*
        Messages.sendParameterStatus(channel, "crate_version", Version.CURRENT.externalNumber());
        Messages.sendParameterStatus(channel, "server_version", PG_SERVER_VERSION);
        Messages.sendParameterStatus(channel, "server_encoding", "UTF8");
        Messages.sendParameterStatus(channel, "client_encoding", "UTF8");
        Messages.sendParameterStatus(channel, "datestyle", sessionSettings.dateStyle());
        Messages.sendParameterStatus(channel, "TimeZone", "UTC");
        Messages.sendParameterStatus(channel, "integer_datetimes", "on");

 */
    }
    /*
    private class MessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        }

        @Override
        public boolean acceptInboundMessage(Object msg) throws Exception {
            return true;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
            assert channel != null : "Channel must be initialized";
            try {
                dispatchState(buffer, channel);
            } catch (Throwable t) {
                ignoreTillSync = true;
                try {
                    AccessControl accessControl = session == null
                            ? AccessControl.DISABLED
                            : getAccessControl.apply(session.sessionSettings());
                    Messages.sendErrorResponse(channel, accessControl, t);
                } catch (Throwable ti) {
                    LOGGER.error("Error trying to send error to client: {}", t, ti);
                }
            }
        }

        private void dispatchState(ByteBuf buffer, DelayableWriteChannel channel) {
            switch (decoder.state()) {
                case STARTUP_PARAMETERS:
                    handleStartupBody(buffer, channel);
                    decoder.startupDone();
                    return;

                case CANCEL:
                    handleCancelRequestBody(buffer, channel);
                    return;

                case MSG:
                    LOGGER.trace("msg={} msgLength={} readableBytes={}", ((char) decoder.msgType()), decoder.payloadLength(), buffer.readableBytes());

                    if (ignoreTillSync && decoder.msgType() != 'S') {
                        buffer.skipBytes(decoder.payloadLength());
                        return;
                    }
                    dispatchMessage(buffer, channel);
                    return;
                default:
                    throw new IllegalStateException("Illegal state: " + decoder.state());
            }
        }

        private void dispatchMessage(ByteBuf buffer, Channel channel) {
            switch (decoder.msgType()) {
                case 'Q': // Query (simple)
                    handleSimpleQuery(buffer, channel);
                    return;
                case 'P':
                    handleParseMessage(buffer, channel);
                    return;
                case 'p':
                    handlePassword(buffer, channel);
                    return;
                case 'B':
                    handleBindMessage(buffer, channel);
                    return;
                case 'D':
                    handleDescribeMessage(buffer, channel);
                    return;
                case 'E':
                    handleExecute(buffer, channel);
                    return;
                case 'H':
                    handleFlush(channel);
                    return;
                case 'S':
                    handleSync(channel);
                    return;
                case 'C':
                    handleClose(buffer, channel);
                    return;
                case 'X': // Terminate (called when jdbc connection is closed)
                    closeSession();
                    channel.close();
                    return;
                default:
                    Messages.sendErrorResponse(
                            channel,
                            session == null
                                    ? AccessControl.DISABLED
                                    : getAccessControl.apply(session.sessionSettings()),
                            new UnsupportedOperationException("Unsupported messageType: " + decoder.msgType()));
            }
        }

        private void closeSession() {
            if (session != null) {
                session.close();
                session = null;
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof SocketException && cause.getMessage().equals("Connection reset")) {
                LOGGER.info("Connection reset. Client likely terminated connection");
                closeSession();
            } else {
                LOGGER.error("Uncaught exception: ", cause);
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            LOGGER.trace("channelDisconnected");
            channel = null;
            closeSession();
            super.channelUnregistered(ctx);
        }
    }
*/

    //----------------------------------
    private List<Statement> select1(){
        List<Statement> retVal= new LinkedList<>();
        Statement stmt = new Statement() {
            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public String toString() {
                return "select 1";
            }
        };
        retVal.add(stmt);
        return retVal;
    }

    public PgDecoder getDecoder() {
        return decoder;
    }

    private void pushMsgChunk(byte msgType, int len,ByteBuf buffer){
        PgCommandMessage msgChunk = new PgCommandMessage( msgType, buffer,  len);
        msgChunks.add(msgChunk);
    }
    private void pushStartUpChunk( int len, int requestCode, ByteBuf buffer){
        PgCommandMessage msgChunk = new PgCommandMessage(  len, requestCode, buffer);
        msgChunks.add(msgChunk);
    }
    private void pushStartParamsChunk( PgDecoder.State state, int len, int requestCode, ByteBuf buffer){
        PgCommandMessage msgChunk = new PgCommandMessage(  state, len,  requestCode, buffer);
        msgChunks.add(msgChunk);
    }

    public int getMessageLen(){
        int retVal=0;
        for(PgCommandMessage chunk: msgChunks){
            if(chunk.getState() == PgDecoder.State.STARTUP_PARAMETERS){
                retVal += chunk.getLength();
            }else{
            if(chunk.getRequestCode()==0) {
                if(decoder.isSslProxyMode()){
                    retVal += chunk.getBytes().length + 5;
                }else {
                    retVal += chunk.getLength() + 5;
                }
            }else{
                retVal += chunk.getLength() + 8;
            }
        }}
        return retVal;
    }

    public ByteBuf pullMessage(){
        ByteBuf buf=null;
        if(msgChunks.size()==1 ){
            if(msgChunks.get(0).getState() == PgDecoder.State.STARTUP_PARAMETERS){
                buf = Unpooled.copiedBuffer(msgChunks.get(0).getBytes());
            }
        }
        if(buf == null){
        buf=Unpooled.buffer(getMessageLen());
        for(PgCommandMessage chunk: msgChunks){
            if(chunk.getState() == PgDecoder.State.STARTUP_PARAMETERS){
                buf.writeBytes(chunk.getBytes());
            }else {
                if (chunk.getRequestCode() == 0) {
                    buf.writeByte(chunk.getMsgType());
                    buf.writeInt(chunk.getLength() + 4);
                    if(chunk.getBytes().length>0) {
                        buf.writeBytes(chunk.getBytes());
                    }
                } else {
                    buf.writeInt(chunk.getLength() + 8);
                    buf.writeInt(chunk.getRequestCode());
                    buf.writeBytes(chunk.getBytes());
                }
            }
        }}
        LOGGER.info("pullMessage from "+msgChunks.size()+" chanks");
        msgChunks.clear();
        return buf;
    }

    public boolean containMsg(){
        return msgChunks.size()>0;
    }
}
