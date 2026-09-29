package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.*;

import static ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferHelper.commonValidation;

/**
 * Соответствует уведомлению notice_4003 Трансфер согласован
 */
@Slf4j
@Service
public class GroupTransferApprovedCommand extends GroupTransferAbstractCommand {

    public GroupTransferApprovedCommand(DepartmentService departmentService, NotificationSettingsService notificationSettingsService, NotificationGlobalProcessor<TripRequest> processor, EmployeeService employeeService) {
        super(departmentService, notificationSettingsService, processor, employeeService);
    }

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (commonValidation(previous, current)) {
            return false;
        }

        if (!Objects.equals(previous.getStatus(), TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL.name())) {
            return false;
        }

        if (!Objects.equals(current.getStatus(), getExpectedStatus())) {
            return false;
        }
        if (current.getApprovalId() == null) {
            return false;
        }
        if (Objects.equals(current.getAuthorId(), current.getApprovalId())) {
            return false;
        }

        log.debug("{}: Запрос прошел валидацию {}", getDescription(), getExpectedStatus());
        return true;
    }

    @Override
    protected List<UUID> getReceiverIds(TripRequest request) {
        List<UUID> receiverIds = new ArrayList<>();
        Optional.ofNullable(request.getAuthorId()).ifPresent(receiverIds::add);
        return receiverIds;
    }

    @Override
    protected String getDescription() {
        return "notice_4003";
    }

    @Override
    protected NotificationType getNotificationType() {
        return NotificationType.GROUP_TRANSFER_APPROVED;
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected String getExpectedStatus() {
        return TripRequestStatus.GROUP_TRANSFER_APPROVED.name();
    }

}
