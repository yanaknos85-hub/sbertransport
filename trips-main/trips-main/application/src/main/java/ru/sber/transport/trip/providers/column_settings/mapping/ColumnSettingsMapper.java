package ru.sber.transport.trip.providers.column_settings.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.Collections;
import java.util.Map;

/**
 * Маппер настроек отображения столбцов.
 */
@Mapper
public interface ColumnSettingsMapper {

    @SneakyThrows
    default JSON toJson(Map<String, Object> map){
        if(map == null || map.isEmpty()) {
            return JSON.json("{}");
        }
        return JSON.valueOf(objectMapper().writeValueAsString(map));
    }

    @SneakyThrows
    default Map<String, Object> toMap(JSON json){
        if (json == null) {
            return Collections.emptyMap();
        }
        return objectMapper().readValue(json.data(), new TypeReference<>() {});
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
}
