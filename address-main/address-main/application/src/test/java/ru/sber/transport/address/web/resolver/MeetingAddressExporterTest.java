package ru.sber.transport.address.web.resolver;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.use_cases.MeetingAddresses;
import ru.sber.transport.address.web.resolver.mapper.MeetingAddressFileMapperImpl;
import ru.sber.transport.address.web.resolver.model.MeetingAddressFileDTO;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.file_works.exporter.DataExporter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка экспортера")
class MeetingAddressExporterTest {

    private final MeetingAddresses addresses = mock(MeetingAddresses.class);

    private final EmployeeOrganizationFunction function = mock(EmployeeOrganizationFunction.class);

    private final DataExporter<MeetingAddressFileDTO> exporter = new MeetingAddressResolver(addresses, new MeetingAddressFileMapperImpl(), function);

    @Test
    @DisplayName("Экспорт")
    void test_export() {
        var expectedList = Instancio.createList(MeetingAddress.class);

        when(addresses.get()).thenReturn(expectedList);

        var actualList = exporter.exportData(Map.of(), new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti("jti").build()));

        assertThat(actualList).hasSameSizeAs(expectedList);

        for (var i = 0; i < expectedList.size(); i++) {
            var actual = actualList.get(i);
            var expected = expectedList.get(i);

            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
            assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
        }
    }

}