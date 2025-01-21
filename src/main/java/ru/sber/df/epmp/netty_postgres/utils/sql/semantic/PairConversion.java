package ru.sber.df.epmp.netty_postgres.utils.sql.semantic;

public record PairConversion<K, V>(K semanticName, V realName) {
    public static <K, V> PairConversion<K, V> of(K semanticName, V realName) {
        return new PairConversion<>(semanticName, realName);
    }
}