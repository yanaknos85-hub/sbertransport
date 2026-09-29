package ru.sber.transport.contractor.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.contractor.dto.EmailIntegrationParamsDto;
import ru.sber.transport.contractor.database.model.EmailIntegrationParams;

/**
 * Маппер параметров интеграции.
 */
@Mapper
public interface EmailIntegrationParamsMapper {

    /**
     * Преобразовать модель в объект обмена данными.
     *
     * @param source исходный объект.
     * @return объект обмена данными.
     */
    EmailIntegrationParams toModel(EmailIntegrationParamsDto source);

    /**
     * Преобразовать объект обмена данными в модель.
     *
     * @param source исходный объект.
     * @return модель.
     */
    EmailIntegrationParamsDto toDto(EmailIntegrationParams source);

}
