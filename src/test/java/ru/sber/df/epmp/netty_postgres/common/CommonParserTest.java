package ru.sber.df.epmp.netty_postgres.common;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public abstract class CommonParserTest {

    public static final String SELECT_ONE = "select 1";
    public static final String SQL;

    static {
        String file = "/sql_00.sql";
//        String file = "/sql_01.sql";
//        String file = "/sql_002.sql";
//        String file = "/sql_001.sql";
//        String file = "/sql_000.sql";
//        String file = "/sql_01.sql";
//        String file = "/sql_02.sql";
        List<String> lines;
        try {
            lines = Files.readAllLines(
                    Paths.get(Objects.requireNonNull(CommonParserTest.class.getResource(file),
                            "Provide correct path to a file in the resource path").toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SQL = String.join(" ", lines);
//        System.out.printf("file: %s %n", file);
//        System.out.printf("bytes: %s in sql%n", SQL.getBytes().length);
    }

}
