package ru.sberbank.ditsib.transport.reports.dto.taxi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LimitResultSet {
    
    private UUID id;
    private String humanReadableId;
    private UUID departmentId;
    
    public LimitResultSet(UUID id, String humanReadableId, UUID departmentId) {
        this.id = id;
        this.humanReadableId = humanReadableId;
        this.departmentId = departmentId;
    }
}
