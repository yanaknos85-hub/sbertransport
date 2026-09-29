package ru.sber.transport.notifications.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.ContactWithEmail;
import ru.sber.transport.notifications.database.model.ContactWithPhoneAndEmail;
import ru.sber.transport.notifications.mapper.GrpcMapper;
import ru.sber.transport.notifications.mapper.GrpcMapperImpl;
import ru.sber.transport.notifications.services.NotificationContactService;
import ru.sber.transport.notifications.services.NotificationSender;
import ru.sber.transport.notifications.services.impl.NotificationContactServiceImpl;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsOuterClass;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправки нотификаций по grpc")
class NotificationsGrpcTest {

    private final NotificationSender sender = mock(NotificationSender.class);

    private final GrpcMapper grpcMapper = new GrpcMapperImpl();

    private final NotificationContactService notificationsContactService = mock(NotificationContactServiceImpl.class);

    private final NotificationsGrpcService grpc = new NotificationsGrpcService(grpcMapper, sender, notificationsContactService);

    @Test
    @DisplayName("Отправка нотификации")
    void testSend() {
        var actualList = new ArrayList<Empty>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var userId = UUID.randomUUID();
        var contact = Instancio.of(ContactWithPhoneAndEmail.class)
                .set(field(ContactWithPhoneAndEmail::isPhoneConfirmed), true)
                .create();

        when(notificationsContactService.findContactDataById(userId))
                .thenReturn(Optional.of(contact));

        NotificationsOuterClass.Object data = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        NotificationsOuterClass.Object code = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        grpc.send(NotificationsOuterClass.NotificationData.newBuilder()
                .setUserId(userId.toString())
                .setChannel(NotificationsOuterClass.Channel.SMS)
                .setTemplate("test message {data}, {code}")
                .putAllData(Map.of(
                        "data", data,
                        "code", code
                ))
                .build(), new StreamObserver<>() {

            @Override
            public void onNext(Empty value) {
                actualList.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(actualList).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        verify(sender, times(1))
                .send(any(), any(), any(), any(), any());

    }

    @Test
    @DisplayName("Отправка нотификации. Пользователь не найден")
    void testSendUserNotFound() {
        var actualList = new ArrayList<Empty>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var userId = UUID.randomUUID();

        when(notificationsContactService.findContactDataById(userId))
                .thenReturn(Optional.empty());

        NotificationsOuterClass.Object data = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        NotificationsOuterClass.Object code = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        grpc.send(NotificationsOuterClass.NotificationData.newBuilder()
                .setUserId(userId.toString())
                .setChannel(NotificationsOuterClass.Channel.SMS)
                .setTemplate("test message {data}, {code}")
                .putAllData(Map.of(
                        "data", data,
                        "code", code
                ))
                .build(), new StreamObserver<>() {

            @Override
            public void onNext(Empty value) {
                actualList.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(actualList).isEmpty();
        assertThat(error.get()).isNotNull();
        assertThat(completed.get()).isFalse();
        assertThat(error.get())
                .asInstanceOf(InstanceOfAssertFactories.type(StatusRuntimeException.class))
                .satisfies(ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode()));

        verify(sender, never())
                .send(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Отправка нотификации. Телефон не подтвержден")
    void testSendPhoneNotConfirmed() {
        var actualList = new ArrayList<Empty>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var userId = UUID.randomUUID();

        var contact = Instancio.of(ContactWithPhoneAndEmail.class)
                .set(field(ContactWithPhoneAndEmail::isPhoneConfirmed), false)
                .create();

        when(notificationsContactService.findContactDataById(userId))
                .thenReturn(Optional.of(contact));

        NotificationsOuterClass.Object data = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        NotificationsOuterClass.Object code = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        grpc.send(NotificationsOuterClass.NotificationData.newBuilder()
                .setUserId(userId.toString())
                .setChannel(NotificationsOuterClass.Channel.SMS)
                .setTemplate("test message {data}, {code}")
                .putAllData(Map.of(
                        "data", data,
                        "code", code
                ))
                .build(), new StreamObserver<>() {

            @Override
            public void onNext(Empty value) {
                actualList.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(actualList).isEmpty();
        assertThat(error.get()).isNotNull();
        assertThat(completed.get()).isFalse();
        assertThat(error.get())
                .asInstanceOf(InstanceOfAssertFactories.type(StatusRuntimeException.class))
                .satisfies(ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.PERMISSION_DENIED.getCode()));

        verify(sender, never())
                .send(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Отправка нотификации. Нет телефона")
    void testChannelNotSupported() {
        var actualList = new ArrayList<Empty>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var userId = UUID.randomUUID();

        var contact = Instancio.of(ContactWithEmail.class).create();

        when(notificationsContactService.findContactDataById(userId))
                .thenReturn(Optional.of(contact));

        NotificationsOuterClass.Object data = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        NotificationsOuterClass.Object code = NotificationsOuterClass.Object.newBuilder()
                .setString("test data")
                .build();

        grpc.send(NotificationsOuterClass.NotificationData.newBuilder()
                .setUserId(userId.toString())
                .setChannel(NotificationsOuterClass.Channel.SMS)
                .setTemplate("test message {data}, {code}")
                .putAllData(Map.of(
                        "data", data,
                        "code", code
                ))
                .build(), new StreamObserver<>() {

            @Override
            public void onNext(Empty value) {
                actualList.add(value);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(actualList).isEmpty();
        assertThat(error.get()).isNotNull();
        assertThat(completed.get()).isFalse();
        assertThat(error.get())
                .asInstanceOf(InstanceOfAssertFactories.type(StatusRuntimeException.class))
                .satisfies(ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.FAILED_PRECONDITION.getCode()));

        verify(sender, never())
                .send(any(), any(), any(), any(), any());
    }

}