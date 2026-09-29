package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Mapper
public interface DateTimeMapper {
    
    default ZonedDateTime toZoned(LocalDateTime source) {
        return source.atZone(ZoneOffset.UTC);
    }
    
    default LocalDateTime toLocal(ZonedDateTime source) {
        return source.toLocalDateTime();
    }
    
}
