package ru.sberbank.ditsib.transport.request.config.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Десериализация строки с исключением спецсимволов.
 */
public class SpecialStringRemoveDeserializer extends JsonDeserializer<String> {
    
    private final static String[] specialChars = { "+", "@" };
    
    @Override
    public String deserialize(
            JsonParser p, DeserializationContext ctxt
                             ) throws IOException, JsonProcessingException {
        var value = p.getText();
        for (var specialChar : specialChars) {
            value = value.replace(specialChar," ");
        }
        value = value.stripTrailing();
        return value;
    }
}
