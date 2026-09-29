package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.request.messaging.message.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationResultDto;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LimitReservationResultMapper {
    
    LimitReservationResultDto toModel(LimitActionResultMessage source);
    
}
