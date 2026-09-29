package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.sberbank.ditsib.transport.vehicle.database.model.Attorney;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyDto;

import java.util.List;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = EmployeeMapper.class,
        builder = @Builder(disableBuilder = true))
public interface AttorneyMapper {

    AttorneyDto mapAttorneyToAttorneyDto(Attorney source);

    List<AttorneyDto> mapAttorneyListToAttorneyDtoList(List<Attorney> sourceList);

}
