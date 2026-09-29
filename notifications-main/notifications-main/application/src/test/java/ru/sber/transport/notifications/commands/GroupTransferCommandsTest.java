package ru.sber.transport.notifications.commands;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.Address;
import ru.sber.transport.notifications.database.model.request.ExpectedData;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.request.Waypoint;
import ru.sber.transport.notifications.dto.contractor.DriverDTO;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferRequestDriverFoundNotificationCommand;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupTransferCommandsTest {

    private final List<NotificationCommand<TripRequest>> commands = List.of(
            new GroupTransferRequestDriverFoundNotificationCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class))
    );

    private final DriverDTO driverDTO = DriverDTO.builder()
            .firstName("DriverFirstName")
            .lastName("DriverLastName")
            .patronymic("DriverPatronymic")
            .phoneNumber("DriverPhoneNumber")
            .build();
    private final VehicleDTO vehicleDTO = VehicleDTO.builder()
            .brand("VehicleBrand")
            .model("VehicleModel")
            .stateNumber("VehicleStateNumber")
            .color("VehicleColor")
            .build();
    private final int count = 3;
    private final List<Address> addresss = IntStream.range(0, count - 1)
            .mapToObj(this::getAddress)
            .toList();

    private final List<Waypoint> waypoints = IntStream.range(0, count - 1)
            .mapToObj(i -> buildWaypoint(addresss, i))
            .toList();

    private final Map<String, Object> informational2 = Map.of("addContactPhone", "877777777");

    @Test
    void commands() {
        assertTrue(commands.stream().anyMatch(it -> !it.validate(null, null)));

        List<TripRequest> ps = List.of(
                buildRequest(UUID.randomUUID(), "", 10, TransportTypeEnum.PERSONAL),
                buildRequest(UUID.randomUUID(), "", 10, TransportTypeEnum.DEDICATED),
                buildRequest(UUID.randomUUID(), "", 10, null)
        );

        var cs = List.of(buildRequest(UUID.randomUUID(), "", 10, TransportTypeEnum.DEDICATED, null, null, null, null, null, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, null, null, null, null, null, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, LocalDateTime.now().plusDays(1), null, Collections.EMPTY_LIST, null, null, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, LocalDateTime.now().plusDays(1), "+3", Collections.EMPTY_LIST, null, null, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, LocalDateTime.now().plusDays(1), "+3", waypoints, null, null, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, LocalDateTime.now().plusDays(1), "+3", waypoints, vehicleDTO, null, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, LocalDateTime.now().plusDays(1), "+3", waypoints, null, driverDTO, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED, LocalDateTime.now().plusDays(1), "+3", waypoints, vehicleDTO, driverDTO, null),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.GROUP_TRANSFER, LocalDateTime.now().plusDays(1), "+3", waypoints, vehicleDTO, driverDTO, informational2)
        );

        ps.forEach(p -> cs.forEach(c -> assertTrue(commands.stream().noneMatch(it -> it.validate(p, c)))));

    }

    public TripRequest buildRequest(UUID id, String status, int statusCode, TransportTypeEnum type) {

        return buildRequest(id, status, statusCode, type,
                LocalDateTime.now().plusDays(1), "+9", waypoints, vehicleDTO,
                driverDTO, null);
    }


    public TripRequest buildRequest(UUID id, String status, int statusCode, TransportTypeEnum type,
                                    LocalDateTime desiredDate, String timeZone, List<Waypoint> waypoint, VehicleDTO vehicleDTO,
                                    DriverDTO driverDTO, Map<String, Object> information) {

        var msg = TripRequest.builder()
                .id(id)
                .transportType(type)
                .author(buildEmployee(id))
                .purposeId(UUID.randomUUID())
                .approvalId(UUID.randomUUID())
                .approvalState(ApprovalState.AWAITING_APPROVAL.name())
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now())
                .desiredDate(desiredDate)
                .expected(ExpectedData.builder().cost(10).distance(100D).time(Duration.ZERO).build())
                .finishedTime(LocalDateTime.now().plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .status(status)
                .tariffId(UUID.randomUUID())
                .tripClass("ECONOMY")
                .driver(driverDTO)
                .vehicle(vehicleDTO)
                .waypoints(waypoint)
                .timeZone(timeZone)
                .information(information);

        return msg.build();
    }

    public Address getAddress(int i) {
        return Address.builder()
                .region("Регион " + i)
                .city("Город" + i)
                .street("улица " + i)
                .house("" + i)
                .build();
    }

    public Waypoint buildWaypoint(List<Address> adresss, int i) {
        return Waypoint.builder()
                .address(getAddress(i))
                .waitTime(Duration.ofMillis(10000000))
                .build();
    }


    public Employee buildEmployee(UUID id2) {
        return Employee.builder()
                .id(id2)
                .departmentId(id2)
                .firstName("firstName")
                .lastName("lastName")
                .patronymic("patronymic")
                .build();
    }

}