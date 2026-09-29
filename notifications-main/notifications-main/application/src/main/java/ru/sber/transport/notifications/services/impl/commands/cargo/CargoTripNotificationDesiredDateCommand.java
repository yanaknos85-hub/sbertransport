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
import ru.sberbank.ditsib.transport.constants.TransportServiceType;

import java.time.temporal.ChronoUnit;

import static ru.sber.transport.notifications.services.impl.commands.cargo.NotificationHelper.commonNotification;

/**
 * Соответствует уведомлению notice_816 Заявка отменена контрагентом
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargoTripNotificationDesiredDateCommand implements NotificationCommand<TripRequest> {
    private final EmployeeService employeeService;
    private final NotificationSettingsService notificationSettingsService;
    private final DepartmentService departmentService;
    private final NotificationGlobalProcessor<TripRequest> processor;

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null) {
            return false;
        }
        if (previous.getTransportType() == null) {
            return false;
        }
        if (previous.getTransportType().getServiceType() != TransportServiceType.CARGO_TRANSPORTATION) {
            return false;
        }

        if (previous.getDesiredDate().truncatedTo(ChronoUnit.MINUTES)
                .equals(current.getDesiredDate().truncatedTo(ChronoUnit.MINUTES))) {
            return false;
        }
        log.info("notice_818: Запрос прошел валидацию REQUEST_CARGO.DESIRED_DATE");
        return true;
    }

    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.CARGO_CHANGE_DESIRED_DATE;
        var author = employeeService.get(request.getAuthorId()).orElse(null);
        commonNotification(request, notificationType, author, departmentService, notificationSettingsService,
                processor);
    }
}
