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

import java.util.List;

import static ru.sber.transport.notifications.services.impl.commands.cargo.NotificationHelper.*;

/**
 * Соответствует уведомлению notice_814 Заявка отменена Инженером
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargoTripNotificationCanceledByEngineerCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (commonValidation(previous, current)) {
            return false;
        }
        var allowState = List.of(TripRequestStatus.CARGO_APPROVED.name(),
                TripRequestStatus.CARGO_AWAITING_DATA.name(), TripRequestStatus.CARGO_AWAITING_TRANSFER.name());
        if (commonCanceledValidation(previous, current, allowState, 801)) {
            return false;
        }

        log.debug("notice_814: Запрос прошел валидацию REQUEST_CARGO.CARGO_CANCELED_BY_ENGINEER");
        return true;
    }

    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.CARGO_CANCELED_BY_ENGINEER;
        var author = employeeService.get(request.getAuthorId()).orElse(null);
        commonNotification(request, notificationType, author, departmentService, notificationSettingsService,
                processor);
    }
}