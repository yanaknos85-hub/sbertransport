package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Маппер организаций.
 */
@Mapper
public interface OrganizationMapper {

    Organization organizationMessageToOrganization(OrganizationMessage source);
}
