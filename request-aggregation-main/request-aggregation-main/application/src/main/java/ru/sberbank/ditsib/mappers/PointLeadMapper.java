package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.database.model.PointLead;
import ru.sberbank.ditsib.dto.point.PointLeadRequestDto;
import ru.sberbank.ditsib.dto.point.PointLeadResponseDto;

/**
 * Маппер точек маршрута заявки.
 */
@Mapper
public interface PointLeadMapper {

    @Mapping(target = "mainLead", ignore = true)
    @Mapping(target = "lead", ignore = true)
    @Mapping(target = "pointNumber", ignore = true)
    @Mapping(target = "id", ignore = true)
    PointLead toEntity(PointLeadRequestDto pointLeadDto);

    PointLeadResponseDto poitLeadToPointLeadResponseDto(PointLead pointLead);
}
