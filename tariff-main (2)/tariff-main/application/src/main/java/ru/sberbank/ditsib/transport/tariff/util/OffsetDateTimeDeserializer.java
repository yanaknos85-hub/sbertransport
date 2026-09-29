package ru.sberbank.ditsib.transport.tariff.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> implements Converter<String, OffsetDateTime> {
    private static final long serialVersionUID = 1L;
    
    public OffsetDateTimeDeserializer() {
    }
    
    public OffsetDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return OffsetDateTime.parse(jp.readValueAs(String.class), DateTimeFormatter.ISO_DATE_TIME);
    }
    
    public OffsetDateTime convert(String s) {
        return OffsetDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(s));
    }
}