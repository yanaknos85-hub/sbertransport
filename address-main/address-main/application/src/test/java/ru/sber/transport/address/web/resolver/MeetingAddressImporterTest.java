package ru.sber.transport.address.web.resolver;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.use_cases.MeetingAddresses;
import ru.sber.transport.address.web.resolver.mapper.MeetingAddressFileMapperImpl;
import ru.sber.transport.address.web.resolver.model.MeetingAddressFileDTO;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.file_works.exceptions.ParsingFailedException;
import ru.sber.transport.file_works.importer.DataImporter;

import javax.naming.OperationNotSupportedException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка импортера")
class MeetingAddressImporterTest {

    private final MeetingAddresses addresses = mock(MeetingAddresses.class);

    private final EmployeeOrganizationFunction function = mock(EmployeeOrganizationFunction.class);

    private final DataImporter<MeetingAddressFileDTO> importer = new MeetingAddressResolver(addresses, new MeetingAddressFileMapperImpl(), function);

    @Test
    @DisplayName("Проверка импорта")
    void test_import() throws OperationNotSupportedException, ParsingFailedException {
        var expected = Instancio.create(MeetingAddressFileDTO.class);

        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(function.apply(userId)).thenReturn(organizationId);

        importer.importData(expected, Map.of(), new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        var actualCaptor = ArgumentCaptor.forClass(MeetingAddress.class);
        verify(addresses).save(eq(organizationId), actualCaptor.capture());

        assertThat(actualCaptor.getValue()).isNotNull();

        var actual = actualCaptor.getValue();
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
