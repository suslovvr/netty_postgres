package ru.sber.df.epmp.netty_postgres.utils.sql;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static ru.sber.df.epmp.netty_postgres.utils.sql.CustomSqlCommand.EXTRACT_SOURCES;

@Slf4j
public class SqlCommandUtil {

    public static boolean containsCommand(String sql) {
        return sql != null && !sql.isEmpty() &&
                Arrays.stream(CustomSqlCommand.values())
                        .anyMatch(cmd -> StringUtils.containsIgnoreCase(sql, cmd.getSqlCommand()));
    }

    @SneakyThrows
    public static String findAndReplaceCommandIfNeeded(String originalSql) {
        for (CustomSqlCommand cmd : CustomSqlCommand.values()) {
            if (StringUtils.containsIgnoreCase(originalSql, cmd.getSqlCommand())) {
                switch (cmd) {
                    case PING -> {
                        return CustomSqlCommand.PING.getReplacement();
                    }
                    case EXTRACT_SOURCES -> {
                        String updatedSql = StringUtils.removeIgnoreCase(originalSql, EXTRACT_SOURCES.getSqlCommand());
                        Set<String> tables = new TreeSet<>(TablesNamesFinder.findTables(updatedSql));
                        StringBuilder sb = new StringBuilder();
                        sb.append("select '");
                        sb.append(String.join("' as table union all select '", tables));
                        sb.append("' as table");
                        if (tables.isEmpty()) {
                            sb.append(" where 1 = 0 ");
                        }
                        String rewrittenQuery = sb.toString();
                        log.debug("Command: {}; Rewritten query: {}", EXTRACT_SOURCES, rewrittenQuery);
                        return rewrittenQuery;
                    }
                }
            }
        }
        return originalSql;
    }

}
