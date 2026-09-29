package ru.sber.transport.trip.providers.trips.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.trip.business.model.Waypoint;

import java.util.List;

@Mapper
public interface WaypointsMapper {

    @SneakyThrows(JsonProcessingException.class)
    default List<Waypoint> toBusiness(JSON source) {
        if (source == null) {
            return List.of();
        }
        return objectMapper().readValue(source.data(), new TypeReference<>() {});
    }

    @SneakyThrows(JsonProcessingException.class)
    default JSON toEntity(List<Waypoint> source) {
        if (source == null) {
            return JSON.json("[]");
        }
        return JSON.json(objectMapper().writeValueAsString(source));
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
}
