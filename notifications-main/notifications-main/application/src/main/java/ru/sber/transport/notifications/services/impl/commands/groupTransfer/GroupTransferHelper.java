package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class GroupTransferHelper {

    private GroupTransferHelper() {
        throw new UnsupportedOperationException("This is a utility class and can not be instantiated");
    }
    public static boolean commonValidation(TripRequest previous, TripRequest current) {
        if (previous == null || current == null) {
            return true;
        }
        if (previous.getTransportType() == null) {
            return true;
        }
        if (!TransportTypeEnum.GROUP_TRANSFER.equals(previous.getTransportType())) {
            return true;
        }
        if (Objects.equals(previous.getStatus(), current.getStatus())) {
            return true;
        }
        return false;
    }

    public static void commonNotification(TripRequest request,
                                          NotificationType notificationType,
                                          Employee receiver, DepartmentService departmentService,
                                          NotificationSettingsService notificationSettingsService,
                                          NotificationGlobalProcessor<TripRequest> processor) {
        if (receiver != null && receiver.getDepartmentId() != null) {
            var department = departmentService.get(receiver.getDepartmentId()).orElseThrow();
            if (department.getOrganizationId() != null) {
                try {
                    NotificationSettings settings = notificationSettingsService.get(department.getOrganizationId(),
                            NotificationClass.REQUEST_GROUP_TRANSFER,
                            notificationType);
                    processor.process(receiver.getId(), settings, request);
                } catch (ru.sber.transport.exceptions.EntityNotFoundException | JsonProcessingException e) {
                    log.warn(e.getMessage());
                }
            } else {
                log.error("Некоторые данные департамента " + department.getId() + " не найдены!");
            }
        } else {
            log.error("Автор не найден!");
        }
    }

    public static Optional<NotificationSettings> safelyGetSettings(NotificationSettingsService notificationSettingsService, Department department, NotificationType notificationType) {
        try {
            return Optional.of(notificationSettingsService.get(department.getOrganizationId(),
                    NotificationClass.REQUEST_GROUP_TRANSFER,
                    notificationType));
        } catch (ru.sber.transport.exceptions.EntityNotFoundException e) {
            //Не является ошибкой отсутствия настройки. Оставлено предупреждение в логах.
            log.warn(e.getMessage());
            return Optional.empty();
        }
    }

}
