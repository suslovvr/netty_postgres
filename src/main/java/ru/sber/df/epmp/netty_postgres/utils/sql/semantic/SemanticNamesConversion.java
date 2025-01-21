package ru.sber.df.epmp.netty_postgres.utils.sql.semantic;

import ru.sber.df.epmp.netty_postgres.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "semantic")
public class SemanticNamesConversion {
    List<SemanticTableConversion> conversions;

    public Optional<SemanticTableConversion> getSemanticTableNameIfAny(String tableName) {
        return conversions.stream()
                .filter(t -> t.getAllPossibleSemanticTableNames().stream()
                        .anyMatch(n -> n.equalsIgnoreCase(Utils.removeQuotation(tableName))))
                .findFirst();
    }
}