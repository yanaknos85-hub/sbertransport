package ru.sber.transport.telemechanic.converter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TimestampToMillisConverter implements Converter<Timestamp, Long> {
    
    @Override
    public Long from(Timestamp source) {
        return source != null
               ? source.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli()
               : null;
    }
    
    @Override
    public Timestamp to(Long source) {
        return source != null
               ? Timestamp.valueOf(LocalDateTime.ofInstant(Instant.ofEpochMilli(source), ZoneId.of(ZoneOffset.UTC.getId())))
               : null;
    }
    
    @Override
    public @NotNull Class<Timestamp> fromType() {
        return Timestamp.class;
    }
    
    @Override
    public @NotNull Class<Long> toType() {
        return Long.class;
    }
}
