package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.google.protobuf.Empty;
import com.google.protobuf.NullValue;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.corporate.grpc.service.DelegatesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.request.external.model.Delegate;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера делегатов")
class DelegatesGrpcProviderImplTest {

    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanup = new GrpcCleanupExtension();

    private final List<OrganizationsOuterClass.Delegate> responses = new ArrayList<>();

    private final ManagedChannel channel = grpcCleanup.addService(new DelegatesGrpc.DelegatesImplBase() {

        @Override
        public void all(Empty request, StreamObserver<OrganizationsOuterClass.Delegate> responseObserver) {
            responses.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        }
    });

    private final DelegatesGrpc.DelegatesStub stub = DelegatesGrpc.newStub(channel);

    private final DelegatesProvider delegatesProvider = new DelegatesGrpcProviderImpl(stub);

    DelegatesGrpcProviderImplTest() throws IOException {
    }

    @AfterEach
    void afterEach() {
        responses.clear();
    }

    @Test
    @DisplayName("Сохранение данных")
    void test_save() {
        final var source = Instancio.create(TestDelegate.class);
        try {
            delegatesProvider.save(source);
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC does not support this method");
        }
    }

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var headId = Instancio.create(UUID.class);
        try {
            delegatesProvider.get(headId);
            fail("Должно бросить исключение");
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("gRPC does not support this method");
        }
    }

    @Test
    @DisplayName("Получение всех данных")
    void test_getAll() {
        responses.add(OrganizationsOuterClass.Delegate.newBuilder().setDelegateId(UUID.randomUUID().toString()).setSupervisorId(UUID.randomUUID().toString()).setStartDate(OrganizationsOuterClass.Date.newBuilder().setYear(1900).setMonth(1).setDay(1).build()).setEndDate(OrganizationsOuterClass.NullableDate.newBuilder().setValue(OrganizationsOuterClass.Date.newBuilder().setYear(1902).setMonth(2).setDay(2).build()).build()).setType("TAXI").build());
        responses.add(OrganizationsOuterClass.Delegate.newBuilder().setDelegateId(UUID.randomUUID().toString()).setSupervisorId(UUID.randomUUID().toString()).setStartDate(OrganizationsOuterClass.Date.newBuilder().setYear(1900).setMonth(3).setDay(1).build()).setEndDate(OrganizationsOuterClass.NullableDate.newBuilder().setNull(NullValue.NULL_VALUE)).setType("TAXI").build());
        responses.add(OrganizationsOuterClass.Delegate.newBuilder().setDelegateId(UUID.randomUUID().toString()).setSupervisorId(UUID.randomUUID().toString()).setStartDate(OrganizationsOuterClass.Date.newBuilder().setYear(1905).setMonth(3).setDay(1).build()).setEndDate(OrganizationsOuterClass.NullableDate.newBuilder().setNull(NullValue.NULL_VALUE)).setType("CADABRA").build());

        final var actualList = delegatesProvider.getAll();

        assertThat(actualList).hasSize(2);
        final var actual1 = actualList.getFirst();

        assertSoftly(id -> {
            id.assertThat(actual1.delegateId()).isNotNull();
            id.assertThat(actual1.supervisorId()).isNotNull();
            id.assertThat(actual1.startDate()).isEqualTo(LocalDate.of(1900, 1, 1));
            id.assertThat(actual1.endDate()).isEqualTo(LocalDate.of(1902, 2, 2));
        });

        final var actual2 = actualList.get(1);

        assertSoftly(id -> {
            id.assertThat(actual2.delegateId()).isNotNull();
            id.assertThat(actual2.supervisorId()).isNotNull();
            id.assertThat(actual2.startDate()).isEqualTo(LocalDate.of(1900, 3, 1));
            id.assertThat(actual2.endDate()).isNull();
        });
    }

    private record TestDelegate(UUID delegateId, UUID supervisorId, LocalDate startDate, LocalDate endDate, boolean active) implements Delegate {}


}