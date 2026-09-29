package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitResponse;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.dao.FraudRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPersonalRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestHistoryRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.request.database.model.CheckinSettings;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationResultDto;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RegionDto;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceResponse;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckResultDTO;
import ru.sberbank.ditsib.transport.request.exceptions.DurationLimitExceededException;
import ru.sberbank.ditsib.transport.request.exceptions.EmployeeAbsenceException;
import ru.sberbank.ditsib.transport.request.exceptions.MultipointLimitExceededException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;
import ru.sberbank.ditsib.transport.request.service.FraudService;
import ru.sberbank.ditsib.transport.request.service.GeoDataProcessingService;
import ru.sberbank.ditsib.transport.request.service.MagentaAuxilaryService;
import ru.sberbank.ditsib.transport.request.service.PersonalTariffService;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.validate.TripSplitCheckService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.ABSENT_EMPLOYEE_FRAUD_COMMENT;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.DURATION_FRAUD_COMMENT;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.SINGLE_TRIP_DURATION_FRAUD_COMMENT;

@ExtendWith(MockitoExtension.class)
class RequestForPersonalServiceImplTest {

    public static final int METER_IN_KILOMETER = 1000;
    @InjectMocks
    private RequestForPersonalServiceImpl requestForPersonalService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TripPurposeRepository tripPurposeRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SQGenerator sqGenerator;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private RequestForPersonalRepository requestForPersonalRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private GeoDataProcessingService geoDataProcessingService;
    @Mock
    private MagentaAuxilaryService magentaAuxilaryService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private RequestHistoryRepository historyRepository;
    @Mock
    private RequestSender<RequestForPersonal> requestSender;
    @Mock
    private PersonalTariffService personalTariffService;
    @Mock
    private EntityDTOMapper entityDTOMapper;
    @Mock
    private RegionDataResolver regionDataResolver;
    @Mock
    private CheckinSettingsService checkinSettingsService;
    @Mock
    private PersonalCarDataResolver personalCarDataResolver;
    @Captor
    private ArgumentCaptor<RequestForPersonal> requestForPersonalArgumentCaptor;
    @Mock
    private Clock clock;
    @Mock
    private FraudService fraudService;
    @Mock
    private EasupGrpcService easupGrpcService;
    @Mock
    private TripSplitCheckService splitCheckService;
    @Mock
    private FraudRepository fraudRepository;
    @Mock
    private RequestChecksGrpcService requestChecksGrpcService;
    @Mock
    private DurationRequestCheckGrpcClient durationRequestCheckGrpcClient;
    @Mock
    private OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient;
    @Mock
    private FraudMonitoringService fraudMonitoringService;

    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2024, 9, 17, 16, 10);
    private static final Clock FIXED_CLOCK = Clock.fixed(CURRENT_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    void add() {
        //TODO тест не завершен, ввиду многообразия кейсов
        var sharedRideId = UUID.randomUUID();
        var token = Instancio.create(String.class);
        var executorGroup = Instancio.create(ExecutorGroupDTO.class);
        var request = Instancio.of(NewRequestDTO.class)
                .set(field(NewRequestDTO::getTransportType), TransportTypeEnum.PERSONAL)
                .set(field(NewRequestDTO::getTimeZone), ZoneId.of(ZoneOffset.UTC.getId()).getId())
                .create();
        var author = Instancio.create(Employee.class);
        var department = Instancio.create(Department.class);
        var organization = Instancio.create(Organization.class);
        var humanReadableId = Instancio.create(String.class);
        var tariff = Instancio.create(PersonalTariff.class);
        var waypoints = Instancio.createList(Waypoint.class);
        var expectedData = Instancio.create(ExpectedData.class);
        var passenger = Instancio.create(Employee.class);
        var savedRequest = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getTimeZone), ZoneId.of(ZoneOffset.UTC.getId()).getId())
                .create();
        var limitReservationResultDto = Instancio.create(LimitReservationResultDto.class);
        var requestForPersonalList = Instancio.createList(RequestForPersonal.class);
        var regionDto = Instancio.create(RegionDto.class);
        var checkinSettings = Instancio.create(CheckinSettings.class);
        var tripPurpose = Instancio.create(TripPurpose.class);
        var personalCarDTO = Instancio.create(PersonalCarDTO.class);

        var expected = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getAuthor), author)
                .set(field(RequestForPersonal::getSegmentsJSON), request.getExpected().getSegments())
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .set(field(RequestForPersonal::getPassenger), passenger)
                .set(field(RequestForPersonal::getPurpose), tripPurpose)
                .set(field(RequestForPersonal::getTariffId), request.getTariffId())
                .set(field(RequestForPersonal::getTariff), tariff)
                .set(field(RequestForPersonal::getOutcomeTariffId), request.getOutcomeTariffId())
                .set(field(RequestForPersonal::getOutcomeTariff), tariff)
                .set(field(RequestForPersonal::getHumanReadableId), humanReadableId)
                .set(field(RequestForPersonal::getTimeZone), request.getTimeZone())
                .set(field(RequestForPersonal::getStatus), TripRequestStatus.PERSONAL_AWAITING_APPROVAL)
                .set(field(RequestForPersonal::getApprovalState), ApprovalState.AWAITING_APPROVAL)
                .set(field(RequestForPersonal::getCreationTime), CURRENT_DATE_TIME)
                .set(field(RequestForPersonal::getRequestOptions), request.getRequestOptions())
                .set(field(RequestForPersonal::getOrganizationId), organization.getId())
                .set(field(RequestForPersonal::getRequestPrice), request.getRequestPrice())
                .set(field(RequestForPersonal::getEmployeeDeviceTimeZone), request.getEmployeeDeviceTimeZone())
                .set(field(RequestForPersonal::getApprovalDeadlineState), DeadlineState.NONE)
                .set(field(RequestForPersonal::getJoinedPassengerIds), request.getJoinedPassengerIds())
                .set(field(RequestForPersonal::getSource), request.getSource())
                .set(field(RequestForPersonal::getMinTariffTaxi), request.getMinTariffTaxi())
                .set(field(RequestForPersonal::getExecutorGroupId), executorGroup.getId())
                .set(field(RequestForPersonal::getExecutorGroupName), executorGroup.getName())
                .set(field(RequestForPersonal::getCommentForDriver), request.getCommentForDriver())
                .set(field(RequestForPersonal::getPassengerCount), request.getPassengerCount())
                .set(field(RequestForPersonal::getCommentForPurpose), request.getCommentForPurpose())
                .set(field(RequestForPersonal::getTransportType), TransportTypeEnum.OFFICIAL)
                .set(field(RequestForPersonal::isCoopTrip), true)
                .set(field(RequestForPersonal::isActive), true)
                .set(field(RequestForPersonal::getApprovalDate), null)
                .set(field(RequestForPersonal::getApprovedBy), null)
                .set(field(RequestForPersonal::getCostSharePart), null)
                .set(field(RequestForPersonal::getFinishedTime), null)
                .set(field(RequestForPersonal::getId), null)
                .set(field(RequestForPersonal::getNumberPassengersJoined), null)
                .set(field(RequestForPersonal::getPassengersFromCoop), null)
                .set(field(RequestForPersonal::getRequestRating), null)
                .set(field(RequestForPersonal::getRideId), null)
                .set(field(RequestForPersonal::getSavingsCash), null)
                .set(field(RequestForPersonal::getSavingsProcents), null)
                .set(field(RequestForPersonal::getStatusCode), 0)
                .set(field(RequestForPersonal::isSharedRideOwner), false)
                .set(field(RequestForPersonal::getAdditionalSum), null)
                .set(field(RequestForPersonal::getAdditionalSumReason), null)
                .set(field(RequestForPersonal::getEmployeeDriverId), passenger.getId())
                .set(field(RequestForPersonal::getOccupiedPlacesCount), request.getOccupiedPlacesCount())
                .set(field(RequestForPersonal::getOrderPaymentFormationFinishingDate), null)
                .set(field(RequestForPersonal::getOrderPaymentFormationStartDate), null)
                .set(field(RequestForPersonal::getPaymentDoneDatetime), null)
                .set(field(RequestForPersonal::getPaymentDoneDeadline), null)
                .set(field(RequestForPersonal::getPaymentDoneDeadlineState), DeadlineState.NONE)
                .set(field(RequestForPersonal::getPersonalCar), personalCarDTO)
                .set(field(RequestForPersonal::getPersonalCarId), request.getPersonalCarId())
                .set(field(RequestForPersonal::isSlaExpired), false)
                .set(field(RequestForPersonal::getTripApprovalDatetime), null)
                .set(field(RequestForPersonal::getTripApprovalDeadline), null)
                .set(field(RequestForPersonal::getTripApprovalDeadlineState), DeadlineState.NONE)
                .set(field(RequestForPersonal::getTripStartLatitude), 0.0)
                .set(field(RequestForPersonal::getTripStartLongitude), 0.0)
                .set(field(RequestForPersonal::getTripStartTime), null)
                .set(field(RequestForPersonal::getTransportType), TransportTypeEnum.PERSONAL)
                .ignore(field(RequestForPersonal::getFraudData))
                .create();

        ReflectionTestUtils.setField(requestForPersonalService, "additionalSumForDriver", 10000);

        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(Optional.of(department)).when(departmentService).get(author.getDepartment().getId());
        doReturn(Optional.of(organization)).when(organizationService).get(department.getOrganization().getId());
        doReturn(humanReadableId).when(sqGenerator).getNextId(Prefix.OT, organization.getDigitId());
        doReturn(tariff).when(personalTariffService).getTariffById(request.getTariffId());
        doReturn(tariff).when(personalTariffService).getTariffById(request.getOutcomeTariffId());
        doReturn(waypoints).when(entityDTOMapper).waypointDTOListToWaypointList(request.getExpected().getWaypoints());
        doReturn(Optional.of(passenger)).when(employeeService).get(request.getPassenger().id());
        doReturn(expectedData).when(entityDTOMapper).dtoToExpectedData(request.getExpected());
        doNothing().when(geoDataProcessingService).saveAddresses(waypoints, passenger);
        doReturn(savedRequest).when(requestForPersonalRepository).saveAndFlush(requestForPersonalArgumentCaptor.capture());
        doReturn(null).when(magentaAuxilaryService).processCoopRequest(any(), any(), any());
        doReturn(limitReservationResultDto).when(reservationService).makeReservation(any(),
                any(),
                anyDouble(),
                anyLong());
        doNothing().when(historyRepository).flush();
        doNothing().when(addressRepository).flush();
        doReturn(requestForPersonalList).when(requestForPersonalRepository).findByRideId(sharedRideId);
        doReturn(regionDto).when(regionDataResolver).getRegion(any());
        doReturn(checkinSettings).when(checkinSettingsService).getByParams(any(), any(), any());
        doReturn(tripPurpose).when(tripPurposeRepository).getReferenceById(request.getPurpose().getId());
        doReturn(savedRequest).when(requestForPersonalRepository).save(requestForPersonalArgumentCaptor.capture());
        doReturn(personalCarDTO).when(personalCarDataResolver).getPersonalCar(department.getOrganization().getId(),
                department.getId(),
                request.getPassenger().id(),
                request.getPersonalCarId(),
                token);
        doReturn(new TripSplitCheckResultDTO(true, null)).when(splitCheckService).check(any());
        doReturn(Collections.emptyList()).when(fraudRepository).findAllByRequestIdIn(anyList());

        var actual = (RequestForPersonal) requestForPersonalService.add(sharedRideId, true, request, author, token, executorGroup);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedRequest);
        assertThat(actual.isCoopTrip()).isFalse();
        //проверяем только первое сохранение, их вообще должно быть не более одного
        var saved = requestForPersonalArgumentCaptor.getAllValues().get(0);
        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("approvalDeadline")
                .ignoringFields("deadlineState")
                .ignoringFields("historyItemsForPersonal")
                .ignoringFields("desiredDate")
                .ignoringFields("fraudData")
                .isEqualTo(expected);
    }

    @Test
    void checkOnAbsenceWhenFalse() {
        var employee = Instancio.create(Employee.class);
        var request = Instancio.create(RequestForPersonal.class);
        var absenceRequest = new EasupAbsenceRequest(request.getDesiredDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
                request.getTimeZone(), request.getPassenger().getPersonnelNumber(),
                request.getExpected().getTime().toMillis());

        doReturn(Optional.empty()).when(easupGrpcService).resolveAbsence(absenceRequest);

        assertThat(requestForPersonalService.checkOnAbsence(request, employee, Optional.empty())).isFalse();
    }

    @Test
    void checkOnAbsenceWhenException() {
        var employee = Instancio.create(Employee.class);
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .create();
        var absenceRequest = new EasupAbsenceRequest(request.getDesiredDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
                request.getTimeZone(), request.getPassenger().getPersonnelNumber(),
                request.getExpected().getTime().toMillis());

        var response = Instancio.create(EasupAbsenceResponse.class);

        doReturn(Optional.of(response)).when(easupGrpcService).resolveAbsence(absenceRequest);

        Optional<String> timezone = Optional.empty();

        assertThatExceptionOfType(EmployeeAbsenceException.class)
                .isThrownBy(() -> requestForPersonalService.checkOnAbsence(
                        request, employee, timezone))
                .withMessage("Невозможно создать заявку");
    }

    @Test
    void checkOnAbsenceWhenTrue() {
        var employee = Instancio.create(Employee.class);
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), Instancio.create(Employee.class))
                .create();
        var absenceRequest = new EasupAbsenceRequest(request.getDesiredDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
                request.getTimeZone(), request.getPassenger().getPersonnelNumber(),
                request.getExpected().getTime().toMillis());

        var response = Instancio.create(EasupAbsenceResponse.class);

        doReturn(Optional.of(response)).when(easupGrpcService).resolveAbsence(absenceRequest);

        assertThat(requestForPersonalService.checkOnAbsence(request, employee, Optional.empty())).isTrue();
    }

    @Test
    void createFraud() {
        var request = Instancio.create(RequestForCarsharing.class);
        requestForPersonalService.createFraud(request, FraudType.ABSENCE, ABSENT_EMPLOYEE_FRAUD_COMMENT);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());

        var toSave = fraudDataCaptor.getValue();
        assertThat(toSave).isNotNull();
        assertThat(toSave.getType()).isEqualTo(FraudType.ABSENCE);
        assertThat(toSave.getRequest().getId()).isEqualTo(request.getId());
        assertThat(toSave.getComment()).isEqualTo(ABSENT_EMPLOYEE_FRAUD_COMMENT);
    }

    @Test
    void checkMultipointLimitWhenDisabled() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .create();

        ReflectionTestUtils.setField(requestForPersonalService, "isMultipointValidationEnabled", false);

        requestForPersonalService.checkMultipointLimit(request, Optional.empty());

        verifyNoInteractions(requestChecksGrpcService);
    }

    @Test
    void checkMultipointLimitWhenLessThanOrEqualToTwoWaypoints() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .create();

        ReflectionTestUtils.setField(requestForPersonalService, "isMultipointValidationEnabled", true);

        requestForPersonalService.checkMultipointLimit(request, Optional.empty());

        verifyNoInteractions(requestChecksGrpcService);
    }

    @Test
    void checkMultipointLimitWhenExceeded() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .create();

        ReflectionTestUtils.setField(requestForPersonalService, "isMultipointValidationEnabled", true);
        doReturn(true).when(requestChecksGrpcService).isMultipointLimitExceeded(
                request.getPassenger().getId(),
                request.getDesiredDate(),
                request.getTimeZone()
        );
        Optional<String> emptyTimeZone = Optional.empty();
        assertThatExceptionOfType(MultipointLimitExceededException.class)
                .isThrownBy(() -> requestForPersonalService.checkMultipointLimit(request, emptyTimeZone));
    }

    @Test
    void checkMultipointLimitWhenNotExceeded() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .create();

        ReflectionTestUtils.setField(requestForPersonalService, "isMultipointValidationEnabled", true);
        doReturn(false).when(requestChecksGrpcService).isMultipointLimitExceeded(
                request.getPassenger().getId(),
                request.getDesiredDate(),
                request.getTimeZone()
        );

        requestForPersonalService.checkMultipointLimit(request, Optional.empty());

        verify(requestChecksGrpcService).isMultipointLimitExceeded(
                request.getPassenger().getId(),
                request.getDesiredDate(),
                request.getTimeZone()
        );
    }

    @Test
    void checkDurationLimitWhenNullExpectedTime() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForPersonalService.checkDurationLimit(request, Optional.empty());

        verifyNoInteractions(durationRequestCheckGrpcClient);
    }

    @Test
    void checkOverrunLimitWhenNullExpectedTime() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForPersonalService.checkOverrunLimit(request, Optional.empty());

        verifyNoInteractions(overrunRequestCheckGrpcClient);
    }

    @Test
    void checkOverrunLimitWhenNullExpectedDistance() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getDistance), null)
                        .create())
                .create();

        requestForPersonalService.checkOverrunLimit(request, Optional.empty());

        verifyNoInteractions(overrunRequestCheckGrpcClient);
    }

    @Test
    void checkOverrunLimitWhenOverrunNotExceeded() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .set(field(ExpectedData::getDistance), 100.0)
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .set(field(RequestForPersonal::getTimeZone), "Europe/Moscow")
                .create();
        doReturn(null).when(overrunRequestCheckGrpcClient).checkOverrunLimit(
                any(UUID.class),
                any(LocalDateTime.class),
                anyInt(),
                any(String.class)
        );

        requestForPersonalService.checkOverrunLimit(request, Optional.empty());

        verify(overrunRequestCheckGrpcClient).checkOverrunLimit(
                request.getPassenger().getId(),
                request.getDesiredDate().atZone(ZoneId.of(request.getTimeZone())).toLocalDateTime(),
                (int) Math.round(request.getExpected().getDistance() * METER_IN_KILOMETER),
                request.getTimeZone()
        );
        verifyNoInteractions(fraudService);
    }

    @Test
    void checkOverrunLimitWhenOverrunExceededAndFraudCreated() {
        var employee = Instancio.create(Employee.class);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .set(field(ExpectedData::getDistance), 500.0)
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getPassenger), employee)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .set(field(RequestForPersonal::getTimeZone), "Europe/Moscow")
                .create();
        var response = CheckOverrunLimitResponse.newBuilder()
                .setComment("Превышен лимит")
                .setTotalDistance(500500)
                .build();
        doReturn(response).when(overrunRequestCheckGrpcClient).checkOverrunLimit(
                any(UUID.class),
                any(LocalDateTime.class),
                anyInt(),
                any(String.class)
        );

        requestForPersonalService.checkOverrunLimit(request, Optional.empty());

        verify(overrunRequestCheckGrpcClient).checkOverrunLimit(
                request.getPassenger().getId(),
                request.getDesiredDate().atZone(ZoneId.of(request.getTimeZone())).toLocalDateTime(),
                (int) Math.round(request.getExpected().getDistance() * METER_IN_KILOMETER),
                request.getTimeZone()
        );

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.OVERRUN, "Превышен лимит 500.5 км");
    }

    @Test
    void checkDurationLimitWhenNotExceeded() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .set(field(RequestForPersonal::getTimeZone), "Europe/Moscow")
                .set(field(RequestForPersonal::getDesiredDate), CURRENT_DATE_TIME)
                .create();

        requestForPersonalService.checkDurationLimit(request, Optional.empty());

        verify(durationRequestCheckGrpcClient).checkDurationLimit(
                eq(request.getPassenger().getId()),
                any(OffsetDateTime.class),
                eq(request.getExpected().getTime().toMillis()),
                eq("Europe/Moscow")
        );
    }

    @Test
    void checkDurationLimitWhenExceeded() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .set(field(RequestForPersonal::getTimeZone), "Europe/Moscow")
                .set(field(RequestForPersonal::getDesiredDate), CURRENT_DATE_TIME)
                .create();

        doThrow(new DurationLimitExceededException()).when(durationRequestCheckGrpcClient).checkDurationLimit(
                any(UUID.class),
                any(),
                anyLong(),
                any(String.class)
        );

        requestForPersonalService.checkDurationLimit(request, Optional.empty());

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.DURATION, DURATION_FRAUD_COMMENT);

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.DURATION);
        assertThat(fraudData.getComment()).isEqualTo(DURATION_FRAUD_COMMENT);
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void checkDurationLimitWhenExceededWithNoExpectedTime() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForPersonalService.checkDurationLimit(request, Optional.empty());

        verifyNoInteractions(durationRequestCheckGrpcClient);
        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkSingleTripDurationWhenNullExpectedTime() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForPersonalService.checkSingleTripDuration(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkSingleTripDurationWhenNotExceeded() {
        ReflectionTestUtils.setField(requestForPersonalService, "singleTripDurationLimitMs", 28800000L);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .create();

        requestForPersonalService.checkSingleTripDuration(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkSingleTripDurationWhenExceededAndFraudCreated() {
        ReflectionTestUtils.setField(requestForPersonalService, "singleTripDurationLimitMs", 28800000L);
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(10))
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .create();

        requestForPersonalService.checkSingleTripDuration(request);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.SINGLE_TRIP_DURATION, SINGLE_TRIP_DURATION_FRAUD_COMMENT);

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.SINGLE_TRIP_DURATION);
        assertThat(fraudData.getComment()).isEqualTo(SINGLE_TRIP_DURATION_FRAUD_COMMENT);
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void checkDurationLimitWhenExceededWithInvalidTimeZone() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getWaypoints), waypoints)
                .set(field(RequestForPersonal::getExpected), expectedData)
                .set(field(RequestForPersonal::getTimeZone), "Invalid/Zone")
                .set(field(RequestForPersonal::getDesiredDate), CURRENT_DATE_TIME)
                .create();

        doThrow(new DurationLimitExceededException()).when(durationRequestCheckGrpcClient).checkDurationLimit(
                any(UUID.class),
                any(),
                anyLong(),
                any(String.class)
        );

        requestForPersonalService.checkDurationLimit(request, Optional.empty());

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.DURATION, DURATION_FRAUD_COMMENT);

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.DURATION);
        assertThat(fraudData.getComment()).isEqualTo(DURATION_FRAUD_COMMENT);
    }
}