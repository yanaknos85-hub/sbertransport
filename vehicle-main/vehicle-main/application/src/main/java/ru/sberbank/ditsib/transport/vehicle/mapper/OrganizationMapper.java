package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.DepartmentInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationNameWithDepartmentInfo;

import java.util.Set;
import java.util.UUID;

/**
 * Маппер организаций.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    Organization organizationMessageToOrganization(OrganizationMessage source);

    OrganizationDto organizationToOrganizationDto(Organization source);

    @Mapping(target = "id", source = "departmentId")
    DepartmentInfoDto organizationWithDepartmentIntoDto(OrganizationNameWithDepartmentInfo model);

    Set<UUID> organizationsToOrganizationUUIDs(Set<Organization> organizations);

    default UUID organizationToOrganizationUUID(Organization organization) {
        return organization != null ? organization.getId() : null;
    }

}
