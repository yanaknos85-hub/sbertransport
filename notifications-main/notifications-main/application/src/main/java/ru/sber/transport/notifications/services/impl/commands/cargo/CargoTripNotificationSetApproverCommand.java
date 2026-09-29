package ru.sber.transport.notifications.services.impl.commands.cargo;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.Objects;

import static ru.sber.transport.notifications.services.impl.commands.cargo.NotificationHelper.commonNotification;
import static ru.sber.transport.notifications.services.impl.commands.cargo.NotificationHelper.commonValidation;

/**
 * Соответствует уведомлению notice_801 Заявка создана, заявка назначена на Согласующего.
 * (кроме автоматически согласованных)
 * Статус "На согласовании"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargoTripNotificationSetApproverCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (commonValidation(previous, current)) {
            return false;
        }

        if (Objects.equals(previous.getStatus(), TripRequestStatus.CARGO_AWAITING_APPROVAL.name())) {
            return false;
        }

        if (!Objects.equals(current.getStatus(), TripRequestStatus.CARGO_AWAITING_APPROVAL.name())) {
            return false;
        }
        if (current.getApprovalId() == null) {
            return false;
        }
        if (current.getApprovalId().equals(current.getAuthorId())) {
            return false;
        }

        log.debug("notice_801: Запрос прошел валидацию REQUEST_CARGO.APPROVE");
        return true;
    }

    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.APPROVE;
        var author = employeeService.get(request.getAuthorId()).orElse(null);
        commonNotification(request, notificationType, author, departmentService, notificationSettingsService,
                processor);
    }
}
