package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.request.Address;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.request.Waypoint;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sber.transport.notifications.dto.customer.CustomerDto;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferHelper.safelyGetSettings;

/**
 * Соответствует уведомлению notice_4009
 */
@Slf4j
@Service
public class GroupTransferDriverArrivedCommand extends GroupTransferAbstractCommand {

    public static final String CUSTOMER_PHONE_FIELD = "addContactPhone";

    public GroupTransferDriverArrivedCommand(DepartmentService departmentService, NotificationSettingsService notificationSettingsService, NotificationGlobalProcessor<TripRequest> processor, EmployeeService employeeService) {
        super(departmentService, notificationSettingsService, processor, employeeService);
    }

    @NotNull
    protected NotificationType getNotificationType() {
        return NotificationType.GROUP_TRANSFER_DRIVER_ARRIVED;
    }

    protected  String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @NotNull
    protected String getDescription() {
        return "notice_4009";
    }
    @NotNull
    protected String getExpectedStatus() {
        return TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();
    }

    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        log.debug("{}: {} start", getDescription(), getCommandName());
        var notificationType = getNotificationType();

        List<Waypoint> waypoints = request.getWaypoints();
        if (waypoints != null && !waypoints.isEmpty()) {
            Address addr = waypoints.get(0).getAddress();
            if (addr != null) {
                request.setDepartureAddress(addr.toFullAddressString());
            }
        }

        Optional.ofNullable(request.getVehicle())
                .map(VehicleDTO::getCarInfo)
                .ifPresent(request::setCarInfo);

        var author = employeeService.get(request.getAuthorId()).orElse(null);
        CustomerDto customer = null;
        if (request.getInformation() != null
                && !request.getInformation().isEmpty()
                && request.getInformation().get(CUSTOMER_PHONE_FIELD) != null
                && !request.getInformation().get(CUSTOMER_PHONE_FIELD).equals("null")) {
            customer = getCustomer(request.getInformation().get(CUSTOMER_PHONE_FIELD));
        }

        super.sendNotification(request);

        if (author != null && author.getDepartmentId() != null) {
            var department = departmentService.get(author.getDepartmentId()).orElseThrow();
            if (department.getOrganizationId() != null) {
                Optional<NotificationSettings> optSettings = safelyGetSettings(notificationSettingsService, department, notificationType);
                if (customer != null && optSettings.isPresent()) {
                    processor.process(author.getId(), optSettings.get(), request, customer);
                }
            } else {
                log.error("Некоторые данные департамента " + department.getId() + " не найдены!");
            }
        } else {
            log.error("Пассажир не найден!");
        }

    }
    @Override
    protected List<UUID> getReceiverIds(TripRequest request) {
        List<UUID> receiverIds = new ArrayList<>();
        Optional.ofNullable(request.getAuthorId()).ifPresent(receiverIds::add);
        Optional.ofNullable(request.getPassengerId()).ifPresent(receiverIds::add);
        return receiverIds;
    }
    private CustomerDto getCustomer(Object comment) {
        return CustomerDto.builder()
                .phone(String.valueOf(comment))
                .build();
    }
}