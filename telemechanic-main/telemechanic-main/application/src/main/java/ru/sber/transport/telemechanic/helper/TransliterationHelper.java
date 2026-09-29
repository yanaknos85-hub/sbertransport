package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;

import java.util.Map;


@UtilityClass
public class TransliterationHelper {

    private final Map<Character, String> mapping = Map.ofEntries(
            Map.entry('0', "0"),
            Map.entry('1', "1"),
            Map.entry('2', "2"),
            Map.entry('3', "3"),
            Map.entry('4', "4"),
            Map.entry('5', "5"),
            Map.entry('6', "6"),
            Map.entry('7', "7"),
            Map.entry('8', "8"),
            Map.entry('9', "9"),
            Map.entry('A', "А"),
            Map.entry('B', "В"),
            Map.entry('C', "С"),
            Map.entry('E', "Е"),
            Map.entry('H', "Н"),
            Map.entry('K', "К"),
            Map.entry('M', "М"),
            Map.entry('O', "О"),
            Map.entry('P', "Р"),
            Map.entry('S', "С"),
            Map.entry('T', "Т"),
            Map.entry('X', "Х"),
            Map.entry('Y', "У"),
            Map.entry('А', "А"),
            Map.entry('В', "В"),
            Map.entry('Е', "Е"),
            Map.entry('К', "К"),
            Map.entry('М', "М"),
            Map.entry('Н', "Н"),
            Map.entry('О', "О"),
            Map.entry('Р', "Р"),
            Map.entry('С', "С"),
            Map.entry('Т', "Т"),
            Map.entry('У', "У"),
            Map.entry('Х', "Х")
    );

    public String transliterateNumber(String number) {
        var result = new StringBuilder();
        for (char ch : number.toUpperCase().toCharArray()) {
            result.append(mapping.getOrDefault(ch, ""));
        }
        return result.toString();
    }
}
