package ru.sberbank.ditsib.transport.reports.config.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * Десериализация строки с исключением спецсимволов.
 */
public class SpecialStringRemoveDeserializer extends JsonDeserializer<String> {
    
    private final static List<Character> specialChars = List.of('-', '+', '@');
    
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (Character.isLetterOrDigit(ch) || specialChars.contains(ch)) {
                sb.append(ch);
            } else {
                sb.append(" ");
            }
        }
        return sb.toString().replaceAll("\\s+", " ").strip();
    }
}
