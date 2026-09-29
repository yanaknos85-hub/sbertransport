package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.OrganizationDto;

/**
 * Маппер организаций.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "tin", source = "tid")
    @Mapping(target = "organizationGroupId", source = "organizationGroup.id")
    @Mapping(target = "address", ignore = true)
    Organization organizationMessageToOrganization(OrganizationMessage source);
    
    OrganizationDto organizationToOrganizationDto(Organization source);
}
