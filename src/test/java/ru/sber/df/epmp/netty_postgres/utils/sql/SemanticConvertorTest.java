package ru.sber.df.epmp.netty_postgres.utils.sql;

import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.PairConversion;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticTableConversion;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SemanticConvertorTest {

    @SneakyThrows
    public static String readFile(String file) {
        List<String> lines;
        lines = Files.readAllLines(
                Paths.get(SemanticConvertorTest.class.getResource(file).toURI()));
        return String.join(" ", lines);
    }

    @Test
    void rewrite_sql_file_001() {
        SemanticNamesConversion semanticNamesConversion;
        SemanticTableConversion table = new SemanticTableConversion();
        table.setSchemaConversion(PairConversion.of("SCHEMA", "SCHEMA___"));
        table.setTableConversion(PairConversion.of("TABLE", "TABLE___"));
        PairConversion<String, String> col01 = PairConversion.of("aaa", "aaa___");
        PairConversion<String, String> col02 = PairConversion.of("bbb", "bbb___");
        PairConversion<String, String> col03 = PairConversion.of("ccc", "ccc___");
        table.setColumnConversions(List.of(col01, col02, col03));
        semanticNamesConversion = new SemanticNamesConversion(List.of(table));

        String sql = readFile("/sql_00.sql");
        String expected = "";
        SemanticConvertor convertor = new SemanticConvertor(sql, semanticNamesConversion);
        convertor.rewrite(convertor.getStatement());
    }


    @Test
    void rewrite_sql_simple() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        List.of(PairConversion.of("f1", "f1__"),
                                PairConversion.of("f2", "f2__"))
                )
        ));

        String sql_01 = "SELECT f1, f2 FROM db.t1";
        String expected_01 = "SELECT f1__ AS f1, f2__ AS f2 FROM db__.t1__";
        String sql_02 = "SELECT x.* FROM db.t1 x";
        String expected_02 = "SELECT x.* FROM db__.t1__ x";
        SemanticConvertor convertor_01 = new SemanticConvertor(sql_01, semanticNamesConversion);
        SemanticConvertor convertor_02 = new SemanticConvertor(sql_02, semanticNamesConversion);
        assertAll(
                () -> assertEquals(expected_01, convertor_01.rewrite(convertor_01.getStatement()).toString()),
                () -> assertEquals(expected_02, convertor_02.rewrite(convertor_02.getStatement()).toString())
        );
    }

    @Test
    void getAllPossibleSemanticTableNames() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        null)
        ));

        List<String> actual = semanticNamesConversion.getConversions()
                .toArray(new SemanticTableConversion[0])[0]
                .getAllPossibleSemanticTableNames();

        List<String> expected = List.of("t1", "db.t1");
        assertEquals(expected, actual);
    }

    @Test
    void rewrite_sql_column_name() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        List.of(PairConversion.of("f1", "f1__"),
                                PairConversion.of("f2", "f2__"),
                                PairConversion.of("f3", "f3__")
                        )
                )
        ));

        String sql_01 = "SELECT \"f1\", f2, `f3` FROM t1";
        //String sql_02 = "SELECT \"t1.f1\", t1.f2, `t1.f3` FROM db.t1";
        String expected_01 = "SELECT f1__ AS \"f1\", f2__ AS f2, f3__ AS `f3` FROM t1__";
        //String expected_02 = "SELECT t1__.f1__ AS \"t1__.f1\", t1__.f2__ AS t1__.f2, t1__.f3__ AS `t1__.f3` FROM db__.t1__";
        SemanticConvertor convertor_01 = new SemanticConvertor(sql_01, semanticNamesConversion);
        //SemanticConvertor convertor_02 = new SemanticConvertor(sql_02, semanticNamesConversion);
        assertAll(
                () -> assertEquals(expected_01, convertor_01.rewrite(convertor_01.getStatement()).toString())
                //, () -> assertEquals(expected_02, convertor_02.rewrite(convertor_02.getStatement()).toString())
        );
    }

    @Test
    void rewrite_sql_table_name() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        null
                )
        ));

        String sql_01 = "SELECT f1 FROM t1";        /* simple table name */
        String sql_02 = "SELECT f1 FROM db.t1";     /* fully qualified table name */
        String sql_03 = "SELECT f1 FROM \"db.t1\""; /* fully qualified table name in double braces */
        String sql_04 = "SELECT f1 FROM `db.t1`";   /* fully qualified table name in backticks */
        String expected_01 = "SELECT f1 FROM t1__";
        String expected_02 = "SELECT f1 FROM db__.t1__";
        String expected_03 = "SELECT f1 FROM db__.t1__";
        String expected_04 = "SELECT f1 FROM db__.t1__";

        SemanticConvertor convertor_01 = new SemanticConvertor(sql_01, semanticNamesConversion);
        SemanticConvertor convertor_02 = new SemanticConvertor(sql_02, semanticNamesConversion);
        SemanticConvertor convertor_03 = new SemanticConvertor(sql_03, semanticNamesConversion);
        SemanticConvertor convertor_04 = new SemanticConvertor(sql_04, semanticNamesConversion);
        assertAll(
                () -> assertEquals(expected_01, convertor_01.rewrite(convertor_01.getStatement()).toString()),
                () -> assertEquals(expected_02, convertor_02.rewrite(convertor_02.getStatement()).toString()),
                () -> assertEquals(expected_03, convertor_03.rewrite(convertor_03.getStatement()).toString()),
                () -> assertEquals(expected_04, convertor_04.rewrite(convertor_04.getStatement()).toString())
        );
    }

    @Test
    void rewrite_sql_table_name_02() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        List.of(PairConversion.of("unknown_f1", "unreachable_f1"),
                                PairConversion.of("unknown_f2", "unreachable_f2"))
                )
        ));

        String sql_01 = "SELECT * FROM t1";        /* simple table name */
        String sql_02 = "SELECT * FROM db.t1";     /* fully qualified table name */
        String sql_03 = "SELECT * FROM \"db.t1\""; /* fully qualified table name in double braces */
        String sql_04 = "SELECT * FROM `db.t1`";   /* fully qualified table name in backticks */
        String sql_05 = "SELECT * FROM Db.T1";
        String expected_01 = "SELECT * FROM t1__";
        String expected_02 = "SELECT * FROM db__.t1__";
        String expected_03 = "SELECT * FROM db__.t1__";
        String expected_04 = "SELECT * FROM db__.t1__";
        String expected_05 = "SELECT * FROM db__.t1__";
        SemanticConvertor convertor_01 = new SemanticConvertor(sql_01, semanticNamesConversion);
        SemanticConvertor convertor_02 = new SemanticConvertor(sql_02, semanticNamesConversion);
        SemanticConvertor convertor_03 = new SemanticConvertor(sql_03, semanticNamesConversion);
        SemanticConvertor convertor_04 = new SemanticConvertor(sql_04, semanticNamesConversion);
        SemanticConvertor convertor_05 = new SemanticConvertor(sql_05, semanticNamesConversion);

        assertAll(
                () -> assertEquals(expected_01, convertor_01.rewrite(convertor_01.getStatement()).toString()),
                () -> assertEquals(expected_02, convertor_02.rewrite(convertor_02.getStatement()).toString()),
                () -> assertEquals(expected_03, convertor_03.rewrite(convertor_03.getStatement()).toString()),
                () -> assertEquals(expected_04, convertor_04.rewrite(convertor_04.getStatement()).toString()),
                () -> assertEquals(expected_05, convertor_05.rewrite(convertor_05.getStatement()).toString())
        );
    }

    @Test
    void rewrite_sql_subselect() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        List.of(PairConversion.of("f1", "f1__"),
                                PairConversion.of("f2", "f2__"))
                )
        ));

        String sql_01 = "select c1 from (SELECT f1, f2 FROM t1)";
        String expected = "SELECT c1 FROM (SELECT f1__ AS f1, f2__ AS f2 FROM t1__)";
        SemanticConvertor convertor = new SemanticConvertor(sql_01, semanticNamesConversion);
        assertAll(
                () -> assertEquals(expected, convertor.rewrite(convertor.getStatement()).toString())
        );
    }

    @Test
    void rewrite_sql_dictget_tuple() {
        SemanticNamesConversion semanticNamesConversion = new SemanticNamesConversion(List.of(
                new SemanticTableConversion(
                        PairConversion.of("db", "db__"),
                        PairConversion.of("t1", "t1__"),
                        List.of(PairConversion.of("f1", "f1__"),
                                PairConversion.of("f2", "f2__"))
                )
        ));

        String sql_01 = """
            SELECT dictGet('db.t1', 'alias_name', tuple('SOME_KEY', 'LEVEL', f1)) AS some_alias
            from db2.t2
            GROUP BY dictGet('db.t1', 'alias_name', tuple('SOME_KEY', 'LEVEL', f1))
            ORDER BY dictGet('db.t1', 'alias_name', tuple('SOME_KEY', 'LEVEL', f1)) ASC
        """;
        String expected = """
            SELECT dictGet('db__.t1__', 'alias_name', tuple('SOME_KEY', 'LEVEL', f1)) AS some_alias
            FROM db2.t2
            GROUP BY dictGet('db__.t1__', 'alias_name', tuple('SOME_KEY', 'LEVEL', f1))
            ORDER BY dictGet('db__.t1__', 'alias_name', tuple('SOME_KEY', 'LEVEL', f1)) ASC
        """.replaceAll("\\s+", " ").trim();
        SemanticConvertor convertor = new SemanticConvertor(sql_01, semanticNamesConversion);
        assertAll(
                () -> assertEquals(expected, convertor.rewrite(convertor.getStatement()).toString())
        );
    }
}