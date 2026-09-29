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
import ru.sber.transport.notifications.services.impl.commands.cargo.*;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CargoCommandsTest {

    List<NotificationCommand<TripRequest>> commands = List.of(
            new CargoTripNotificationCanceledByContractorCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class)),
            new CargoTripNotificationCanceledByEngineerCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class)),
            new CargoTripNotificationSetApproverCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class)),
            new CargoTripNotificationSetApproveStatusCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class)),
            new CargoTripNotificationSetCourierRouteCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class)),
            new CargoTripNotificationSetDriverRouteCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class)),
            new CargoTripNotificationTransferFinishedCommand(Mockito.mock(DepartmentService.class),
                    Mockito.mock(NotificationSettingsService.class),
                    Mockito.mock(NotificationGlobalProcessor.class),
                    Mockito.mock(EmployeeService.class))
    );

    @Test
    void commands() {
        assertTrue(commands.stream().anyMatch(it -> !it.validate(null, null)));

        List<TripRequest> ps = List.of(
                buildRequest(UUID.randomUUID(), "", 10, TransportTypeEnum.PERSONAL),
                buildRequest(UUID.randomUUID(), "", 10, TransportTypeEnum.DEDICATED),
                buildRequest(UUID.randomUUID(), "", 10, null)
                );
        var cs = List.of(buildRequest(UUID.randomUUID(), "", 10, TransportTypeEnum.DEDICATED),
                buildRequest(UUID.randomUUID(), "1", 801, TransportTypeEnum.DEDICATED));

        cs.forEach(c->ps.forEach(p -> assertTrue(commands.stream().noneMatch(it -> it.validate(p, c)))));

    }

    public  TripRequest buildRequest(UUID id, String status, int statusCode, TransportTypeEnum type) {

        int count = 3;
        List<Address> addresss = IntStream.range(0, count - 1)
                .mapToObj(this::getAddress)
                .toList();

        var waypoints = IntStream.range(0, count - 1)
                .mapToObj(i -> buildWaypoint(addresss, i))
                .toList();

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
                .desiredDate(LocalDateTime.now().plusDays(1))
                .expected(ExpectedData.builder().cost(10).distance(100D).time(Duration.ZERO).build())
                .finishedTime(LocalDateTime.now().plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .status(status)
                .tariffId(UUID.randomUUID())
                .tripClass("ECONOMY")
                .statusCode(statusCode)
                .driver(DriverDTO.builder()
                        .firstName("DriverFirstName")
                        .lastName("DriverLastName")
                        .patronymic("DriverPatronymic")
                        .phoneNumber("DriverPhoneNumber")
                        .build())
                .vehicle(VehicleDTO.builder()
                        .brand("VehicleBrand")
                        .model("VehicleModel")
                        .stateNumber("VehicleStateNumber")
                        .color("VehicleColor")
                        .build())
                .waypoints(waypoints);

        if (statusCode == 202) {
            msg.approvalState(ApprovalState.DECLINED.name());
            msg.statusCode(202);
        }

        return msg.build();
    }

    public  Address getAddress(int i) {
        return Address.builder()
                .region("Регион " + i)
                .city("Город" + i)
                .street("улица " + i)
                .house("" + i)
                .build();
    }

    public  Waypoint buildWaypoint(List<Address> adresss, int i) {
        return Waypoint.builder()
                .address(getAddress(i))
                .waitTime(Duration.ofMillis(10000000))
                .build();
    }


    public  Employee buildEmployee(UUID id2) {
        return Employee.builder()
                .id(id2)
                .departmentId(id2)
                .firstName("firstName")
                .lastName("lastName")
                .patronymic("patronymic")
                .build();
    }

}
