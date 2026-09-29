package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.messaging.listener.message.FleetOwnerOrganizationMessage;

import static org.assertj.core.api.Assertions.assertThat;

class FleetOwnerOrganizationMapperTest {
    
    private final FleetOwnerOrganizationMapper fleetOwnerOrganizationMapper = Mappers.getMapper(FleetOwnerOrganizationMapper.class);
    
    @Test
    void transportToGetTransportResponse() {
        var message = Instancio.create(FleetOwnerOrganizationMessage.class);
        var organization = Instancio.create(Organization.class);
        var expected = new FleetOwnerOrganization(
                null,
                organization,
                message.edfOperatorId(),
                message.edfCode(),
                true
        );
        var actual = fleetOwnerOrganizationMapper.fleetOwnerOrganizationMessageToFleetOwnerOrganization(message,
                                                                                                        organization,
                                                                                                        true);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(fleetOwnerOrganizationMapper.fleetOwnerOrganizationMessageToFleetOwnerOrganization(null,
                                                                                                      null,
                                                                                                      true)).isNull();
    }
    
}