package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.TimingService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"grpc.server.port=-1",
        "spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications=DEBUG",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=false"} )
@DisplayName("Проверка сервиса уведомлений GroupTransfer при отсутствии настроек")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
public class GroupTransferLoseNotificationsTest extends GroupTransferCommon {

    @Autowired
    @Qualifier("tripRequestInput")
    protected Consumer<Message<RequestMessage>> requestMessageInput;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private NotificationSettingsService notificationSettingsService;

    @Autowired
    private TimingService timingService;

    @MockitoBean
    private SmsSender smsSender;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeService employeeService;

    static final UUID departmentId = UUID.randomUUID();
    static final UUID departmentHeadId = UUID.randomUUID();
    static final UUID organizationId = UUID.randomUUID();
    static final UUID authorId = UUID.randomUUID();
    static final UUID passengerId = UUID.randomUUID();

    @Autowired
    private EmployeeRepository employeeRepository;
    private Employee passenger;
    private Employee author;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {

            var department = Department.builder().id(departmentId)
                    .departmentHeadId(departmentHeadId)
                    .organizationId(organizationId).build();

            var department2 = Department.builder().id(UUID.randomUUID())
                    .departmentHeadId(departmentHeadId)
                    .organizationId(UUID.randomUUID()).build();

            departmentRepository.save(department);
            departmentRepository.save(department2);

            Employee employee = new Employee();
            employee.setId(authorId);
            employee.setUserId(authorId);
            employee.setEmail("123@mail.ru");
            employee.setPhoneConfirmed(true);
            employee.setPhone("+79111111111");
            employee.setOrganizationId(UUID.randomUUID());
            employee.setDepartmentId(department.getId());
            author = employeeRepository.save(employee);

            create_or_update_notification_with_send_time (
                    "REQUEST_GROUP_TRANSFER",
                    "Водитель назначен",
                    "notice_4006",
                    organizationId.toString(),
                    null,
                    "GROUP_TRANSFER_DRIVER_FOUND",
                    null,
                    "ORGANIZATION",

                    "На заявку {ID} назначен автомобиль: {carInfo}. Водитель: {driverFio}, {driverPhone}.",
                    true,
                    true,
                    true,

                    0,
                    "AT_EVENT",
                    null,
                    null
            );
        });
    }

    @DisplayName("Проверка НЕ реакции на отсутствие настройоки")
    @Test
    void testByInputChannelEntityNotFoundException() throws JsonProcessingException {

        transactionTemplate.executeWithoutResult(status -> {

            assertThat(employeeRepository.findById(authorId)).isPresent();
//            assertThat(employeeRepository.findById((passengerId)).isPresent();
        });

        UUID id = UUID.randomUUID();
        requestMessageInput.accept(MessageBuilder.withPayload(
                createRequestMessage(id, TripRequestStatus.GROUP_TRANSFER_DRIVER_SEARCH)).build());

        requestMessageInput.accept(MessageBuilder.withPayload(
                createRequestMessage(id, TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND)).build());

        verify(smsSender, times(1)).send(any(), any(), any());

        var notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).isSent()).isTrue();
    }

    private RequestMessage createRequestMessage(UUID id, TripRequestStatus requestStatus) {

        return RequestMessage.builder()
                .id(id)
                .purposeId(UUID.randomUUID())
                .approvalId(UUID.randomUUID())
                .deleted(false)
                .authorId(authorId)
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .timeZone(ZoneOffset.UTC.toString())
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(authorId)
                .tariffId(UUID.randomUUID())
                .tripClass("ECONOMY")
                .transportType(TransportTypeEnum.GROUP_TRANSFER.name())
                .waypoints(List.of(
                        new RequestMessage.Waypoint(
                                id,
                                AddressMessage.builder().id(UUID.randomUUID()).build(),
                                null,
                                false,
                                false,
                                null,
                                0
                        ),
                        new RequestMessage.Waypoint(
                                id,
                                AddressMessage.builder().id(UUID.randomUUID()).build(),
                                null,
                                false,
                                false,
                                null,
                                1
                        )))
                .status(requestStatus.toString())
                .rideId(UUID.randomUUID())
                .approvalState("APPROVED")
                .driverData(new RequestMessage.DriverData("DriverLastName", "DriverFirstName", "DriverPatronymic", "DriverPhoneNumber"))
                .vehicleData(new RequestMessage.VehicleData("VehicleBrand", "VehicleModel", "VehicleStateNumber", "VehicleColor"))
                .build();

    }
}
