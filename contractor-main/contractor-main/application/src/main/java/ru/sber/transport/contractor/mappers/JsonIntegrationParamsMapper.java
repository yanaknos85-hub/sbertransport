package ru.sber.transport.contractor.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.contractor.dto.JsonIntegrationParamsDto;
import ru.sber.transport.contractor.database.model.JsonIntegrationParams;
import ru.sber.ditsib.encription.PasswordEncryption;

import java.util.Optional;

/**
 * Маппер параметров интеграции.
 */
@Mapper
public interface JsonIntegrationParamsMapper {

    /**
     * Name of mapper for protection data.
     */
    String QUALIFIER_PROTECTION = "protection";

    /**
     * Placeholder for protected data.
     */
    String PROTECTED_FLAG = "[protected]";

    /**
     * Update parameters of integration.
     *
     * @param target object to update.
     * @param source source data.
     */
    @Mapping(target = "password", ignore = true)
    void update(@MappingTarget JsonIntegrationParams target, JsonIntegrationParamsDto source);

    /**
     * Преобразовать объект обмена данными в модель.
     *
     * @param source исходный объект.
     * @return модель.
     */
    @Mapping(target = "password", source = "password", qualifiedByName = QUALIFIER_PROTECTION)
    JsonIntegrationParamsDto toDto(JsonIntegrationParams source);

    /**
     * Update data with encryption sensitive part of it.
     *
     * @param params data to update.
     * @param source source data.
     */
    default void encryption(@MappingTarget JsonIntegrationParams params, String source) {
        Optional.ofNullable(source)
                .filter(s -> !s.equals(PROTECTED_FLAG))
                .map(passwordEncryption()::encode)
                .ifPresent(params::setPassword);
    }

    /**
     * Mask protected data.
     *
     * @param source source data.
     * @return masked data.
     */
    @Named(QUALIFIER_PROTECTION)
    default String protection(String source) {
        return Optional.ofNullable(source).map(s -> PROTECTED_FLAG).orElse(null);
    }

    /**
     * Get encryption bean.
     *
     * @return encryption bean.
     */
    @Lookup
    default PasswordEncryption passwordEncryption() {
        return null;
    }

}
