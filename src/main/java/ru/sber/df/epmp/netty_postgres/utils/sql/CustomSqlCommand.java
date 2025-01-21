package ru.sber.df.epmp.netty_postgres.utils.sql;

import java.util.HashMap;
import java.util.Map;

public enum CustomSqlCommand {

    PING("PING", "select 'PONG'"),
    EXTRACT_SOURCES("EXTRACT SOURCES", "");

    private static final Map<String, CustomSqlCommand> BY_CMD_NAME = new HashMap<>();

    static {
        for (CustomSqlCommand cmd : values()) {
            BY_CMD_NAME.put(cmd.sqlCommand, cmd);
        }
    }

    private final String sqlCommand;
    private final String replacement;

    CustomSqlCommand(String sqlCommand, String replacement) {
        this.sqlCommand = sqlCommand;
        this.replacement = replacement;
    }

    public String getSqlCommand() {
        return sqlCommand;
    }

    public String getReplacement() {
        return replacement;
    }

    public static CustomSqlCommand valueBySqlCommand(String sqlCommand) {
        return BY_CMD_NAME.get(sqlCommand);
    }

}
