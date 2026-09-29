package ru.sber.transport.contractor.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Десериализация строки с исключением спецсимволов.
 */
public class SpecialStringRemoveDeserializer extends JsonDeserializer<String> {
    
    private static final String[] SPECIAL_CHARS = { "+", "@" };
    
    @Override
    public String deserialize(
            JsonParser p, DeserializationContext ctxt
                             ) throws IOException {
        var value = p.getText();
        for (var specialChar : SPECIAL_CHARS) {
            value = value.replace(specialChar," ");
        }
        value = value.stripTrailing();
        return value;
    }
}
