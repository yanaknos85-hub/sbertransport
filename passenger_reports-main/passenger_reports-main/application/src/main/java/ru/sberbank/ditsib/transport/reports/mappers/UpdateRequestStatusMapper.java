package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.reports.messaging.messages.UpdateTripRequestStatusMessage;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UpdateRequestStatusMapper {
    @Mapping(target = "id", source = "requestId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "userId", source = "userId")
    UpdateTripRequestStatusMessage toMessage(UUID requestId, String status, LocalDateTime dateTime, String userId);
}
