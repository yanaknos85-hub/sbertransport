package ru.sberbank.ditsib.transport.request.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingInfo;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoResponseDTO;

@Mapper
public interface CarsharingDTOMapper {
    
    @Mapping(source = "deepLink", target = "deepLink")
    CarsharingInfoResponseDTO getCarsharingResponseDTO(CarsharingInfo carsharingInfo, String deepLink);
    
    
}
