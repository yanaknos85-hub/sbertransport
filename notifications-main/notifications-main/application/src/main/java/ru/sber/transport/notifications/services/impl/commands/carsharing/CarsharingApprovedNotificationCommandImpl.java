package ru.sber.transport.notifications.services.impl.commands.carsharing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSender;

import java.util.EnumMap;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CarsharingApprovedNotificationCommandImpl implements NotificationCommand<TripApprove> {

    private static final String CARSHARING_NOTIFICATION_MESSAGE = "Перейдите в приложение для каршеринга";

    private final EmployeeService employeeService;

    private final NotificationSender notificationSender;

    private final NotificationService notificationService;

    private final ObjectMapper objectMapper;

    @Override
    public boolean validate(TripApprove previous, TripApprove current) {
        return current != null
                && ApproveStatus.APPROVED.equals(current.getApproveStatus())
                && TransportTypeEnum.CARSHARING.equals(current.getRequest().getTransportType());
    }

    @Override
    public void sendNotification(TripApprove current) throws JsonProcessingException {
        var request = current.getRequest();
        var passenger = employeeService.get(request.getPassengerId()).orElseThrow();
        var messages = new EnumMap<ChannelType, String>(ChannelType.class);
        var notification = new Notification();
        notification.setReceiverId(passenger.getId());
        notification.setEntity(objectMapper.writeValueAsString(current));
        var saved = notificationService.save(notification);
        messages.put(ChannelType.PUSH, CARSHARING_NOTIFICATION_MESSAGE);
        notificationSender.send(saved.getId(), passenger, NotificationType.TRANSPORT_BOOKING, messages, null);
        saved.setSent(true);
        notificationService.save(saved);
    }
}
