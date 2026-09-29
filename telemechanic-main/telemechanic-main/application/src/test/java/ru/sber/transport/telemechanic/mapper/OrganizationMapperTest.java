package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.OrganizationDto;

import java.util.Collections;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@DisplayName("Тест маппера подразделений")
class OrganizationMapperTest {
    
    private final OrganizationMapper mapper = Mappers.getMapper(OrganizationMapper.class);
    
    @Test
    void organizationMessageToOrganization() {
        var source = Instancio.of(OrganizationMessage.class)
                              .set(field(OrganizationMessage::getContacts), Collections.emptyList())
                              .create();
        var expected = new Organization(source.getId(),
                                        source.getDigitId(),
                                        source.getOfficialName(),
                                        source.getMsrn(),
                                        source.getTid(),
                                        source.getOrganizationGroup().id(),
                                        true,
                                        emptySet(),
                                        null, null);
        var actual = mapper.organizationMessageToOrganization(source);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(mapper.organizationMessageToOrganization(null)).isNull();
    }
    
    @Test
    void organizationToOrganizationDto() {
        var source = Instancio.create(Organization.class);
        var expected = new OrganizationDto(source.getId(), source.getOfficialName());
        var actual = mapper.organizationToOrganizationDto(source);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(mapper.organizationToOrganizationDto(null)).isNull();
    }
}