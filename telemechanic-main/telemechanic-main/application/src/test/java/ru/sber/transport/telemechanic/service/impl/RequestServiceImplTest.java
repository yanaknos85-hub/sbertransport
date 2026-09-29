package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.dao.EwbTitleRepository;
import ru.sber.transport.telemechanic.database.dao.RequestHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.RequestRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.*;
import ru.sber.transport.telemechanic.exception.BadRequestException;
import ru.sber.transport.telemechanic.exception.CloseRequestException;
import ru.sber.transport.telemechanic.exception.EwbNotFoundException;
import ru.sber.transport.telemechanic.exception.TechnicContractNotFoundException;
import ru.sber.transport.telemechanic.helper.CheckHelper;
import ru.sber.transport.telemechanic.mapper.RequestMapper;
import ru.sber.transport.telemechanic.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.sber.transport.telemechanic.TestData.createRequest;
import static ru.sber.transport.telemechanic.exception.CloseRequestException.MSG_AUTHOR;
import static ru.sber.transport.telemechanic.exception.CloseRequestException.MSG_STATUS;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса списка проверок")
class RequestServiceImplTest {
    private Organization organization;
    private Department department;
    private Employee employee1;
    private Employee employee2;
    private Transport transport;
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    private final static LocalDateTime LOCAL_DATE = LocalDateTime.of(2022, 11, 29, 10, 11);
    @InjectMocks
    private RequestServiceImpl requestService;
    @Mock
    protected Clock clock;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestHistoryRepository requestHistoryRepository;
    @Mock
    private EwbRepository ewbRepository;
    @Mock
    private RequestMapper requestMapper;
    @Mock
    private TransportService transportService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SQGenerator sqGenerator;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private CheckHelper checkHelper;
    @Mock
    private EwbTitleRepository ewbTitleRepository;
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private EwbPathService ewbPathService;
    @Mock
    private DepartmentTimeZoneService departmentTimeZoneService;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                                   .id(UUID.randomUUID())
                                   .officialName("officialName1")
                                   .digitId(1L)
                                   .msrn("11111111")
                                   .tin("111111")
                                   .build();
        department = Instancio.create(Department.class);
        var position = Instancio.of(Position.class)
                                .set(field(Position::getOrganization), organization).create();
        var position2 = Instancio.of(Position.class)
                                .set(field(Position::getOrganization), organization).create();
        employee1 = Instancio.of(Employee.class)
                                .set(field(Employee::getOrganization), organization)
                                .set(field(Employee::getDepartment), department)
                                .set(field(Employee::getPosition), position)
                                .create();
        employee2 = Instancio.of(Employee.class)
                             .set(field(Employee::getOrganization), organization)
                             .set(field(Employee::getDepartment), department)
                             .set(field(Employee::getPosition), position2)
                             .create();
        transport = Instancio.of(Transport.class)
                             .set(field(Transport::getStatus), TransportStatus.IN_USE)
                             .set(field(Transport::getStateNumber), "А010ЕК50")
                             .create();
    }
    
    @Test
    void createEmpty() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var humanReadableId = "ОТ-123456";
        var createRequest = new CreateRequestDto(UUID.randomUUID());
        when(transportService.getTransportById(any())).thenReturn(transport);
        when(departmentService.get(employee1.getDepartment().getId())).thenReturn(Optional.of(department));
        when(sqGenerator.getNextId(any(), any())).thenReturn(humanReadableId);
        when(requestRepository.save(any())).thenReturn(null);
        when(requestHistoryRepository.save(any())).thenReturn(null);
        var contractorOrganizationId = UUID.randomUUID();
        doReturn(contractorOrganizationId).when(ewbTariffService).getContractorOrganizationId(any(UUID.class));
        var request = requestService.createEmpty(createRequest, employee1, false);
        var ewbCheck = request.getChecks().stream()
                              .filter(check -> check.getCheckType().getInEwbPath() != null)
                              .toList();
        
        assertNotNull(request);
        assertEquals(humanReadableId, request.getHumanReadableId());
        var oldPath = Arrays.stream(CheckType.values())
                            .filter(checkType -> Objects.isNull(checkType.getInEwbPath()))
                            .toList();
        assertEquals(oldPath.size(), request.getChecks().size());
        assertEquals(employee1, request.getAuthor());
        assertEquals(LOCAL_DATE, request.getCreationTime());
        assertEquals(transport.getStateNumber(), request.getTransport().getStateNumber());
        assertThat(ewbCheck).isEmpty();
        assertThat(request.getOrganizationId()).isEqualTo(contractorOrganizationId);
        
        request = requestService.createEmpty(createRequest, employee1, true);
        ewbCheck = request.getChecks().stream()
                          .filter(check -> check.getCheckType().getInEwbPath() != null)
                          .sorted(Comparator.comparing(check -> check.getCheckType().getOrdinal()))
                          .toList();
        assertNotNull(request);
        assertThat(ewbCheck).hasSize(2);
        assertEquals(CheckType.ODOMETER, ewbCheck.get(0).getCheckType());
        assertEquals(CheckType.LITREAGE, ewbCheck.get(1).getCheckType());
        assertThat(request.getOrganizationId()).isEqualTo(contractorOrganizationId);
        
        doReturn(null).when(ewbTariffService).getContractorOrganizationId(any(UUID.class));
        assertThatExceptionOfType(TechnicContractNotFoundException.class)
                .isThrownBy(() -> requestService.createEmpty(createRequest, employee1, false))
                .withMessage("У организации нет активных договоров или тарифов на проведение технических осмотров");
    }

    @Test
    void get() {
        var source = createRequest(employee1, transport, organization.getId());
        when(requestRepository.findById(any())).thenReturn(Optional.of(source));
        var actual = requestService.get(source.getId());
        assertEquals(actual.getHumanReadableId(), source.getHumanReadableId());
        assertEquals(actual.getCreationTime(), source.getCreationTime());
        assertEquals(actual.getAuthor().getId(), source.getAuthor().getId());
        assertEquals(actual.getStatus(), source.getStatus());
        assertEquals(actual.getTransport().getStateNumber(), source.getTransport().getStateNumber());
    }
    
    @Test
    void getChecksTree() {
        var request = Instancio.create(Request.class);
        request.getChecks().addAll(List.of(
                new Check(UUID.randomUUID(), CheckType.VEHICLE_NUMBER, CheckStatus.DONE, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.OIL_LEVEL, CheckStatus.DONE, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.WIND_SCREEN, CheckStatus.DONE, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.COOLANT_LEVEL, CheckStatus.IN_PROGRESS, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.HEADLAMPS_LR, CheckStatus.IN_PROGRESS, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.ODOMETER, CheckStatus.DONE, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.HEADLAMPS_LF, CheckStatus.DONE, 1, request, null),
                new Check(UUID.randomUUID(), CheckType.SPLASH_GUARDS_LR, CheckStatus.DONE, 1, request, null)
                                          ));
        doReturn(Optional.of(request)).when(requestRepository).findById(any(UUID.class));
        doReturn(Instancio.create(ChecksTreeDto.ChecksTree.class)).when(checkHelper).createChecksTree(anySet());
        var actual = requestService.getChecksTreeByRequestId(UUID.randomUUID());
        assertTrue(actual.isCallTelemech());
        assertFalse(actual.getChecks().getPass().isEmpty());
        assertFalse(actual.getChecks().getFinished().isEmpty());
    }

    @Test
    void changeStatus() {
        var source = createRequest(employee1, transport, organization.getId());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        source.setChecksFinishedTime(LocalDateTime.now(clock));
        assertEquals(RequestStatus.IN_PROGRESS, source.getStatus());
        when(requestRepository.findById(any())).thenReturn(Optional.of(source));
        var actual = requestService.changeStatus(source.getId(), RequestStatus.DONE, employee1);

        assertEquals(actual.getId(), source.getId());
        assertEquals(RequestStatus.DONE, actual.getStatus());
        assertEquals(LocalDateTime.now(clock), actual.getChecksFinishedTime());
    }

    @Test
    @DisplayName("Проверка На линии")
    void getOnTheLineRequest() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var request = createRequest(employee1, transport, organization.getId());
        request.setId(UUID.randomUUID());
        request.setStatus(RequestStatus.ON_THE_LINE);
        when(requestRepository.findAllByAuthorAndStatusIn(employee1,
                List.of(RequestStatus.ON_THE_LINE,
                        RequestStatus.DONE,
                        RequestStatus.WARNING))).thenReturn(Collections.singletonList((request)));
        doReturn(false).when(ewbPathService).calculatingClientPath(employee1);
        var actual = requestService.getOnTheLineRequest(employee1);
        assertEquals(request.getStatus(), actual.requestStatus());
        assertFalse(actual.ewbPath());
        
        var transport2 = Instancio.of(Transport.class)
                                  .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                  .set(field(Transport::getStateNumber), "А199ЕК799")
                                  .create();
        var request2 = createRequest(employee2, transport2, organization.getId());
        request2.setId(UUID.randomUUID());
        request2.setStatus(RequestStatus.ON_THE_LINE);
        request2.setCreationTime(LocalDateTime.now(clock).minusDays(1));
        when(requestRepository.findAllByAuthorAndStatusIn(employee2,
                List.of(RequestStatus.ON_THE_LINE,
                        RequestStatus.DONE,
                        RequestStatus.WARNING))).thenReturn(Collections.singletonList(request2));
        actual = requestService.getOnTheLineRequest(employee2);
        assertEquals(RequestStatus.ON_THE_LINE, actual.requestStatus());
        assertFalse(actual.ewbPath());
        
        var position = Instancio.of(Position.class)
                                .set(field(Position::getOrganization), organization).create();
        var employee3 = Instancio.of(Employee.class)
                                .set(field(Employee::getOrganization), organization)
                                .set(field(Employee::getDepartment), department)
                                .set(field(Employee::getPosition), position)
                                .create();
        var transport3 = Instancio.of(Transport.class)
                                  .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                  .set(field(Transport::getStateNumber), "А010ЕК51")
                                  .set(field(Transport::getOrganizations), Set.of(organization))
                                  .create();
        var request3 = createRequest(employee3, transport3, organization.getId());
        request3.setId(UUID.randomUUID());
        request3.setStatus(RequestStatus.ON_THE_LINE);
        request3.setCreationTime(LocalDateTime.now(clock).minusDays(2));
        var request4 = createRequest(employee3, transport3, organization.getId());
        request4.setId(UUID.randomUUID());
        request4.setStatus(RequestStatus.WARNING);
        request4.setCreationTime(LocalDateTime.now(clock).minusDays(3));
        var request5 = createRequest(employee3, transport3, organization.getId());
        request5.setId(UUID.randomUUID());
        request5.setStatus(RequestStatus.DONE);
        request5.setCreationTime(LocalDateTime.now(clock).minusDays(1));
        when(requestRepository.findAllByAuthorAndStatusIn(employee3,
                List.of(RequestStatus.ON_THE_LINE,
                        RequestStatus.DONE,
                        RequestStatus.WARNING))).thenReturn(List.of(request3, request4, request5));
        actual = requestService.getOnTheLineRequest(employee3);
        assertEquals(RequestStatus.DONE, actual.requestStatus());
        assertFalse(actual.ewbPath());
        
        doReturn(Collections.emptyList()).when(requestRepository).findAllByAuthorAndStatusIn(any(Employee.class), anyList());
        actual = requestService.getOnTheLineRequest(employee3);
        assertFalse(actual.ewbPath());
    }
    
    @Test
    @DisplayName("Проверка на линии по ЭПЛ")
    void getOnTheLineRequestWithEwb() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(true).when(ewbPathService).calculatingClientPath(employee1);
        doReturn("UTC+03:00").when(departmentTimeZoneService).getTimeZoneByDepartmentId(any(UUID.class));
        
        var ewb = Instancio.create(Ewb.class);
        ewb.getRequest().setStatus(RequestStatus.ON_THE_LINE);
        ewb.getRequest().setCreationTime(LocalDateTime.now(clock));
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findEwbForRequestOnTheLine(any(UUID.class), any(LocalDate.class), anyList());
        
        var actual = requestService.getOnTheLineRequest(employee1);
        assertNotNull(actual);
        assertTrue(actual.ewbPath());
        
        var expected = ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of("UTC+03:00")).toLocalDate();
        verify(ewbRepository).findEwbForRequestOnTheLine(any(UUID.class), eq(expected), anyList());
        
        ewb.setRequest(null);
        actual = requestService.getOnTheLineRequest(employee1);
        assertTrue(actual.ewbPath());
        assertEquals(ewb.isQrCode(), actual.qrCode());
    }
    
    @Test
    @DisplayName("Проверка на линии по ЭПЛ после дедлайна старого пути")
    void getOnTheLineRequestWithEwbAfterDeadline() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(true).when(ewbPathService).calculatingClientPath(employee1);
        doReturn("UTC+03:00").when(departmentTimeZoneService).getTimeZoneByDepartmentId(any(UUID.class));
        var ewb = Instancio.create(Ewb.class);
        ewb.getRequest().setStatus(RequestStatus.ON_THE_LINE);
        ewb.getRequest().setCreationTime(LocalDateTime.now(clock));
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findEwbForRequestOnTheLine(any(UUID.class), any(LocalDate.class), anyList());
        
        var actual = requestService.getOnTheLineRequest(employee1);
        assertNotNull(actual);
        assertTrue(actual.ewbPath());
        assertNotNull(actual.requestId());
        assertNotNull(actual.requestStatus());
        
        var expected = ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of("UTC+03:00")).toLocalDate();
        verify(ewbRepository).findEwbForRequestOnTheLine(any(UUID.class), eq(expected), anyList());
    }
    
    @Test
    @DisplayName("Проверка на линии с ЭПЛ с заявкой не в статусе 'НА ЛИНИИ'")
    void onTheLineWithEwbAndRequestStatusIsNotOnTheLine() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(true).when(ewbPathService).calculatingClientPath(employee1);
        doReturn("UTC+03:00").when(departmentTimeZoneService).getTimeZoneByDepartmentId(any(UUID.class));
        
        var ewb = Instancio.create(Ewb.class);
        ewb.getRequest().setStatus(RequestStatus.IN_PROGRESS);
        ewb.getRequest().setCreationTime(LocalDateTime.now(clock));
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findEwbForRequestOnTheLine(any(UUID.class), any(LocalDate.class), anyList());
        
        var actual = requestService.getOnTheLineRequest(employee1);
        assertNotNull(actual);
        assertTrue(actual.ewbPath());
        assertNull(actual.transport());
        
        var expected = ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of("UTC+03:00")).toLocalDate();
        verify(ewbRepository).findEwbForRequestOnTheLine(any(UUID.class), eq(expected), anyList());
    }

    @Test
    @DisplayName("Проверка В процессе проверки")
    void getInProgressRequest() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var userId = UUID.randomUUID();
        var request = createRequest(employee1, transport, organization.getId());
        request.setId(UUID.randomUUID());
        request.setStatus(RequestStatus.IN_PROGRESS);
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doReturn(Collections.emptyList()).when(requestRepository).findAllByAuthorAndStatusIn(any(Employee.class), anyList());
        assertNull(requestService.getInProgressRequest(userId));
        when(requestRepository.findAllByAuthorAndStatusIn(any(Employee.class), anyList()))
                .thenReturn(Collections.singletonList((request)));
        doReturn(new ActiveResponse(request.getId())).when(requestMapper).requestToActiveResponse(request);
        assertEquals(request.getId(), requestService.getInProgressRequest(userId).id());
        
        var position = Instancio.of(Position.class)
                                .set(field(Position::getOrganization), organization).create();
        var employee3 = Instancio.of(Employee.class)
                                .set(field(Employee::getOrganization), organization)
                                .set(field(Employee::getDepartment), department)
                                .set(field(Employee::getPosition), position)
                                .create();
        var transport3 = Instancio.of(Transport.class)
                                  .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                  .set(field(Transport::getStateNumber), "А010ЕК51")
                                  .set(field(Transport::getOrganizations), Set.of(organization))
                                  .create();
        var request3 = createRequest(employee3, transport3, organization.getId());
        request3.setId(UUID.randomUUID());
        request3.setStatus(RequestStatus.ON_THE_LINE);
        request3.setCreationTime(LocalDateTime.now(clock).minusDays(2));
        var request4 = createRequest(employee3, transport3, organization.getId());
        request4.setId(UUID.randomUUID());
        request4.setStatus(RequestStatus.WARNING);
        request4.setCreationTime(LocalDateTime.now(clock).minusDays(3));
        var request5 = createRequest(employee3, transport3, organization.getId());
        request5.setId(UUID.randomUUID());
        request5.setStatus(RequestStatus.IN_PROGRESS);
        request5.setCreationTime(LocalDateTime.now(clock).minusDays(1));
        when(requestRepository.findAllByAuthorAndStatusIn(any(Employee.class), anyList()))
                .thenReturn(List.of(request3, request4, request5));
        assertNull(requestService.getInProgressRequest(userId));
        assertEquals(RequestStatus.ON_THE_LINE, request3.getStatus());
        assertEquals(RequestStatus.WARNING, request4.getStatus());
        assertEquals(RequestStatus.IN_PROGRESS, request5.getStatus());
    }
    
    @Test
    void shouldChangeStatusForCallingTelemechanic() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var requestCaptor = ArgumentCaptor.forClass(Request.class);
        var requestId = UUID.randomUUID();
        var request = Request.builder().status(RequestStatus.DONE).build();
        var userId = UUID.randomUUID();
        request.getChecks().add(Check.builder().checkType(CheckType.VEHICLE_NUMBER).checkStatus(CheckStatus.DONE).build());
        doReturn(Optional.of(request)).when(requestRepository).findById(any(UUID.class));
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        var exception = assertThrows(BadRequestException.class, () -> requestService.changeStatusForCallTelemechanic(requestId, userId));
        assertEquals("Невозможно перевести заявку из статуса DONE", exception.getMessage());
        
        request.setStatus(RequestStatus.IN_PROGRESS);
        assertDoesNotThrow(() -> requestService.changeStatusForCallTelemechanic(requestId, userId));
        verify(requestRepository, times(1)).save(requestCaptor.capture());
        assertEquals(RequestStatus.DONE, requestCaptor.getValue().getStatus());
        assertEquals(LocalDateTime.now(fixedClock), requestCaptor.getValue().getChecksFinishedTime());
        
        request.setStatus(RequestStatus.IN_PROGRESS);
        request.getChecks().add(Check.builder().checkType(CheckType.OIL_LEVEL).checkStatus(CheckStatus.IN_PROGRESS).build());
        assertDoesNotThrow(() -> requestService.changeStatusForCallTelemechanic(requestId, userId));
        verify(requestRepository, times(2)).save(requestCaptor.capture());
        assertEquals(RequestStatus.WARNING, requestCaptor.getValue().getStatus());
        assertEquals(LocalDateTime.now(fixedClock), requestCaptor.getValue().getChecksFinishedTime());
    }
    
    @Test
    void shouldUpdateRequest() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var requestId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var request = Instancio.create(Request.class);
        request.setStatus(RequestStatus.WARNING);
        var newData = new MonitoringRequestDto(
                request.getId(),
                request.getHumanReadableId(),
                request.getCreationTime(),
                request.getStatus(),
                null,
                new EmployeeDto(request.getAuthor().getId(),
                                request.getAuthor().getFirstName(),
                                request.getAuthor().getLastName(),
                                request.getAuthor().getPatronymic(),
                                request.getAuthor().getPersonnelNumber(),
                                request.getAuthor().getOrganization().getId(),
                                request.getAuthor().getOrganization().getOfficialName()),
                new TransportDto(request.getTransport().getId(),
                                 request.getTransport().getStateNumber(),
                                 request.getTransport().getBrand(),
                                 request.getTransport().getModel(),
                                 null, null, null),
                new ChecksStatus(0, 0, 0, 0),
                Collections.emptyList(),
                "comment"
                
        );
        
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doReturn(Optional.of(request)).when(requestRepository).findById(requestId);
        doReturn(Instancio.create(RequestHistory.class)).when(requestHistoryRepository).save(any(RequestHistory.class));
        doReturn(request).when(requestRepository).save(any(Request.class));
        
        requestService.update(requestId, newData, userId);
        verify(requestRepository, times(1)).save(any(Request.class));
    }
    
    @Test
    void shouldReturnRequestForMonitoring() {
        var request = Instancio.create(Request.class);
        var userId = UUID.randomUUID();
        var mappedRequest = new MonitoringRequestDto(
                request.getId(),
                request.getHumanReadableId(),
                request.getCreationTime(),
                request.getStatus(),
                null,
                new EmployeeDto(request.getAuthor().getId(),
                                request.getAuthor().getFirstName(),
                                request.getAuthor().getLastName(),
                                request.getAuthor().getPatronymic(),
                                request.getAuthor().getPersonnelNumber(),
                                request.getAuthor().getOrganization().getId(),
                                request.getAuthor().getOrganization().getOfficialName()),
                new TransportDto(request.getTransport().getId(),
                                 request.getTransport().getStateNumber(),
                                 request.getTransport().getBrand(),
                                 request.getTransport().getModel(),
                                 null, null, null),
                new ChecksStatus(0, 0, 0, 0),
                Collections.emptyList(),
                "comment"
        
        );
        var ewb = Instancio.create(Ewb.class);
        var requestId = request.getId();
        
        doReturn(Optional.of(request)).when(requestRepository).findById(requestId);
        doReturn(mappedRequest).when(requestMapper).requestToMonitoringRequestDto(request);
        doReturn(Optional.of(ewb)).when(ewbRepository).findEwbWithTransportByRequestId(requestId);
        doReturn(Collections.emptyList()).when(ewbTitleRepository).findByEwbId(ewb.getId());
        var ewbNotFoundException = assertThrows(EwbNotFoundException.class, () -> requestService.getForMonitoring(requestId, userId));
        assertEquals(EwbNotFoundException.MSG_FORMAT.formatted(ewb.getId()), ewbNotFoundException.getMessage());
        
        doReturn(List.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbId(ewb.getId());
        doReturn(List.of(new MonitorCheckTreeDto(UUID.randomUUID(),
                                                 CheckTypeMonitoring.VEHICLE_NUMBER,
                                                 CheckStatus.DONE,
                                                 1,
                                                 3,
                                                 List.of(UUID.randomUUID()),
                                                 "no comment",
                                                 null))).when(checkHelper).createChecksTree(request);
        var actual = requestService.getForMonitoring(requestId, userId);
        assertNotNull(actual);
        assertEquals(ewb.getId(), actual.ewb().ewbId());
        assertEquals(ewb.getTransport().getType(), actual.transport().transportType());
        assertEquals(1, actual.checks().size());
        assertEquals(CheckTypeMonitoring.VEHICLE_NUMBER, actual.checks().get(0).checkType());
        assertThat(actual.author())
            .extracting(
                EmployeeDto::id,
                EmployeeDto::firstName,
                EmployeeDto::lastName,
                EmployeeDto::patronymic,
                EmployeeDto::personnelNumber,
                EmployeeDto::organizationId,
                EmployeeDto::organizationOfficialName
            )
            .containsExactly(
                request.getAuthor().getId(),
                request.getAuthor().getFirstName(),
                request.getAuthor().getLastName(),
                request.getAuthor().getPatronymic(),
                request.getAuthor().getPersonnelNumber(),
                request.getAuthor().getOrganization().getId(),
                request.getAuthor().getOrganization().getOfficialName()
            );
    }
    
    @Test
    void close() {
        var employee = Instancio.create(Employee.class);
        var employeeId = employee.getId();
        var request = Instancio.create(Request.class);
        request.setStatus(RequestStatus.IN_PROGRESS);
        var requestId = request.getId();
        var requestHistoryCaptor = ArgumentCaptor.forClass(RequestHistory.class);
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(employee).when(employeeService).getByUserId(employee.getId());
        doReturn(Optional.of(request)).when(requestRepository).findById(request.getId());
        
        var authorException = assertThrows(CloseRequestException.class, () -> requestService.close(requestId, employeeId));
        assertEquals(MSG_AUTHOR, authorException.getMessage());
        
        request.setAuthor(employee);
        
        var requestStatusException = assertThrows(CloseRequestException.class, () -> requestService.close(requestId, employeeId));
        assertEquals(MSG_STATUS, requestStatusException.getMessage());
        
        requestStatusException = assertThrows(CloseRequestException.class, () -> requestService.close(requestId, employeeId));
        assertEquals(MSG_STATUS, requestStatusException.getMessage());
        
        request.setStatus(RequestStatus.ON_THE_LINE);
        requestService.close(requestId, employeeId);
        
        verify(requestHistoryRepository, times(1)).save(requestHistoryCaptor.capture());
        assertEquals(RequestStatus.FINISHED, requestHistoryCaptor.getValue().getStatus());
        assertEquals(employee, requestHistoryCaptor.getValue().getInitiator());
        assertEquals(LOCAL_DATE, requestHistoryCaptor.getValue().getChangeTime());
    }
    
    @Test
    @DisplayName("Авто обновление статуса тех заявок: заявки не найдены")
    void statusAutoUpdateWhenNotFound() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(List.of()).when(requestRepository).findAllByStatusInAndCreationTimeBetween(Set.of(RequestStatus.IN_PROGRESS, RequestStatus.WARNING, RequestStatus.DONE),
                                                                                           LocalDateTime.now(fixedClock).minusDays(2),
                                                                                           LocalDateTime.now(fixedClock).minusDays(1));
        requestService.statusAutoUpdate();
        verify(requestRepository, never()).saveAll(anyList());
        verify(requestHistoryRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Авто обновление статуса тех заявки: успех")
    void statusAutoUpdate(){
        var historyCaptor = ArgumentCaptor.forClass(List.class);
        var requestCaptor = ArgumentCaptor.forClass(List.class);
        var comment = "Статус заявки изменен пользователем: Система (Планировщик)";
        var request = Instancio.of(Request.class)
                               .set(field(Request::getStatus), RequestStatus.IN_PROGRESS)
                               .set(field(Request::getCreationTime), LocalDateTime.now(fixedClock).minusDays(1))
                               .create();
        
        var request2 = Instancio.of(Request.class)
                               .set(field(Request::getStatus), RequestStatus.ON_THE_LINE)
                               .set(field(Request::getCreationTime), LocalDateTime.now(fixedClock).minusDays(1))
                               .create();
        
        var requestHistory = Instancio.of(RequestHistory.class)
                                              .set(field(RequestHistory::getStatus), RequestStatus.EXPIRED)
                                                      .set(field(RequestHistory::getOldStatus), RequestStatus.IN_PROGRESS)
                                              .set(field(RequestHistory::getRequestId), request.getId())
                                              .set(field(RequestHistory::getInitiator), null)
                                              .set(field(RequestHistory::getComment), comment)
                                              .create();
        
        var requestHistory2 = Instancio.of(RequestHistory.class)
                                      .set(field(RequestHistory::getStatus), RequestStatus.FINISHED)
                                      .set(field(RequestHistory::getOldStatus), request2.getStatus())
                                      .set(field(RequestHistory::getRequestId), request2.getId())
                                      .set(field(RequestHistory::getInitiator), null)
                                      .set(field(RequestHistory::getComment), comment)
                                      .create();
        
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(List.of(request)).when(requestRepository)
                                  .findAllByStatusInAndCreationTimeBetween(Set.of(RequestStatus.IN_PROGRESS, RequestStatus.WARNING, RequestStatus.DONE),
                                                                           LocalDateTime.now(fixedClock).minusDays(2),
                                                                           LocalDateTime.now(fixedClock).minusDays(1));
        doReturn(requestHistory).doReturn(requestHistory2).when(requestMapper)
                                .requestToRequestHistory(any(Request.class), any(RequestStatus.class), anyString(), any());
        
        requestService.statusAutoUpdate();
        
        verify(requestRepository, times(1)).saveAll(requestCaptor.capture());
        verify(requestHistoryRepository, times(1)).saveAll(historyCaptor.capture());
        
        var list = requestCaptor.getValue();
        assertThat(list).hasSize(1);
        var requestCapture = (Request) list.getFirst();
        assertThat(requestCapture).isNotNull();
        assertThat(requestCapture.getId()).isEqualTo(request.getId());
        assertThat(requestCapture.getStatus()).isEqualTo(RequestStatus.EXPIRED);
        
        var historyList = historyCaptor.getValue();
        assertThat(historyList).hasSize(1);
        var history = (RequestHistory) historyList.getFirst();
        assertThat(history).isNotNull();
        assertThat(history.getStatus()).isEqualTo(RequestStatus.EXPIRED);
        assertThat(history.getInitiator()).isNull();
        assertThat(history.getChangeTime()).isNotNull();
        assertThat(history.getRequestId()).isEqualTo(request.getId());
        assertThat(history.getComment()).isEqualTo(comment);
        assertThat(history.getOldStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
    }
    
    @Test
    void updateRequestThirdTitleSent() {
        var argumentCaptor = ArgumentCaptor.forClass(Request.class);
        var request = Instancio.create(Request.class);
        var emlpoyee = Instancio.create(Employee.class);
        
        doReturn(emlpoyee).when(employeeService).getByUserId(any(UUID.class));
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        requestService.updateRequestThirdTitleSent(request, emlpoyee.getId());
        verify(requestRepository, times(1)).save(argumentCaptor.capture());
        
        var requestToSave = argumentCaptor.getValue();
        assertThat(requestToSave).isNotNull();
        assertThat(requestToSave.getInspector().getId()).isEqualTo(emlpoyee.getId());
    }
}