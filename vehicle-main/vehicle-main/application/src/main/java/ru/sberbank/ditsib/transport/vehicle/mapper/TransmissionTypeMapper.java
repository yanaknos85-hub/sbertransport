package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.TransmissionType;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransmissionTypeMapper {
    TransmissionTypeDto transmissionTypeToTransmissionTypeDto(TransmissionType source);

    List<TransmissionTypeDto> listTransmissionTypeToListTransmissionTypeDto(List<TransmissionType> source);
}
