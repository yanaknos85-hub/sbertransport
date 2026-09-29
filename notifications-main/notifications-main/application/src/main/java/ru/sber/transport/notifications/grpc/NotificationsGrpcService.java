package ru.sber.transport.notifications.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.lang.Nullable;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.HasPhone;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.mapper.GrpcMapper;
import ru.sber.transport.notifications.services.NotificationContactService;
import ru.sber.transport.notifications.services.NotificationSender;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsGrpc;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsOuterClass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class NotificationsGrpcService extends NotificationsGrpc.NotificationsImplBase {

    private final GrpcMapper grpcMapper;

    private final NotificationSender notificationSender;

    private final NotificationContactService notificationContactService;

    @Override
    public void send(NotificationsOuterClass.NotificationData request,
                     StreamObserver<Empty> responseObserver) {

        log.info("Sending notification to {} by channel {}", request.getUserId(), request.getChannel());
        try {
            var channel = grpcMapper.mapChannel(request.getChannel());
            var user = UUID.fromString(request.getUserId());

            var contact = notificationContactService.findContactDataById(user)
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("User not found " + user)
                            .asRuntimeException());

            if (!channelSupported(channel, contact)) {
                throw Status.FAILED_PRECONDITION
                        .withDescription("Channel not supported")
                        .asRuntimeException();
            }

            if (contact instanceof HasPhone hasPhone && channel == ChannelType.SMS && !phoneConfirmed(hasPhone)) {
                throw Status.PERMISSION_DENIED
                        .withDescription("Phone is not confirmed")
                        .asRuntimeException();
            }

            var message = Map.of(channel, request.getTemplate());
            var data = new HashMap<String, Object>();
            data.put("code", request.getDataMap().get("code").getInt());
            notificationSender.send(UUID.randomUUID(), contact, NotificationType.CUSTOM_NOTIFICATION, message, data);

            responseObserver.onNext(Empty.getDefaultInstance());

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Responding failed", e);
            responseObserver.onError(e);
        }
    }

    private boolean channelSupported(@Nullable ChannelType channel, HasContactData contact) {
        return channel != null && channel.getContactClass().isAssignableFrom(contact.getClass());
    }


    private boolean phoneConfirmed(HasPhone hasPhone) {
        return hasPhone.getPhone() != null && hasPhone.isPhoneConfirmed();
    }
}
