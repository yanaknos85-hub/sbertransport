package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sber.transport.notifications.services.impl.commands.common.TripRequestAbstractCommand;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferHelper.commonNotification;
import static ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferHelper.commonValidation;

@Slf4j
public abstract class GroupTransferAbstractCommand extends TripRequestAbstractCommand {

    protected GroupTransferAbstractCommand(DepartmentService departmentService, NotificationSettingsService notificationSettingsService, NotificationGlobalProcessor<TripRequest> processor, EmployeeService employeeService) {
        super(departmentService, notificationSettingsService, processor, employeeService);
    }

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (commonValidation(previous, current)) {
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
    protected TransportTypeEnum getExpectedTransportType() {
        return TransportTypeEnum.GROUP_TRANSFER;
    }

    @Override
    protected NotificationClass getNotificationClass() {
        return NotificationClass.REQUEST_GROUP_TRANSFER;
    }

}
