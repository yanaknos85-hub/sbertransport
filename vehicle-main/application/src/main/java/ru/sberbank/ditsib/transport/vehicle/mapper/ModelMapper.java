package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Model;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelDto;

import java.util.List;

@Mapper(componentModel = "spring", imports = BrandMapper.class)
public interface ModelMapper {
    
    ModelDto modelToModelDto(Model source);
    
    List<ModelDto> listModelToListModelDto(List<Model> sourceList);
}
