package ru.sber.transport.contractor.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Десериализатор номеров телефонов. Если начинается с 8, первое число заменяется на +7
 */
public class PhoneSerializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var sourceValue = p.getValueAsString();
        if (sourceValue.startsWith("8")) {
            sourceValue = sourceValue.replaceFirst("8", "+7");
        }
        return sourceValue;
    }
}
