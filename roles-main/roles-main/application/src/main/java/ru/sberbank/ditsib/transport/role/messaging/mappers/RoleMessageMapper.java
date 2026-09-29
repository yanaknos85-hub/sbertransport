package ru.sberbank.ditsib.transport.role.messaging.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;
import ru.sber.transport.roles.messages.RoleMessage;

import java.util.Collections;
import java.util.List;

@Mapper
public interface RoleMessageMapper {

    @Mapping(target = "scopes", source = "entity.defaultFor")
    @Mapping(target = "exclusives", source = "entity.exclusive")
    @Mapping(target = "deleted", source = "deleted")
    RoleMessage toMessage(RoleRecord entity, boolean deleted);

    @SuppressWarnings("java:S3958")
    @SneakyThrows(JsonProcessingException.class)
    default List<String> getList(JSON source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return objectMapper().readValue(source.data(), new TypeReference<>() {});
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

}
