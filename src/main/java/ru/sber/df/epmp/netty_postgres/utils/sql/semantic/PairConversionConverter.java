package ru.sber.df.epmp.netty_postgres.utils.sql.semantic;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class PairConversionConverter implements Converter<String, PairConversion<String, String>> {
    @Override
    public PairConversion<String, String> convert(String source) {
        String[] data = source.split(":");
        return new PairConversion<>(data[0], data[1]);
    }
}
