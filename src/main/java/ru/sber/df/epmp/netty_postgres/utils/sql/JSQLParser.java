package ru.sber.df.epmp.netty_postgres.utils.sql;

import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class JSQLParser {

    @SneakyThrows
    public static Set<String> parseAndExtractTables(String sql) {
        Set<String> tables = new TreeSet<>();
        Statement stmt = CCJSqlParserUtil.parse(sql);
        extractTables(stmt, tables);
        return tables;
    }

    @SneakyThrows
    public static void extractTables(Statement stmt, Set<String> tables) {
        if (stmt instanceof PlainSelect select) {
            FromItem from = select.getFromItem();
            if (from == null) {
                return;
            }
            if (from instanceof ParenthesedSelect innerSelect) {
                extractTables(innerSelect.getSelect(), tables);
                return;
            }
            tables.add(((Table) from).getFullyQualifiedName());
            List<Join> joins = select.getJoins();
            if (joins != null && !joins.isEmpty()) {
                List<String> joinTables = joins.stream()
                        .map(j -> ((Table) j.getFromItem()).getFullyQualifiedName())
                        .toList();
                tables.addAll(joinTables);
            }
        }

        if (stmt instanceof SetOperationList) {
            List<Select> selects = ((SetOperationList) stmt).getSelects();
            for (Select select : selects) {
                extractTables(select, tables);
            }
        }
    }

}
