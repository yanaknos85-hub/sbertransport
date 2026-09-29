package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.contractor.messages.avro.DispatcherMessage;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.dto.DispatcherDto;
import ru.sber.transport.dispatcher.dto.DispatcherSelectDto;
import ru.sber.transport.dispatcher.dto.NewDispatcherDto;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;

import java.util.Objects;

/**
 * Маппер диспетчеров.
 */
@Mapper
public interface DispatcherMapper {

    /**
     * Обновление модели.
     *
     * @param target модель.
     * @param source источник.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "contractor", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "consent", ignore = true)
    @Mapping(target = "autopark", ignore = true)
    void update(@MappingTarget Dispatcher target, NewDispatcherDto source);

    /**
     * Преобразование в объект данных.
     *
     * @param saved источник.
     * @return результат.
     */
    @Mapping(target = "contractorId", source = "contractor.id")
    @Mapping(target = "autoassign", source = "contractor.autoassign")
    @Mapping(target = "autoparkId", source = "autopark.id")
    @Mapping(target = "autoparkName", source = "autopark.name")
    DispatcherDto toDto(Dispatcher saved);

    /**
     * Преобразование в объект данных.
     *
     * @param saved источник.
     * @return результат.
     */
    DispatcherSelectDto toSelectDto(Dispatcher saved);

    /**
     * Преобразование в объект сообщения.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "contractorId", source = "contractor.id")
    @Mapping(target = "autoparkId", source = "autopark.id")
    ContractorUpdateTripMessage.Dispatcher toMessage(Dispatcher source);

    @Mapping(target = "contractorId", source = "contractor.id")
    @Mapping(target = "patroymic", ignore = true)
    DispatcherMessage toAvroMessage(Dispatcher source);

    @BeforeMapping
    default void setPhoneConfirmed(@MappingTarget Dispatcher target, NewDispatcherDto newData) {
        target.setPhoneConfirmed(Objects.equals(target.getPhone(), newData.phone()));
    }

}
