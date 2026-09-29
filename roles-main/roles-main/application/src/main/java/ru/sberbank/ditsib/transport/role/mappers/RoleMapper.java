package ru.sberbank.ditsib.transport.role.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.role.dto.ExeclusiveUsing;
import ru.sberbank.ditsib.transport.role.dto.RoleDto;
import ru.sberbank.ditsib.transport.role.dto.Scope;

import java.util.Collections;
import java.util.List;

@Mapper
public interface RoleMapper {

    @Mapping(target = "defaultFor", source = "scopes")
    RoleRecord toModel(RoleDto role);

    @Mapping(target = "scopes", source = "defaultFor")
    RoleDto toDto(RoleRecord entity);

    @SneakyThrows(JsonProcessingException.class)
    default List<Scope> toDto(JSON scopes) {
        if (scopes == null) {
            return Collections.emptyList();
        }
        return objectMapper().readValue(scopes.data(), new TypeReference<>() {});
    }

    @SneakyThrows(JsonProcessingException.class)
    default List<ExeclusiveUsing> toExclusiveDto(JSON exclusives) {
        if (exclusives == null) {
            return Collections.emptyList();
        }
        return objectMapper().readValue(exclusives.data(), new TypeReference<>() {});
    }

    @SneakyThrows(JsonProcessingException.class)
    default JSON toModel(List<Scope> scopes) {
        if (scopes == null) {
            return JSON.valueOf("[]");
        }
        var scopesJson = objectMapper().writeValueAsString(scopes);
        return JSON.json(scopesJson);
    }

    @SneakyThrows(JsonProcessingException.class)
    default JSON toExclusiveModel(List<ExeclusiveUsing> source) {
        var json = objectMapper().writeValueAsString(source);
        return JSON.json(json);
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
}
