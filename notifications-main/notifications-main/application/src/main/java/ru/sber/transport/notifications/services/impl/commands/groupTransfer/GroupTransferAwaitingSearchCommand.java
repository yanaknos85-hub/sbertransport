package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Соответствует уведомлению notice_2101 Трансфер согласован
 */
@Slf4j
@Service
public class GroupTransferAwaitingSearchCommand extends GroupTransferAbstractCommand {

    public GroupTransferAwaitingSearchCommand(DepartmentService departmentService, NotificationSettingsService notificationSettingsService, NotificationGlobalProcessor<TripRequest> processor, EmployeeService employeeService) {
        super(departmentService, notificationSettingsService, processor, employeeService);
    }

    @NotNull
    protected NotificationType getNotificationType() {
        return NotificationType.GROUP_TRANSFER_AWAITING_SEARCH;
    }

    protected  String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @NotNull
    protected String getDescription() {
        return "notice_4004";
    }

    @NotNull
    protected String getExpectedStatus() {
        return TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH.name();
    }

    @Override
    protected List<UUID> getReceiverIds(TripRequest request) {
        List<UUID> receiverIds = new ArrayList<>();
        Optional.ofNullable(request.getAuthorId()).ifPresent(receiverIds::add);
        Optional.ofNullable(request.getPassengerId()).ifPresent(receiverIds::add);
        return receiverIds;
    }

}