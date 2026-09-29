package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchDto;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MedicRequestMapper {
    
    MedicRequestSearchDto medicRequestAllOrganizationsToMedicRequestSearchDto(MedicRequestRegistryAllOrganizationsRequest source);
    
    @Mapping(target = "organizationId", source = "organizationId")
    MedicRequestSearchDto medicRequestSelfOrganizationToMedicRequestSearchDto(MedicRequestRegistrySelfOrganizationRequest source,
                                                                              UUID organizationId);
}
