package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.projection.ReportProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    
    ReportDto reportProjectionToReportDto(ReportProjection source);
    
    List<ReportDto> reportProjectionListToReportDtoList(List<ReportProjection> source);
}
