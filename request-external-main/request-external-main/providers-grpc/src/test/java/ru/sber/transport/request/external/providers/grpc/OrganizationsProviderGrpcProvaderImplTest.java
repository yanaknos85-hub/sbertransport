package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Organization;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера организаций")
class OrganizationsProviderGrpcProvaderImplTest {

    private final OrganizationsGrpc.OrganizationsBlockingStub stub = mock(OrganizationsGrpc.OrganizationsBlockingStub.class);

    private final OrganizationsProvider organizationsProvider = new OrganizationsProviderGrpcProvaderImpl(stub);

    @Test
    @DisplayName("Сохранение данных")
    void test_save() {
        try {
            organizationsProvider.save(Instancio.create(TestOrganization.class));
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC saving is not implemented");
        }
    }

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var digitId = Instancio.create(Integer.class);

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(id.toString()).setDigitId(digitId).build());

        final var actual = organizationsProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDigitId()).isEqualTo((long) digitId);
    }

    private record TestOrganization(UUID getId, long getDigitId, List<String> getAvailableClasses) implements Organization {}

}