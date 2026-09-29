package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.reports.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;

import java.time.Duration;

@Mapper
public interface ExpectedDataMapper {
    
    ExpectedDataDTO toDto(ExpectedData source);
    
    default Integer map(Duration value) {
        return Long.valueOf(value == null ? 0 : value.toMinutes()).intValue();
    }
    
}
