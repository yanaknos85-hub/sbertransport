package ru.sber.transport.contractor.util;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class LoginPasswordGeneratorUtils {

    private static final char[] SYMBOLS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789").toCharArray();

    private static final Map<Character, String> TRANSLITERATION_TABLE =
            Map.ofEntries(
                    Map.entry('а', "a"),
                    Map.entry('б', "b"),
                    Map.entry('в', "v"),
                    Map.entry('г', "g"),
                    Map.entry('д', "d"),
                    Map.entry('е', "e"),
                    Map.entry('ё', "yo"),
                    Map.entry('ж', "zh"),
                    Map.entry('з', "z"),
                    Map.entry('и', "i"),
                    Map.entry('й', "y"),
                    Map.entry('к', "k"),
                    Map.entry('л', "l"),
                    Map.entry('м', "m"),
                    Map.entry('н', "n"),
                    Map.entry('о', "o"),
                    Map.entry('п', "p"),
                    Map.entry('р', "r"),
                    Map.entry('с', "s"),
                    Map.entry('т', "t"),
                    Map.entry('у', "u"),
                    Map.entry('ф', "f"),
                    Map.entry('х', "kh"),
                    Map.entry('ц', "ts"),
                    Map.entry('ч', "ch"),
                    Map.entry('ш', "sh"),
                    Map.entry('щ', "sch"),
                    Map.entry('ъ', ""),
                    Map.entry('ы', "y"),
                    Map.entry('ь', ""),
                    Map.entry('э', "e"),
                    Map.entry('ю', "yu"),
                    Map.entry('я', "ya")
            );

    public static String generateLogin(String lastName, String firstName, String patronymic) {
        String lastNameTranslit = transliterate(lastName);
        String firstNameTranslit = transliterate(firstName);
        String patronymicTranslit = (patronymic != null ? transliterate(patronymic) : "");

        StringBuilder sb = new StringBuilder("KA-" + lastNameTranslit.toLowerCase());
        sb.append(firstNameTranslit.substring(0, 1).toLowerCase());

        if (!patronymicTranslit.isEmpty()) {
            sb.append(patronymicTranslit.substring(0, Math.min(2, patronymicTranslit.length())).toLowerCase());
        }

        sb.append("-").append(generateSeq(6));


        return sb.toString();
    }

    public static String generatePassword(int length) {
        return generateSeq(length);
    }

    private static String generateSeq(int length) {
        var random = ThreadLocalRandom.current();
        char[] buffer = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = SYMBOLS[random.nextInt(SYMBOLS.length)];
        }
        return new String(buffer);
    }

    private static String transliterate(String message) {
        StringBuilder builder = new StringBuilder();
        for (var ch : message.toLowerCase().toCharArray()) {
            builder.append(TRANSLITERATION_TABLE.getOrDefault(ch, String.valueOf(ch)));
        }
        return builder.toString();
    }
}
