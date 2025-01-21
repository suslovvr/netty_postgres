package ru.sber.df.epmp.netty_postgres.utils.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class SqlHints {

    /**
     * Possible values: true or false or not set at all.
     * If it is set to true then datagate will skip processing and will send SQL-requests as is.
     **/
    public static final String DATAGATE_RAW_MODE = "datagate_raw_mode";

    public static boolean containsHint(String sql) {
        return StringUtils.containsAnyIgnoreCase(sql, DATAGATE_RAW_MODE);
    }

    public static boolean isDatagateRawModeEnabled(String sql) {
        return sql != null
                && !sql.isEmpty()
                && Arrays.stream(StringUtils.split(sql, "/*", 10))
                .filter(SqlHints::containsHint)
                .filter(h -> StringUtils.containsIgnoreCase(h, DATAGATE_RAW_MODE))
                .anyMatch(v -> StringUtils.containsAnyIgnoreCase(v, "true", "1"));
    }
}
