package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.limits.grpc.service.Limits;
import ru.sber.transport.request.external.providers.exceptions.ReserveException;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера лимитов")
class LimitsGrpcProviderImplTest {

    private LimitsGrpcProviderImpl limits;

    @RegisterExtension
    private final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();

    public static Stream<Arguments> statusSource() {
        return Stream.of(
            Arguments.of(Limits.Status.DATA_NOT_FOUND, ReserveException.Type.DATA_NOT_FOUND),
            Arguments.of(Limits.Status.NOT_SUFFICIENT, ReserveException.Type.NOT_SUFFICIENT),
            Arguments.of(Limits.Status.SERVICE_NOT_AVAILABLE, ReserveException.Type.SERVICE_NOT_AVAILABLE),
            Arguments.of(Limits.Status.TYPE_NOT_AVAILABLE, ReserveException.Type.TYPE_NOT_AVAILABLE)
        );
    }

    @Test
    @DisplayName("Проверка успешной резервации")
    void test_reserve() throws IOException {
        final var result = new AtomicReference<Limits.ReserveRequest>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();
        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        result.set(reserveRequest);
                        responseObserver.onNext(Limits.Response.newBuilder().setStatus(Limits.Status.OK).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        completed.set(true);
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        final var channel = grpcCleanupExtension.addService(service);

        limits = new LimitsGrpcProviderImpl(LimitServiceGrpc.newStub(channel), "service", "type");

        final var cost = Instancio.of(BigDecimal.class).create();
        final var data = Instancio.of(TestOrderData.class).create();

        limits.reserve(data, it -> cost);

        channel.shutdownNow();

        assertThat(result.get().getId()).isEqualTo(data.getId().toString());
        assertThat(result.get().getCost().getIntegerPart()).isEqualTo(cost.intValue());
        assertThat(result.get().getService()).isEqualTo("service");
        assertThat(result.get().getType()).isEqualTo("type");
        assertThat(result.get().getCost().getFractionPart()).isEqualTo(
            cost.toPlainString().contains(".") ? Integer.parseInt(cost.toPlainString().split("\\.")[1]) : 0);
        assertThat(completed.get()).isTrue();
        assertThat(error.get()).isNull();
    }

    @MethodSource("statusSource")
    @ParameterizedTest
    @DisplayName("Проверка ошибок резервации")
    void test_reserve_errors(Limits.Status status, ReserveException.Type type) throws IOException {
        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setStatus(status).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        final var channel = grpcCleanupExtension.addService(service);

        limits = new LimitsGrpcProviderImpl(LimitServiceGrpc.newStub(channel), "service", "type");

        final var data = Instancio.of(TestOrderData.class).create();

        try {
            limits.reserve(data, it -> it.getPlanned().getCost());
            fail("ReserveException expected");
        } catch (ReserveException e) {
            assertThat(e.getType()).isEqualTo(type);
        }

        channel.shutdownNow();
    }

    @Test
    @DisplayName("Проверка успешной фиксации")
    void test_spend() throws IOException {
        final var result = new AtomicReference<Limits.FixRequest>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();
        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.FixRequest> confirm(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(Limits.FixRequest reserveRequest) {
                        result.set(reserveRequest);
                        responseObserver.onNext(Limits.Response.newBuilder().setStatus(Limits.Status.OK).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        completed.set(true);
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        final var channel = grpcCleanupExtension.addService(service);

        limits = new LimitsGrpcProviderImpl(LimitServiceGrpc.newStub(channel), "service", "type");

        final var id = UUID.randomUUID();

        limits.confirm(id);

        channel.shutdownNow();

        assertThat(result.get().getId()).isEqualTo(id.toString());
        assertThat(completed.get()).isTrue();
        assertThat(error.get()).isNull();
    }

    @Test
    @DisplayName("Проверка неожиданных ошибок")
    void test_spend_exception() throws IOException {
        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.FixRequest> confirm(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(Limits.FixRequest reserveRequest) {
                        responseObserver.onError(new RuntimeException("Test"));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        final var channel = grpcCleanupExtension.addService(service);

        limits = new LimitsGrpcProviderImpl(LimitServiceGrpc.newStub(channel), "service", "type");

        final var id = UUID.randomUUID();

        try {
            limits.confirm(id);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage(e.getMessage());
        }
    }

    @Test
    @DisplayName("Проверка успешной отмены")
    void test_cancel() throws IOException {
        final var result = new AtomicReference<Limits.FixRequest>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();
        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.FixRequest> cancel(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(Limits.FixRequest reserveRequest) {
                        result.set(reserveRequest);
                        responseObserver.onNext(Limits.Response.newBuilder().setStatus(Limits.Status.OK).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        completed.set(true);
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        final var channel = grpcCleanupExtension.addService(service);

        limits = new LimitsGrpcProviderImpl(LimitServiceGrpc.newStub(channel), "service", "type");

        final var id = UUID.randomUUID();

        limits.cancel(id);

        channel.shutdownNow();

        assertThat(result.get().getId()).isEqualTo(id.toString());
        assertThat(completed.get()).isTrue();
        assertThat(error.get()).isNull();
    }

    @Test
    @DisplayName("Проверка неизвестного статуса")
    void test_cancel_unknownStatus() throws IOException {
        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.FixRequest> cancel(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(Limits.FixRequest reserveRequest) {
                        responseObserver.onNext(
                            Limits.Response.newBuilder().setStatus(Limits.Status.UNRECOGNIZED).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        final var channel = grpcCleanupExtension.addService(service);

        limits = new LimitsGrpcProviderImpl(LimitServiceGrpc.newStub(channel), "service", "type");

        final var id = UUID.randomUUID();

        try {
            limits.cancel(id);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("io.grpc.StatusRuntimeException: UNKNOWN: Application error processing RPC");
        }

        channel.shutdownNow();
    }

}