package ru.sber.transport.notifications.services.impl.commands.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class TripRequestAbstractCommand implements NotificationCommand<TripRequest> {

    protected final DepartmentService departmentService;
    protected final NotificationSettingsService notificationSettingsService;
    protected final NotificationGlobalProcessor<TripRequest> processor;
    protected final EmployeeService employeeService;

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null) {
            return false;
        }
        if (previous.getTransportType() == null) {
            return false;
        }
        if (!Objects.equals(previous.getTransportType(), getExpectedTransportType())) {
            return false;
        }
        if (Objects.equals(previous.getStatus(), current.getStatus())) {
            return false;
        }
        if (Objects.equals(previous.getStatus(), getExpectedStatus())) {
            return false;
        }
        if (!Objects.equals(current.getStatus(), getExpectedStatus())) {
            return false;
        }
        log.debug("{}: Запрос прошел валидацию {}", getDescription(), getCommandName());
        return true;
    }


    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        log.debug("{}: {} start", getDescription(), getCommandName());
        var notificationType = getNotificationType();
        List<UUID> receiverIds = getReceiverIds(request);
        receiverIds = receiverIds.stream().distinct().toList();
        receiverIds.forEach(receiverId -> {
                var receiver = employeeService.get(receiverId).orElse(null);
                commonNotification(request,
                        notificationType,
                        receiver,
                        departmentService, notificationSettingsService,
                        processor);
            }
        );
    }
    void commonNotification(TripRequest request,
                                          NotificationType notificationType,
                                          Employee receiver, DepartmentService departmentService,
                                          NotificationSettingsService notificationSettingsService,
                                          NotificationGlobalProcessor<TripRequest> processor) {
        if (receiver != null && receiver.getDepartmentId() != null) {
            var department = departmentService.get(receiver.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                try {
                    NotificationSettings settings = notificationSettingsService.get(department.getOrganizationId(),
                            getNotificationClass(),
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

    @NotNull
    protected abstract NotificationClass getNotificationClass();

    protected abstract TransportTypeEnum getExpectedTransportType();

    @NotNull
    protected abstract List<UUID> getReceiverIds(TripRequest request);

    protected abstract String getDescription();

    protected abstract NotificationType getNotificationType();

    protected abstract String getCommandName();


    protected abstract String getExpectedStatus();
}
