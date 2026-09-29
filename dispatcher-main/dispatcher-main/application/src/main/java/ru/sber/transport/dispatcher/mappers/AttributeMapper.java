package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.database.model.Attribute;
import ru.sber.transport.dispatcher.dto.AttributeDTO;
import ru.sber.transport.dispatcher.dto.NewAttributeDTO;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;

/**
 * Маппер аттрибутов водителей.
 */
@Mapper(uses = ContractorMapper.class)
public interface AttributeMapper {

    /**
     * Преобразовать модель в объект обмена данными.
     *
     * @param source исходный объект.
     * @return объект обмена данными.
     */
    @Mapping(target = "contractor", source = "contractor", qualifiedByName = "short")
    AttributeDTO toDto(Attribute source);

    /**
     * Обновить объект модель данных.
     *
     * @param target целевой объект.
     * @param source исходный объект.
     */
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Attribute target, NewAttributeDTO source);

    @Mapping(target = "contractor", source = "contractor.id")
    ContractorUpdateTripMessage.Driver.DriverTag toMessage(Attribute attribute);
}
