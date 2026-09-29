package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.NullValue;
import io.qameta.allure.Feature;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Employee;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера сотрудников")
class EmployeeImplTest {

    private final EmployeesGrpc.EmployeesBlockingStub stub = mock(EmployeesGrpc.EmployeesBlockingStub.class);

    private final EmployeesProvider employeesProvider = new EmployeesGrpcProviderImpl(stub);

    @Test
    @DisplayName("Сохранение данных")
    void test_save() {
        try {
            employeesProvider.save(Instancio.create(TestEmployee.class));
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC does not support this method");
        }
    }

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var lastName = Instancio.create(String.class);
        final var firstName = Instancio.create(String.class);
        final var patronymic = Instancio.create(String.class);

        final var response = OrganizationsOuterClass.Employee.newBuilder()
                .setId(id.toString())
                .setDepartmentId(departmentId.toString())
                .setOrganizationId(organizationId.toString())
                .setPositionId(positionId.toString())
                .setLastName(lastName)
                .setFirstName(firstName)
                .setPatronymic(OrganizationsOuterClass.NullableString.newBuilder().setValue(patronymic).build())
                .build();

        when(stub.one(any())).thenReturn(response);

        final var actual = employeesProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDepartmentId()).isEqualTo(departmentId);
        assertThat(actual.getOrganizationId()).isEqualTo(organizationId);
        assertThat(actual.getLastName()).isEqualTo(lastName);
        assertThat(actual.getFirstName()).isEqualTo(firstName);
        assertThat(actual.getPatronymic()).isEqualTo(patronymic);
        assertThat(actual.getPositionId()).isEqualTo(positionId);
    }

    @Test
    @DisplayName("Получение данных. Нет отчества")
    void test_get_noPatronymic() {
        final var id = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var lastName = Instancio.create(String.class);
        final var firstName = Instancio.create(String.class);

        final var response = OrganizationsOuterClass.Employee.newBuilder()
                .setId(id.toString())
                .setDepartmentId(departmentId.toString())
                .setOrganizationId(organizationId.toString())
                .setPositionId(positionId.toString())
                .setLastName(lastName)
                .setFirstName(firstName)
                .setPatronymic(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .build();

        when(stub.one(any())).thenReturn(response);

        final var actual = employeesProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDepartmentId()).isEqualTo(departmentId);
        assertThat(actual.getOrganizationId()).isEqualTo(organizationId);
        assertThat(actual.getLastName()).isEqualTo(lastName);
        assertThat(actual.getFirstName()).isEqualTo(firstName);
        assertThat(actual.getPatronymic()).isNull();
        assertThat(actual.getPositionId()).isEqualTo(positionId);
    }

    private record TestEmployee(UUID getId, UUID getDepartmentId, UUID getOrganizationId, UUID getPositionId,
                                String getFirstName, String getLastName, String getPatronymic,
                                String getPersonnelNumber, String getCostCenter) implements Employee {
    }

}