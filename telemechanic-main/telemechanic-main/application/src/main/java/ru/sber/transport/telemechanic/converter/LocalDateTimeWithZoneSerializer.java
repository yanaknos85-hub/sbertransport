package ru.sber.transport.telemechanic.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeWithZoneSerializer extends JsonSerializer<LocalDateTime> implements Converter<LocalDateTime, String> {
    
    public LocalDateTimeWithZoneSerializer() {}
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializer) throws IOException {
        gen.writeString(this.convert(value));
    }
    
    @Override
    public String convert(LocalDateTime localDateTime) {
        var offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    
    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(LocalDateTime.class);
    }
    
    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }
}
