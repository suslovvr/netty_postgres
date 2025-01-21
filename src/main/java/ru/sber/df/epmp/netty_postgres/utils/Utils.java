package ru.sber.df.epmp.netty_postgres.utils;

import java.util.regex.Pattern;

public final class Utils {

    private Utils() {}

    static final String quotationCharsRegex = "[`'\"]";
    static final Pattern quotationPattern = Pattern.compile(quotationCharsRegex);

    public static String quoteString(char quote, String str) {
        return String.format("%c%s%c", quote, str, quote);
    }

    public static String removeQuotation(String name) {
        return quotationPattern.matcher(name).replaceAll("");
    }
}
