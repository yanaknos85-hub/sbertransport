package ru.sber.transport.authentication.providers.grpc.notification.impl;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.dto.SendingChannel;
import ru.sber.transport.authentication.providers.grpc.notification.mapper.NotificationsGrpcMapper;
import ru.sber.transport.authentication.providers.grpc.notification.mapper.NotificationsGrpcMapperImpl;
import ru.sber.transport.authentication.web.exceptions.NotificationsGrpcExceptionFactory;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsGrpc;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsOuterClass;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Slf4j
@DisplayName("Проверка отправки уведомлений по grpc")
class NotificationsGrpcClientImplTest {

    @Rule
    public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

    private final NotificationsGrpcMapper notificationsGrpcMapper = new NotificationsGrpcMapperImpl();

    @Test
    @SneakyThrows
    @DisplayName("Успешная отправка уведомления")
    void testSuccessSend() {
        // given
        var serverName = InProcessServerBuilder.generateName();
        var userId = UUID.randomUUID();
        var template = "{code}";
        var data = Map.of(
                "string", "string",
                "long", 1L,
                "int", 1,
                "double", 1.0D,
                "list", List.of(1)
        );

        grpcCleanupRule.register(InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(createNotifications(null)).build().start());

        var channel = grpcCleanupRule.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        var stub = NotificationsGrpc.newBlockingStub(channel);

        var client = new NotificationsGrpcClientImpl(notificationsGrpcMapper);
        ReflectionTestUtils.setField(client, "blockingStub", stub);

        assertDoesNotThrow(() -> client.send(userId, template, data, SendingChannel.EMAIL));
    }

    @ParameterizedTest
    @MethodSource("errorSend")
    @SneakyThrows
    @DisplayName("Отправка уведомления с ошибкой")
    void testErrorSend(StatusRuntimeException exception, Class<? extends RuntimeException> exceptionClass) {
        // given
        var serverName = InProcessServerBuilder.generateName();
        var userId = UUID.randomUUID();
        var template = "{code}";
        var data = Map.of(
                "string", "string",
                "long", 1L,
                "int", 1,
                "double", 1.0D,
                "list", List.of(1)
        );

        grpcCleanupRule.register(InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(createNotifications(exception)).build().start());

        var channel = grpcCleanupRule.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        var stub = NotificationsGrpc.newBlockingStub(channel);

        var client = new NotificationsGrpcClientImpl(notificationsGrpcMapper);
        ReflectionTestUtils.setField(client, "blockingStub", stub);

        assertThatThrownBy(() -> client.send(userId, template, data, SendingChannel.EMAIL))
                .isInstanceOf(exceptionClass);
    }

    private static Stream<Arguments> errorSend() {
        return Stream.of(
                Arguments.of(Status.NOT_FOUND.asRuntimeException(), NotificationsGrpcExceptionFactory.NotificationsGrpcNotFoundException.class),
                Arguments.of(Status.PERMISSION_DENIED.asRuntimeException(), NotificationsGrpcExceptionFactory.NotificationsGrpcForbiddenException.class),
                Arguments.of(Status.FAILED_PRECONDITION.asRuntimeException(), NotificationsGrpcExceptionFactory.NotificationsGrpcPreconditionFailedException.class),
                Arguments.of(Status.ABORTED.asRuntimeException(), StatusRuntimeException.class)
        );
    }

    private NotificationsGrpc.NotificationsImplBase createNotifications(StatusRuntimeException exception) {
        return mock(NotificationsGrpc.NotificationsImplBase.class, delegatesTo(
                new NotificationsGrpc.NotificationsImplBase() {

                    @Override
                    public void send(NotificationsOuterClass.NotificationData request, StreamObserver<Empty> responseObserver) {
                        if (exception != null) {
                            responseObserver.onError(exception);
                            return;
                        }

                        responseObserver.onNext(Empty.getDefaultInstance());

                        responseObserver.onCompleted();
                    }
                }
        ));
    }
}