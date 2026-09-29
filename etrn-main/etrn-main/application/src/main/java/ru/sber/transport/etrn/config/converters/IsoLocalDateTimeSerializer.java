package ru.sber.transport.etrn.config.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class IsoLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> implements Converter<LocalDateTime, String> {
    private static final long serialVersionUID = 1L;

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
        gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public String convert(LocalDateTime source) {
        return source.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
