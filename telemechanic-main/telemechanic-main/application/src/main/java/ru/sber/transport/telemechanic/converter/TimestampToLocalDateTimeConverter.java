package ru.sber.transport.telemechanic.converter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TimestampToLocalDateTimeConverter implements Converter<Timestamp, LocalDateTime> {
    
    @Override
    public LocalDateTime from(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    @Override
    public Timestamp to(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }
    
    @Override
    public @NotNull Class<Timestamp> fromType() {
        return Timestamp.class;
    }
    
    @Override
    public @NotNull Class<LocalDateTime> toType() {
        return LocalDateTime.class;
    }
}
