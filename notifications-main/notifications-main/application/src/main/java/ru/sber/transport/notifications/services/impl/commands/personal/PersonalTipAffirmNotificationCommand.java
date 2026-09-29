package ru.sber.transport.notifications.services.impl.commands.personal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;

import java.util.List;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL;

/**
 * Соответствует уведомлению notice_308
 */
@Slf4j
@Service
public class PersonalTipAffirmNotificationCommand extends PersonalAbstractCommand {

    public PersonalTipAffirmNotificationCommand(DepartmentService departmentService, NotificationSettingsService notificationSettingsService, NotificationGlobalProcessor<TripRequest> processor, EmployeeService employeeService) {
        super(departmentService, notificationSettingsService, processor, employeeService);
    }

    @Override
    protected List<UUID> getReceiverIds(TripRequest request) {
        return List.of(request.getPassengerId());
    }

    @Override
    protected String getDescription() {
        return "notice_308";
    }

    @Override
    protected NotificationType getNotificationType() {
        return NotificationType.TRIP_AFFIRM;
    }

    @Override
    protected String getExpectedStatus() {
        return PERSONAL_AWAITING_TRIP_APPROVAL.name();
    }
}
