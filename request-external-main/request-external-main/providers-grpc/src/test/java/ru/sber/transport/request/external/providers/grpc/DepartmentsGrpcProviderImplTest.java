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
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера подразделений")
class DepartmentsGrpcProviderImplTest {

    private final DepartmentsGrpc.DepartmentsBlockingStub stub = mock(DepartmentsGrpc.DepartmentsBlockingStub.class);

    private final DepartmentsProvider departmentsProvider = new DepartmentsGrpcProviderImpl(stub);

    @Test
    @DisplayName("Сохранение данных")
    void test_save() {
        try {
            departmentsProvider.save(Instancio.create(TestDepartment.class));
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC does not support this method");
        }
    }

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var headId = UUID.randomUUID();

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(id.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setValue(headId.toString()).build()).build());

        final var actual = departmentsProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getHeadId()).isEqualTo(headId);
    }

    @Test
    @DisplayName("Получение данных без руководителя")
    void test_get_noHead() {
        final var id = UUID.randomUUID();

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(id.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());

        final var actual = departmentsProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getHeadId()).isNull();
    }

    @Test
    @DisplayName("Получение дочерних подразделений")
    void test_getChildrenDepartments() {
        try {
            departmentsProvider.getChildrenDepartments(Instancio.createSet(UUID.class));
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC does not support this method");
        }
    }

    @Test
    @DisplayName("Получение подразделений по организации, статусу и уровню")
    void test_findDepartmentsByOrganizationIdAndStatusAndLevelBetween() {
        try {
            departmentsProvider.findDepartmentsByOrganizationIdAndStatusAndLevelBetween(
                    Instancio.create(UUID.class),
                    Instancio.create(DepartmentStatus.class),
                    Instancio.create(Integer.class),
                    Instancio.create(Integer.class));
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC does not support this method");
        }
    }


    private record TestDepartment(UUID getId, UUID getHeadId, UUID getParentId, String getName, DepartmentStatus getStatus, UUID getOrganizationId, Integer getLevel) implements Department {}

}