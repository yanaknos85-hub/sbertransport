package ru.sber.transport.telemechanic.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeWithZoneDeserializer extends JsonDeserializer<LocalDateTime> implements Converter<String, LocalDateTime> {

    public LocalDateTimeWithZoneDeserializer() {}
    
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this.convert(p.readValueAs(String.class));
    }
    
    public LocalDateTime convert(String s) {
        return LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(s));
    }
}
