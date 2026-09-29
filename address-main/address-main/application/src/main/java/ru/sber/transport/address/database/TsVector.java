package ru.sber.transport.address.database;

import lombok.Getter;
import ru.sber.transport.address.utils.Translit;

import java.util.LinkedList;
import java.util.List;

public class TsVector {

    private final List<String> values = new LinkedList<>();

    private String delimiter;

    @Getter
    private boolean newData;

    private Object vectoredString;

    public TsVector(Object vectoredString) {
        this.vectoredString = vectoredString;
    }

    public TsVector(String delimiter) {
        if (delimiter == null) {
            delimiter = " ";
        }
        newData = true;
        this.delimiter = delimiter;
    }

    public void addValue(String value) {
        newData = true;
        values.add(value);
    }

    public Object getStringToVector() {
        if (newData) {
            var builder = new StringBuilder();
            for (var item : values) {
                append(builder, item);
            }
            return Translit.transliterate(builder.toString().toLowerCase());
        }
        return vectoredString;
    }

    private void append(StringBuilder builder, String data) {
        if (data != null) {
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(data);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(getStringToVector());
    }
}
