package ru.sber.df.epmp.netty_postgres.utils.sql.semantic;

import ru.sber.df.epmp.netty_postgres.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticTableConversion {
    PairConversion<String, String> schemaConversion;
    PairConversion<String, String> tableConversion;
    List<PairConversion<String, String>> columnConversions;

    public Optional<PairConversion<String, String>> findColumnConversionBySemanticName(String name) {
        Map<String, PairConversion<String, String>> names = new HashMap<>();
        columnConversions.forEach(c -> names.put(c.semanticName(), c));
        columnConversions.forEach(c -> names.put(getFullyQualifiedSemanticColumnName(c.semanticName()), c));
        return Optional.ofNullable(names.getOrDefault(Utils.removeQuotation(name), null));
    }

    public String getFullyQualifiedSemanticColumnName(String column) {
        return String.format("%s.%s", tableConversion.semanticName(), column);
    }

    public String getFullyQualifiedSemanticTableName() {
        return String.format("%s.%s", schemaConversion.semanticName(), tableConversion.semanticName());
    }

    public List<String> getAllPossibleSemanticTableNames() {
        return List.of(getTableConversion().semanticName(), getFullyQualifiedSemanticTableName());
    }
}