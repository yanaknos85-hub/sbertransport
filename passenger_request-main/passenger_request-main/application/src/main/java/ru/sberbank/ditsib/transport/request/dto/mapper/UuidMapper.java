package ru.sberbank.ditsib.transport.request.dto.mapper;

import org.mapstruct.Mapper;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Mapper
public interface UuidMapper {
    
    default UUID map(String value) {
        return Optional.ofNullable(value)
                       .filter(id -> Pattern.matches("^[a-f\\d]{8}(-[a-f\\d]{3})-[a-f\\d]{12}$", value))
                       .map(UUID::fromString)
                       .orElse(null);
    }
    
}
