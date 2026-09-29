package ru.sber.transport.etrn.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты маппера OrganizationMapper")
class OrganizationMapperTest {

    private OrganizationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrganizationMapperImpl();
    }

    @Test
    @DisplayName("fromMessage — маппинг полей")
    void fromMessage_mapsFields() {
        // Arrange
        UUID orgId = UUID.randomUUID();
        OrganizationMessage message = new OrganizationMessage(
                orgId,
                1L,
                "ООО Тестовая организация",
                "г. Москва, ул. Тестовая, д. 1",
                null, null, null, null, false,
                null);

        // Act
        var result = mapper.fromMessage(message);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orgId);
        assertThat(result.getOfficialName()).isEqualTo("ООО Тестовая организация");
        assertThat(result.getAddress()).isEqualTo("г. Москва, ул. Тестовая, д. 1");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("fromMessage — маппинг с organizationGroup")
    void fromMessage_mapsWithGroup() {
        // Arrange
        UUID orgId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        var group = new OrganizationMessage.OrganizationGroup(groupId, "Внутренние", true);
        OrganizationMessage message = new OrganizationMessage(
                orgId,
                1L,
                "ООО Группа",
                "г. Москва",
                null, null, null, null, false,
                group);

        // Act
        var result = mapper.fromMessage(message);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrganizationGroup()).isNotNull();
        assertThat(result.getOrganizationGroup().getId()).isEqualTo(groupId);
    }
}
