package ru.sber.transport.contractor.testutils;

public final class TestUtils {
    public static String replaceEnd(String source, String s) {
        return source.substring(0, source.length() - s.length()).concat(s);
    }


    public static String replaceEnd(String source, int i) {
        var iString = String.valueOf(i);
        return source.substring(0, source.length() - iString.length()).concat(iString);
    }
}
