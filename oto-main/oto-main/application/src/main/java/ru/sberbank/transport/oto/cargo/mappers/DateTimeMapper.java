package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Mapper
public interface DateTimeMapper {
    
    default ZonedDateTime toZoned(LocalDateTime source) {
        if (source == null) {
            return null;
        }
        return source.atZone(ZoneOffset.UTC);
    }
    
    default LocalDateTime toLocal(ZonedDateTime source) {
        return source.toLocalDateTime();
    }
    
}
