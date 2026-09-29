package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.AccessiblePosition;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.AccessiblePositionDto;

@Mapper(componentModel = "spring")
public interface AccessiblePositionMapper {

    AccessiblePositionDto accessiblePositionToAccessiblePositionDto(AccessiblePosition source);
}
