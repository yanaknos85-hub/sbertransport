package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Region;
import ru.sber.transport.telemechanic.dto.RegionDto;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    @Mapping(target = "title", expression = "java(region.getName() + \" (\" + region.getCode() + ')')")
    RegionDto mapRegionToDto(Region region);
}
