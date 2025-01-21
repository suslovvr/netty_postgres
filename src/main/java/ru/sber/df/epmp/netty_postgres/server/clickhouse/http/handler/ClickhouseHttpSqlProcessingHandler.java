package ru.sber.df.epmp.netty_postgres.server.clickhouse.http.handler;

import ru.sber.df.epmp.netty_postgres.utils.Utils;
import ru.sber.df.epmp.netty_postgres.utils.sql.SemanticConvertor;
import ru.sber.df.epmp.netty_postgres.utils.sql.SqlCommandUtil;
import ru.sber.df.epmp.netty_postgres.utils.sql.SqlHints;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticTableConversion;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class ClickhouseHttpSqlProcessingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final SemanticNamesConversion semanticNamesConversion;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String query = request.content().toString(Charset.defaultCharset());
        log.info("FullHttpRequest: {}", request);
        log.info("Query: {}", query);
        request.content().resetReaderIndex();

        try {
            if (!SqlHints.isDatagateRawModeEnabled(query)) {
                if (SqlCommandUtil.containsCommand(query)) {
                    log.info("Command FOUND.. make a query substitution");
                    request.content().clear();
                    request.content().writeCharSequence(SqlCommandUtil.findAndReplaceCommandIfNeeded(query), Charset.defaultCharset());
                    request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                } else {
                    String systemDatabasesQuery = "select name as TABLE_SCHEM, null as TABLE_CATALOG from system.databases where name like";
                    String systemTablesQuery = "SELECT name as TABLE_NAME, engine as TABLE_TYPE, database as TABLE_SCHEM,comment as REMARKS, * FROM system.tables";
                    String systemColumnsQuery = "select NULL as TABLE_CAT, database as TABLE_SCHEM, table as TABLE_NAME, name as COLUMN_NAME";
                    query = query.replaceAll("\\s+", " ").trim();

                    if (query.toUpperCase().contains(systemDatabasesQuery.toUpperCase())) {
                        List<SemanticTableConversion> list = semanticNamesConversion.getConversions();

                        String rewrittenSql = "SELECT name as TABLE_SCHEM, null as TABLE_CATALOG FROM system.databases WHERE 1 = 0";

                        if (!list.isEmpty()) {
                            log.info("FOUND SemanticSchemas: {}", list);
                            String template = """
                                    select '%s' as TABLE_SCHEM, null as TABLE_CATALOG
                                    FROM system.databases
                                    where name = '%s'
                                    """;
                            rewrittenSql = list.stream().map(c ->
                                            String.format(template,
                                                    c.getSchemaConversion().semanticName(),
                                                    c.getSchemaConversion().realName()))
                                    .distinct()
                                    .collect(Collectors.joining(" union all "));
                        } else {
                            log.warn("Semantic schema not found!");
                        }

                        log.info("Substitution query: {}", rewrittenSql);
                        request.content().clear();
                        request.content().writeCharSequence(rewrittenSql, Charset.defaultCharset());
                        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                        ctx.fireChannelRead(request.retain());
                        return;
                    }

                    if (query.toUpperCase().contains(systemTablesQuery.toUpperCase())) {
                        String requestedSchema = Utils.removeQuotation(query.split(" WHERE ")[1]
                                .replaceAll(";", "")
                                .split("=")[1].trim());

                        List<SemanticTableConversion> semanticTables = semanticNamesConversion.getConversions().stream()
                                .filter(c -> c.getSchemaConversion().semanticName().equalsIgnoreCase(requestedSchema))
                                .collect(Collectors.toList());

                        String rewrittenSql = "SELECT name as TABLE_NAME, engine as TABLE_TYPE, database as TABLE_SCHEM FROM system.tables WHERE 1 = 0";
                        if (!semanticTables.isEmpty()) {
                            log.info("FOUND SemanticTables: {}", semanticTables);
                            String template = """
                                    SELECT '%s' as TABLE_NAME, engine as TABLE_TYPE, '%s' as TABLE_SCHEM
                                    FROM system.tables
                                    WHERE name = '%s' AND database = '%s'
                                    """;
                            rewrittenSql = semanticTables.stream().map(c -> String.format(template,
                                    c.getTableConversion().semanticName(),
                                    c.getSchemaConversion().semanticName(),
                                    c.getTableConversion().realName(),
                                    c.getSchemaConversion().realName())).distinct().collect(Collectors.joining(" union all "));
                        } else {
                            log.warn("Semantic schema not found!");
                        }
                        log.info("Substitution query: {}", rewrittenSql);
                        request.content().clear();
                        request.content().writeCharSequence(rewrittenSql, Charset.defaultCharset());
                        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                        ctx.fireChannelRead(request.retain());
                        return;
                    }

                    if (query.toUpperCase().contains(systemColumnsQuery.toUpperCase()) & query.contains(" system.columns")) {
                        String[] parts = Utils.removeQuotation(query.toUpperCase().split(" WHERE ")[1]).split(" AND ");
                        String requestedSchema = parts[0].split(" LIKE ")[1].trim();
                        String requestedTable = parts[1].split(" LIKE ")[1].trim();

                        Optional<SemanticTableConversion> optionalConversion = semanticNamesConversion.getConversions().stream()
                                .filter(c -> c.getSchemaConversion().semanticName().equalsIgnoreCase(requestedSchema))
                                .filter(c -> c.getTableConversion().semanticName().equalsIgnoreCase(requestedTable))
                                .findFirst();

                        String rewrittenSql = """
                                                select
                                                    NULL as TABLE_CAT,
                                                    database as TABLE_SCHEM,
                                                    table as TABLE_NAME,
                                                    name as COLUMN_NAME,
                                                    type as TYPE_NAME,
                                                    position as ORDINAL_POSITION,
                                                    toInt32(position(type, 'Nullable(') >= 1 ? 1 : 0) as NULLABLE,
                                                    10 as NUM_PREC_RADIX,
                                                from
                                                    system.columns
                                                where
                                                    database like 'helloworld'
                                                    and table like 'my_first_table'
                                                    and name like '%'
                                                    and 1 = 0
                                """;
                        if (optionalConversion.isPresent()) {
                            SemanticTableConversion conversion = optionalConversion.get();
                            log.info("FOUND SemanticColumns: {}", optionalConversion);
                            String template = """
                                                    select
                                                        NULL as TABLE_CAT,
                                                        '%s' as TABLE_SCHEM,
                                                        '%s' as TABLE_NAME,
                                                        '%s' as COLUMN_NAME,
                                                        type as TYPE_NAME
                                                    from
                                                        system.columns
                                                    where
                                                        database like '%s'
                                                        and table like '%s'
                                                        and name like '%s'
                                    """;
                            rewrittenSql = conversion.getColumnConversions().stream().map(c -> String.format(template,
                                            conversion.getSchemaConversion().semanticName(),
                                            conversion.getTableConversion().semanticName(),
                                            c.semanticName(),
                                            conversion.getSchemaConversion().realName(),
                                            conversion.getTableConversion().realName(),
                                            c.realName()))
                                    .collect(Collectors.joining(" union all "));
                        } else {
                            log.warn("Semantic schema not found!");
                        }
                        log.info("Substitution query: {}", rewrittenSql);
                        request.content().clear();
                        request.content().writeCharSequence(rewrittenSql, Charset.defaultCharset());
                        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                        ctx.fireChannelRead(request.retain());
                        return;
                    }

                    if (!query.contains("FORMAT RowBinaryWithNamesAndTypes")) {
                        log.info("SQL SCHEMA|TABLE|COLUMN substitution..");
                        SemanticConvertor convertor = new SemanticConvertor(query, semanticNamesConversion);
                        String rewrittenSql = convertor.rewrite(convertor.getStatement()).toString();
                        request.content().clear();
                        request.content().writeCharSequence(rewrittenSql, Charset.defaultCharset());
                        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        ctx.fireChannelRead(request.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

}

