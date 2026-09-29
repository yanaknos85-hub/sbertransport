package ru.sber.transport.push.providers.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.database.push.tables.records.SendHistoryRecord;

@Mapper
public interface SendHistoryMapper {

    SendHistoryRecord toRecord(SendHistoryDto sendHistoryDto);

    default JSON toJson(SendHistoryDto.ErrorDescription errorDescription){
        if (errorDescription != null) {
            try {
                return JSON.valueOf(objectMapper().writeValueAsString(errorDescription));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else return null;
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
}
