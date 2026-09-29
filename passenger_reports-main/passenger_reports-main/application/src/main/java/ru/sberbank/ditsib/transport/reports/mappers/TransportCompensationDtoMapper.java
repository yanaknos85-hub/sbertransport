package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.model.TransportCompensation;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportCompensationDtoMapper {
    
    @Mapping(target = "requestId", source = "request.id")
    TransportCompensationDTO mapToTransportCompensationDto(TransportCompensation transportCompensation);
    
    List<TransportCompensationDTO> mapToTransportCompensationDtoList(List<TransportCompensation> transportCompensation);
}
