package ru.sber.df.epmp.netty_postgres;

import ru.sber.df.epmp.netty_postgres.common.CommonParserTest;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.junit.jupiter.api.Test;

public class CalciteParserTest extends CommonParserTest {

    static final SqlParser.Config parserConfig;

    static {
       parserConfig = SqlParser.config()
                .withUnquotedCasing(Casing.UNCHANGED)
                .withQuotedCasing(Casing.UNCHANGED)
                .withCaseSensitive(true);
    }

    /**
     * To check avg time per executions
     */
    @Test
    void load_file() throws SqlParseException {
        long start = System.currentTimeMillis();
        SqlParser.create(SQL, parserConfig).parseQuery();
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.printf("time elapsed: %s in ms%n", timeElapsed);
    }

    /**
     * To check avg time per executions
     */
    @Test
    void select_one() throws SqlParseException {
        long start = System.currentTimeMillis();
        SqlParser.create(SELECT_ONE, parserConfig).parseQuery();
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.printf("time elapsed: %s in ms%n", timeElapsed);
    }

    @Test
    void multiple_sql() throws SqlParseException {
        String sql_01 = "select 1";
        String sql_02 = "select 1 from t1 where 1 = 1";
        String sql_03 = "select 1 from db.t1 where 1 = 1";
        String sql_04 = """
            SELECT f1, c FROM db.t1
            UNION ALL
            SELECT f2, c FROM db.t2
            UNION ALL
            SELECT f3, c FROM db.t3
            """;
        String sql_05 = """
           SELECT f1, f2 FROM t1 LEFT OUTER JOIN t2 ON t1.id = t2.id
           """;
        String sql_06 = """
           SELECT f1, f2 FROM t1 WHERE t1_id IN (SELECT t2_id FROM t2)
           """;
        SqlParser.Config parserConfig = SqlParser.config()
                .withUnquotedCasing(Casing.UNCHANGED)
                .withQuotedCasing(Casing.UNCHANGED)
                .withCaseSensitive(true);

        SqlNode node1 = SqlParser.create(sql_01, parserConfig).parseQuery();
        SqlNode node2 = SqlParser.create(sql_02, parserConfig).parseQuery();
        SqlNode node3 = SqlParser.create(sql_03, parserConfig).parseQuery();
        SqlNode node4 = SqlParser.create(sql_04, parserConfig).parseQuery();
        SqlNode node5 = SqlParser.create(sql_05, parserConfig).parseQuery();
        SqlNode node6 = SqlParser.create(sql_06, parserConfig).parseQuery();
    }


}
