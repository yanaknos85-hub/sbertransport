package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.WheelSize;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WheelSizeMapper {
    WheelSizeDto wheeSizeToWheelSizeDto(WheelSize source);

    List<WheelSizeDto> listWheelSizeToListWheelSizeDto(List<WheelSize> source);
}
