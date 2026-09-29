package ru.sber.transport.contractor.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> implements Converter<OffsetDateTime, String> {

    private static final long serialVersionUID = 1L;


    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
        gen.writeString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Override
    public String convert(OffsetDateTime offsetDateTime) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime);
    }
}