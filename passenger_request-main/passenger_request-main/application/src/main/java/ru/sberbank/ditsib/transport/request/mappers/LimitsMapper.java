package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationDto;
import ru.sberbank.ditsib.transport.request.messaging.message.LimitActionMessage;

/**
 * Маппер объектов лимитов.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LimitsMapper {

    @Mapping(target = "action", constant = "SPEND")
    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "sum", source = "sum")
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "transportType", source = "transportType")
    @Mapping(target = "coop", source = "isCoop")
    @Mapping(target = "driver", source = "isDriver")
    @Mapping(target = "moneySaved", source = "moneySaved")
    @Mapping(target = "humanReadableId", source = "request.humanReadableId")
    LimitActionMessage toSpendMessage(
            Request request, Integer sum, boolean isCoop, boolean isDriver, TransportTypeEnum transportType,
            Integer moneySaved
                                     );
    
    
    LimitReservationDto toModel(LimitActionMessage reservation);
}
