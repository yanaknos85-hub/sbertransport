package ru.sber.transport.notifications.commands;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.approvals.messages.ApproveSharedRideMessage;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;
import ru.sber.transport.messages.corporate.avro.*;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request.messaging.TaxiTripMessage;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Import(TestChannelBinderConfiguration.class)
public class SharedCommands {
    @MockitoBean
    protected PushSender pushSender;

    @Autowired
    @Qualifier("organizations")
    protected Consumer<Message<OrganizationMessage>> organizationMessageInput;

    @Autowired
    @Qualifier("deadlineSettingsInput")
    protected Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsMessageInput;

    @Autowired
    @Qualifier("departments")
    protected Consumer<Message<DepartmentMessage>> departmentMessageInput;

    @Autowired
    @Qualifier("employee")
    protected Consumer<Message<EmployeeMessage>> employeeMessageInput;

    @Autowired
    @Qualifier("employee")
    protected Consumer<Message<EmployeeMessage>> supervisorEmployeeMessageInput;

    @Autowired
    @Qualifier("employee")
    protected Consumer<Message<EmployeeMessage>> delegateEmployeeMessageInput;

    @Autowired
    @Qualifier("delegateInput")
    protected Consumer<Message<DelegateMessage>> delegateMessageInput;

    @Autowired
    @Qualifier("tripRequestInput")
    protected Consumer<Message<RequestMessage>> requestMessageInput;

    @Autowired
    @Qualifier("tripApproveInput")
    protected Consumer<Message<ApproveTripRequestMessage>> approveTripRequestMessageInput;

    @Autowired
    @Qualifier("taxiTripInput")
    protected Consumer<Message<TaxiTripMessage>> taxiTripMessageInput;

    @Autowired
    @Qualifier("driverInput")
    protected Consumer<Message<DriverMessage>> driverMessageInput;

    @Autowired
    @Qualifier("taxiTariffInput")
    protected Consumer<Message<TaxiTariffMessage>> taxiTariffMessageInput;

    protected OrganizationMessage organizationMessage;
    protected DeadlineSettingsMessage deadlineMessage;
    protected UUID ownerId;
    protected DepartmentMessage departmentMessage;
    protected EmployeeMessage passengerMessage1;
    protected EmployeeMessage passengerMessage2;
    protected EmployeeMessage supervisorEmployeeMessage;
    protected EmployeeMessage delegateEmployeeMessage;
    protected EmployeeMessage approverEmployeeMessage;
    protected List<RequestMessage.Waypoint> waypoints;
    protected ApproveSharedRideMessage sharedRideMessage;
    protected ApproveTripRequestMessage approveTripRequestMessage;
    protected RequestMessage requestMessage;
    protected DriverMessage driverMessage;
    protected TaxiTripMessage taxiTripMessage;
    protected TaxiTariffMessage tariffMessage;
    protected DelegateMessage delegateMessage;

    @BeforeEach
    public void init() {
        organizationMessage = new OrganizationMessage();
        organizationMessage.setId(UUID.randomUUID());

        deadlineMessage = new DeadlineSettingsMessage(
                UUID.randomUUID(),
                organizationMessage.getId(),
                new DeadlineSettingsMessage.RequestDeadlineSettingsItem(
                        UUID.randomUUID(),
                        ChronoUnit.HOURS,
                        1,
                        TransportTypeEnum.TAXI.name(),
                        TripRequestStatus.TAXI_AWAITING_APPROVAL.name()
                ),
                null,
                null,
                new DeadlineSettingsMessage.RequestDeadlineSettingsItem(
                        UUID.randomUUID(),
                        ChronoUnit.HOURS,
                        1,
                        TransportTypeEnum.PERSONAL.name(),
                        TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name()
                ),
                null,
                null,
                null,
                null,
                null,

                new DeadlineSettingsMessage.RequestDeadlineSettingsItem(
                        UUID.randomUUID(),
                        ChronoUnit.HOURS,
                        1,
                        TransportTypeEnum.PUBLIC.name(),
                        TripRequestStatus.PUBLIC_AWAITING_APPROVAL.name()
                ),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        ownerId = UUID.randomUUID();

        departmentMessage = DepartmentMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setOrganizationId(organizationMessage.getId())
                .setHeadId(ownerId)
                .setDeleted(false)
                .setHumanReadableId(Instancio.create(String.class))
                .setCode(Instancio.create(String.class))
                .setName("Department 1")
                .build();

        supervisorEmployeeMessage = EmployeeMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setDeleted(false)
                .setDepartmentId(departmentMessage.getId())
                .setOrganizationId(organizationMessage.getId())
                .setFirstName("Моисей")
                .setLastName("Шилов")
                .setPatronymic("Дмитриевич")
                .setHumanReadableId(Instancio.create(String.class))
                .setConsent(Instancio.create(Boolean.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setContacts(List.of(Contact.newBuilder().setType(ContactEmployeeType.EMAIL).setIsConfirmed(false).setValue("supervisor@sbertransport.ru").setInternal(true).build()))
                .setUserId(UUID.randomUUID())
                .build();

        delegateEmployeeMessage = EmployeeMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setDeleted(false)
                .setDepartmentId(departmentMessage.getId())
                .setOrganizationId(organizationMessage.getId())
                .setFirstName("Болеслав")
                .setLastName("Федотов")
                .setPatronymic("Арсеньевич")
                .setHumanReadableId(Instancio.create(String.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setConsent(Instancio.create(Boolean.class))
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setContacts(List.of(Contact.newBuilder().setType(ContactEmployeeType.EMAIL).setIsConfirmed(false).setValue("delegate@sbertransport.ru").setInternal(true).build()))
                .setUserId(UUID.randomUUID()).build();

        passengerMessage1 = EmployeeMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setDeleted(false)
                .setDepartmentId(departmentMessage.getId())
                .setOrganizationId(organizationMessage.getId())
                .setFirstName("Александр")
                .setLastName("Пушкин")
                .setPatronymic("Сергеевич")
                .setHumanReadableId(Instancio.create(String.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setConsent(Instancio.create(Boolean.class))
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setContacts(List.of(Contact.newBuilder().setType(ContactEmployeeType.EMAIL).setIsConfirmed(false).setValue("mail1@mail.ru").setInternal(true).build()))
                .setUserId(UUID.randomUUID()).build();

        approverEmployeeMessage = EmployeeMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setDeleted(false)
                .setDepartmentId(departmentMessage.getId())
                .setOrganizationId(organizationMessage.getId())
                .setFirstName("Александр")
                .setLastName("Островский")
                .setPatronymic("Николаевич")
                .setHumanReadableId(Instancio.create(String.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setConsent(Instancio.create(Boolean.class))
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setContacts(List.of(Contact.newBuilder().setType(ContactEmployeeType.EMAIL).setIsConfirmed(false).setValue("mail2@mail.ru").setInternal(true).build()))
                .setUserId(UUID.randomUUID()).build();

        passengerMessage2 = EmployeeMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setDeleted(false)
                .setDepartmentId(departmentMessage.getId())
                .setOrganizationId(organizationMessage.getId())
                .setFirstName("Александр 2")
                .setLastName("Пушкин 2")
                .setPatronymic("Сергеевич 2")
                .setHumanReadableId(Instancio.create(String.class))
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setConsent(Instancio.create(Boolean.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setContacts(List.of(Contact.newBuilder().setType(ContactEmployeeType.EMAIL).setIsConfirmed(false).setValue("mail3@mail.ru").setInternal(true).build()))
                .setUserId(UUID.randomUUID()).build();
        waypoints = createWaypoints();

        requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .build();

        sharedRideMessage = new ApproveSharedRideMessage(
                requestMessage.getId(),
                null,
                true,
                approverEmployeeMessage.getId(),
                requestMessage.getDesiredDate(),
                "NEW",
                null
        );

        approveTripRequestMessage = new ApproveTripRequestMessage(
                requestMessage.getId(),
                null,
                requestMessage.getApprovalId(),
                null,
                List.of(approverEmployeeMessage.getId())
        );

        driverMessage = new DriverMessage(
                UUID.randomUUID(),
                "Тест",
                "Тестов",
                "Тестович",
                null,
                UUID.randomUUID(),
                true,
                499,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null
        );

        taxiTripMessage = new TaxiTripMessage(
                TripType.COOP.name(),
                UUID.randomUUID(),
                500000 + "",
                organizationMessage.getId(),
                "Красный Запорожец О111ОО Рандомов Рандом Рандомович",
                UUID.randomUUID(),
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(5),
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(10),
                null,
                InboundTaxiTripStatus.TRIP_IN_PROGRESS.name(),
                4.70,
                Duration.ofMinutes(15).plusHours(5),
                15600,
                Duration.ofMinutes(5),
                null,
                null,
                requestMessage.getRideId(),
                null,
                false,
                new TaxiTripMessage.Driver("Фамилия", "Имя", "Отчество", "Телефон"),
                null
        );

        tariffMessage = new TaxiTariffMessage(
                UUID.randomUUID(),
                "TT1",
                null,
                TransportServiceType.EMPLOYEE_TRANSPORTATION.name(),
                null,
                null,
                TransportTypeEnum.TAXI.name(),
                null,
                true,
                null,
                null,
                null,
                TaxiClass.ECONOMY.name(),
                0,
                0,
                0,
                0,
                5000,
                0,
                0,
                0,
                0,
                0,
                1,
                1,
                1,
                1,
                1,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                null,
                null,
                null,
                1,
                1,
                1,
                1,
                1,
                null,
                0,
                false,
                null,
                false
        );

        delegateMessage = DelegateMessage.builder()
                .id(UUID.randomUUID())
                .deleted(false)
                .delegateId(delegateEmployeeMessage.getId())
                .supervisorId(supervisorEmployeeMessage.getId())
                .transportTypeId(TransportTypeEnum.TAXI.getId())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .build();
    }

    public List<RequestMessage.Waypoint> createWaypoints() {
        return List.of(
                createWaypoint(0),
                createWaypoint(1),
                createWaypoint(2),
                createWaypoint(3)
        );
    }

    protected RequestMessage.Waypoint createWaypoint(int orderingIndex) {
        return createWaypoint(UUID.randomUUID(), null, orderingIndex);
    }

    protected RequestMessage.Waypoint createWaypoint(UUID id, Boolean checkinAutomatic, int orderingIndex) {
        return new RequestMessage.Waypoint(
                id,
                AddressMessage.builder().id(UUID.randomUUID()).build(),
                null,
                checkinAutomatic,
                false,
                null,
                orderingIndex
        );
    }

    @SuppressWarnings("unchecked")
    public void verifyTokenAndMessage(String notificationMessage, UUID token) {
        var tokenCaptor = ArgumentCaptor.forClass(List.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        var additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pushSender).send(any(UUID.class), tokenCaptor.capture(), typeCaptor.capture(), templateCaptor.capture(),
                dataCaptor.capture(), additionalDataCaptor.capture());
        var actualTokens = tokenCaptor.getValue();
        var actualMessages = templateCaptor.getValue();
        var actualData = dataCaptor.getValue();

        assertThat(actualTokens).hasSize(1);
        assertThat(actualMessages).isEqualTo(notificationMessage);
        assertThat(actualData).isNull();

        var actualToken = (UUID) actualTokens.getFirst();

        assertThat(actualToken).isEqualTo(token);
    }

    @SuppressWarnings("unchecked")
    public void verifyTokensAndMessages(String notificationMessage, UUID token) {
        var tokenCaptor = ArgumentCaptor.forClass(List.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        var additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pushSender).send(any(UUID.class), tokenCaptor.capture(), typeCaptor.capture(), templateCaptor.capture(),
                dataCaptor.capture(), additionalDataCaptor.capture());
        var actualTokens = tokenCaptor.getValue();
        var actualMessages = templateCaptor.getValue();
        var actualData = dataCaptor.getValue();

        assertThat(actualTokens).hasSize(1);
        assertThat(actualMessages).isEqualTo(notificationMessage);
        assertThat(actualData).isNull();

        var actualToken = (UUID) actualTokens.getFirst();

        assertThat(actualToken).isEqualTo(token);
    }
}
