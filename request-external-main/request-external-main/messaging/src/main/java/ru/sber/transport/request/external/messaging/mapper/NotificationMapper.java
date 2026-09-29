package ru.sber.transport.request.external.messaging.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.request.external.messaging.message.NotificationMessage;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.TripOrderData;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final EmployeesProvider employeesProvider;

    public NotificationMessage toMessage(TripOrderData order, String messageType, UUID receiver) {
        return NotificationMessage.builder()
                .id(order.getId())
                .applicationType("SBT")
                .messageType(messageType)
                .receivers(List.of(receiver))
                .data(List.of(
                        NotificationMessage.Data.builder()
                                .key("humanReadableId").value(order.getHumanReadableId()).build(),
                        NotificationMessage.Data.builder()
                                .key("passengerName").value(createName(order.getPassenger())).build(),
                        NotificationMessage.Data.builder()
                                .key("requestId").value(String.valueOf(order.getId())).build()
                ))
                .build();
    }

    public ru.sber.transport.messages.notification_900.avro.NotificationMessage toAvroMessage(TripOrderData order,
                                                                                              String messageType,
                                                                                              UUID receiver) {
        return ru.sber.transport.messages.notification_900.avro.NotificationMessage.newBuilder()
                .setId(order.getId())
                .setApplicationType("SBT")
                .setMessageType(messageType)
                .setData(createData(order))
                .setReceivers(List.of(receiver))
                .build();
    }

    private ru.sber.transport.messages.notification_900.avro.DataList createData(TripOrderData order) {
        return ru.sber.transport.messages.notification_900.avro.DataList.newBuilder()
                .setValues(List.of(
                        ru.sber.transport.messages.notification_900.avro.Data.newBuilder()
                                .setKey("humanReadableId").setValue(order.getHumanReadableId()).build(),
                        ru.sber.transport.messages.notification_900.avro.Data.newBuilder()
                                .setKey("passengerName").setValue(createName(order.getPassenger())).build(),
                        ru.sber.transport.messages.notification_900.avro.Data.newBuilder()
                                .setKey("requestId").setValue(String.valueOf(order.getId())).build()
                ))
                .build();
    }

    private String createName(Employee employee) {
        if (employee.getLastName() == null) {
            employee = employeesProvider.get(employee.getId());
        }
        final var builder = new StringBuilder(employee.getFirstName());
        Optional.ofNullable(employee.getPatronymic()).ifPresent(it -> builder.append(" ").append(it));
        Optional.ofNullable(employee.getLastName()).filter(it -> !it.isBlank()).map(it -> it.charAt(0)).ifPresent(it -> builder.append(" ").append(it).append("."));
        return builder.toString();
    }
}
