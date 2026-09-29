package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.messaging.listener.message.FleetOwnerOrganizationMessage;

/**
 * Маппер организаций владельцев автопарков.
 */
@Mapper(componentModel = "spring")
public interface FleetOwnerOrganizationMapper {
    
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(source = "organization", target = "organization")
    @Mapping(source = "message.edfOperatorId", target = "edfOperatorId")
    @Mapping(source = "message.edfCode", target = "edfCode")
    @Mapping(source = "active", target = "active")
    FleetOwnerOrganization fleetOwnerOrganizationMessageToFleetOwnerOrganization(FleetOwnerOrganizationMessage message,
                                                                                 Organization organization,
                                                                                 boolean active);
}
