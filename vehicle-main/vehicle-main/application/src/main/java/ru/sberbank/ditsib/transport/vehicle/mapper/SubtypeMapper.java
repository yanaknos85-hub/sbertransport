package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Subtype;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeDto;

import java.util.List;

@Mapper(componentModel = "spring", imports = TypeMapper.class)
public interface SubtypeMapper {
    
    SubtypeDto subtypeToSubtypeDto(Subtype subtype);
    
    List<SubtypeDto> listSubtypeToListSubtypeDto(List<Subtype> subtypeList);
}
