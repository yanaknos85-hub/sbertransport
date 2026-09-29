package ru.sber.transport.notifications.services.impl.commands.cargo;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.List;
import java.util.Objects;

@Slf4j
public final class NotificationHelper {

    private NotificationHelper() {
        throw new UnsupportedOperationException("This is a utility class and can not be instantiated");
    }

    public static boolean commonValidation(TripRequest previous, TripRequest current) {
        return previous == null ||
                current == null ||
                previous.getTransportType() == null ||
                previous.getTransportType().getServiceType() != TransportServiceType.CARGO_TRANSPORTATION ||
                Objects.equals(previous.getStatus(), current.getStatus());
    }

    public static boolean commonRoutValidation(TripRequest previous, TripRequest current,
                                               List<TransportTypeEnum> notAllow) {
        return !notAllow.contains(previous.getTransportType()) ||
                !Objects.equals(previous.getStatus(), TripRequestStatus.CARGO_AWAITING_DATA.name()) ||
                !Objects.equals(current.getStatus(), TripRequestStatus.CARGO_AWAITING_TRANSFER.name()) ||
                Objects.equals(current.getAuthorId(), current.getApprovalId());
    }

    public static boolean commonCanceledValidation(TripRequest previous, TripRequest current,
                                                   List<String> allowState, int statusCode) {
        if (current.getStatusCode() != statusCode) {
            return true;
        }
        if (!allowState.contains(previous.getStatus())) {
            return true;
        }
        if (!Objects.equals(current.getStatus(), TripRequestStatus.CARGO_CANCELED.name())) {
            return true;
        }
        return false;
    }

    public static void commonNotification(TripRequest request,
                                          NotificationType notificationType,
                                          Employee author, DepartmentService departmentService,
                                          NotificationSettingsService notificationSettingsService,
                                          NotificationGlobalProcessor<TripRequest> processor) throws JsonProcessingException {
        if (author != null && author.getDepartmentId() != null) {
            var department = departmentService.get(author.getDepartmentId()).orElseThrow();
            if (department.getOrganizationId() != null) {
                NotificationSettings settings = notificationSettingsService.get(department.getOrganizationId(),
                        NotificationClass.REQUEST_CARGO,
                        notificationType);
                processor.process(author.getId(), settings, request);
            } else {
                log.error("Некоторые данные департамента " + department.getId() + " не найдены!");
            }
        } else {
            log.error("Автор не найден!");
        }
    }
}
