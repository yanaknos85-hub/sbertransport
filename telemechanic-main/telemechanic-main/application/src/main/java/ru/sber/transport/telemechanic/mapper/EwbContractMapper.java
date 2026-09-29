package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.telemechanic.database.projection.EwbContractDetailsProjection;
import ru.sber.transport.telemechanic.dto.EwbContractDetails;

@Mapper(componentModel = "spring")
public interface EwbContractMapper {
    
    EwbContractDetails ewbContractDetailsProjectionToEwbContractDetails(EwbContractDetailsProjection source);
}
