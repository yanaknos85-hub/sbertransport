package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.customer.CustomerDto;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import static ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferHelper.safelyGetSettings;

/**
 * Соответствует уведомлению notice_4007 Ожидайте трансфер
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupTransferRequestDriverFoundNotificationCommand implements NotificationCommand<TripRequest> {
    public static final String CUSTOMER_PHONE_FIELD = "addContactPhone";
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;

    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null) {
            return false;
        }

        if (current.getDesiredDate() == null || current.getTimeZone() == null) {
            return false;
        }
        if (current.getWaypoints().isEmpty()) {
            return false;
        }
        if (current.getDriver() == null) {
            return false;
        }
        if (current.getVehicle() == null) {
            return false;
        }

        if (!(TripRequestStatus.GROUP_TRANSFER_DRIVER_SEARCH.name().equals(previous.getStatus())
                || TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH.name().equals(previous.getStatus()))) {
            return false;
        }
        if (!TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name().equals(current.getStatus())) {
            return false;
        }
        if (!TransportTypeEnum.GROUP_TRANSFER.equals(current.getTransportType())) {
            return false;
        }

        return true;
    }

    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        log.debug("notice_4007: GroupTransferRequestDriverFoundNotificationCommand start");
        var notificationType = NotificationType.GROUP_TRANSFER_DRIVER_FOUND;
        var notificationTypeMzk = NotificationType.GROUP_TRANSFER_DRIVER_FOUND_MZK;

        LocalDateTime localDateTime = request.getDesiredDate().atZone(ZoneId.of(ZoneOffset.UTC.getId())).withZoneSameInstant(ZoneId.of(request.getTimeZone())).toLocalDateTime();
        request.setLocalDesiredDate(localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        request.setLocalDesiredTime(localDateTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        request.setDepartureAddress(request.getWaypoints().get(0).getAddress().toFullAddressString());
        request.setDestinationAddress(request.getWaypoints().get(request.getWaypoints().size() - 1).getAddress().toFullAddressString());
        request.setDriverFio(request.getDriver().getDriverFio());
        request.setCarInfo(request.getVehicle().getCarInfo());

        CustomerDto customer = null;

        if (request.getInformation() != null
                && !request.getInformation().isEmpty()
                && request.getInformation().get(CUSTOMER_PHONE_FIELD) != null
                && !request.getInformation().get(CUSTOMER_PHONE_FIELD).equals("null")) {
            customer = getCustomer(request.getInformation().get(CUSTOMER_PHONE_FIELD));
        }

        var driver = request.getDriver();
        var author = employeeService.get(request.getAuthorId()).orElse(null);
        var passenger = employeeService.get(request.getPassengerId()).orElse(null);

        if (driver != null
                && author != null && author.getDepartmentId() != null
                && passenger!= null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(author.getDepartmentId()).orElseThrow();
            if (department.getOrganizationId() != null) {
                Optional<NotificationSettings> optSettings = safelyGetSettings(notificationSettingsService, department, notificationType);
                if (optSettings.isPresent()) {
                    processor.process(author.getId(), optSettings.get(), request);
                    if (passenger.getId() != null && !Objects.equals(passenger.getId(), author.getId())) {
                        processor.process(passenger.getId(), optSettings.get(), request);
                    }
                }
                optSettings = safelyGetSettings(notificationSettingsService, department, notificationTypeMzk);
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


    private CustomerDto getCustomer(Object comment) {
        return CustomerDto.builder()
                .phone(String.valueOf(comment))
                .build();
    }
}