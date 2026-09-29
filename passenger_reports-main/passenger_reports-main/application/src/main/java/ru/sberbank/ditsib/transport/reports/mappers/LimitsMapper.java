package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;
import ru.sberbank.ditsib.transport.messaging.messages.LimitMessage;
import ru.sberbank.ditsib.transport.reports.dto.LimitShortDTO;
import ru.sberbank.ditsib.transport.reports.model.Limit;

import java.util.Optional;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LimitsMapper {

    @Mapping(target = "departmentLimitId", source = "limitId")
    Limit fromMessage(LimitMessage message);

    @Mapping(target = "id", ignore = true)
    Limit update(Limit source, @MappingTarget Limit target);

    default LimitStatus stringToLimitStatus(String limitStatus) {
        return Optional.ofNullable(limitStatus).map(LimitStatus::valueOf).orElse(null);
    }

    default LimitSharingType stringToLimitSharingType(String limitSharingType) {
        return Optional.ofNullable(limitSharingType).map(LimitSharingType::valueOf).orElse(null);
    }

    default LimitServiceType stringToLimitServiceType(String limitServiceType) {
        return Optional.ofNullable(limitServiceType).map(LimitServiceType::valueOf).orElse(null);
    }

    default LimitType stringToLimitType(String limitType) {
        return Optional.ofNullable(limitType).map(LimitType::valueOf).orElse(null);
    }

    default TransportTypeEnum stringTransportType(String transportType) {
        return TransportTypeEnum.getByName(transportType).orElse(null);
    }

    LimitShortDTO toShortDto(Limit limit);
}
