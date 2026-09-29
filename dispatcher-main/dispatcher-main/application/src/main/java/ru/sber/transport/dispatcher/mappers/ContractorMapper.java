package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.dto.ContractorDTO;
import ru.sber.transport.dispatcher.dto.NewContractorDTO;
import ru.sber.transport.dispatcher.messages.ContractorMessage;

/**
 * Маппер договоров.
 */
@Mapper(uses = {BooleanMapper.class, DispatcherMapper.class})
public interface ContractorMapper {

    /**
     * Конвертация в объект обмена данных.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "humanReadableId", source = "digitId")
    @Mapping(target = "isInternal", expression = "java(source.isInternal())")
    ContractorDTO toDto(Contractor source);

    /**
     * Конвертация в сокращенный объект обмена данных.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "mainDispatcher", ignore = true)
    @Mapping(target = "isInternal", expression = "java(source.isInternal())")
    @Named("short")
    ContractorDTO toShortDto(Contractor source);

    /**
     * Обновление контрагента.
     *
     * @param target целевой объект.
     * @param source исходный объект.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "mainDispatcher", ignore = true)
    @Mapping(target = "internal", source = "isInternal")
    void update(@MappingTarget Contractor target, NewContractorDTO source);

    /**
     * Преобразование в сообщение брокера.
     *
     * @param contractor      источник.
     * @param integrationType тип интеграции.
     * @return результат.
     */
    @Mapping(target = "deleted", source = "contractor.active", qualifiedByName = BooleanMapper.NEGATE)
    @Mapping(target = "integrationType", source = "integrationType")
    ContractorMessage toMessage(Contractor contractor, String integrationType);

    @Mapping(target = "deleted", source = "active", qualifiedByName = BooleanMapper.NEGATE)
    ru.sber.transport.contractor.messages.avro.ContractorMessage toAvroMessage(Contractor contractor);
}
