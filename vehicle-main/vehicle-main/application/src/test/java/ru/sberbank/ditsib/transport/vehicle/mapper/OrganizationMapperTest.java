package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Тест маппера подразделений")
class OrganizationMapperTest {

    private final OrganizationMapper mapper = Mappers.getMapper(OrganizationMapper.class);

    @Test
    void fromMessage() {
        var message = new OrganizationMessage();
        message.setId(UUID.randomUUID());
        message.setDeleted(false);
        message.setAddress("address");
        message.setContacts(Collections.emptyList());
        message.setDigitId(11L);
        message.setMsrn("msrn");
        message.setOfficialName("officialName");
        message.setTid("tid");
        var actual = mapper.organizationMessageToOrganization(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getDigitId(), message.getDigitId());
        assertEquals(actual.getOfficialName(), message.getOfficialName());
    }

    @Test
    void organizationToOrganizationDto() {
        var organization = Instancio.create(Organization.class);
        var expected = new OrganizationDto(organization.getId(), organization.getOfficialName(), organization.getDigitId());
        var actual = mapper.organizationToOrganizationDto(organization);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void organizationToOrganizationUUIDs() {
        var organizations = Instancio.ofSet(Organization.class).create();
        var result = mapper.organizationsToOrganizationUUIDs(organizations);

        assertEquals(result, organizations.stream().map(Organization::getId).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @MethodSource("organizationProvider")
    void organizationToOrganizationUUID(Organization organization) {
        var result = mapper.organizationToOrganizationUUID(organization);

        if (organization == null) {
            assertNull(result);
        } else assertEquals(result, organization.getId());
    }

    static Stream<Organization> organizationProvider() {
        return Stream.of(Instancio.create(Organization.class), null);
    }
}