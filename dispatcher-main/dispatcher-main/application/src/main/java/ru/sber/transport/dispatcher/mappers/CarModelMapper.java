package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.dispatcher.dto.CarModelDto;
import ru.sber.transport.dispatcher.database.model.CarModel;

@Mapper
public interface CarModelMapper {

    CarModel toModel(CarModelDto source);

}
