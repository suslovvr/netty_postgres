package ru.sber.df.epmp.netty_postgres.utils.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JsqlParserTest {

    /**
     * <a href="https://clickhouse.com/docs/en/interfaces/formats">Formats for Input and Output Data</a>
     * <a href="https://clickhouse.com/docs/en/sql-reference/statements/select">SELECT Query Definition</a>
     */
    @Disabled
    @Test
    void parse_sql_001() throws JSQLParserException {
        String sql = "select 2 FORMAT RowBinaryWithNamesAndTypes";
        CCJSqlParserUtil.parse(sql);
    }
}
