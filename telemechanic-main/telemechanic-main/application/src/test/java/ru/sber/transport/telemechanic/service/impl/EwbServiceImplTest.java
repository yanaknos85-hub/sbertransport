package ru.sber.transport.telemechanic.service.impl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.util.JAXBSource;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.telemechanic.client.KorusClient;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.config.properties.KorusProperties;
import ru.sber.transport.telemechanic.database.dao.*;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.database.model.DrivingLicense;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.dispatcher.GetOrganizationDispatcherResponse;
import ru.sber.transport.telemechanic.dto.ewb.*;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleFile;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchAllOrganizationsRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchResponseDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchSelfOrganizationRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.SecondTitleFile;
import ru.sber.transport.telemechanic.dto.ewb.second_title.SecondTitleForm;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SendAndSaveTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitlesRequest;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.fourth_title.FourthTitleFile;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title.ThirdTitleFile;
import ru.sber.transport.telemechanic.dto.ewb.xjb.*;
import ru.sber.transport.telemechanic.dto.ewb.xjb.Contact;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryResponse;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbSearchDto;
import ru.sber.transport.telemechanic.enumerate.*;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.helper.CentralOrganizationHelper;
import ru.sber.transport.telemechanic.helper.CheckHelper;
import ru.sber.transport.telemechanic.human_readable_id.constant.Prefix;
import ru.sber.transport.telemechanic.mapper.*;
import ru.sber.transport.telemechanic.messaging.sender.EwbClosedSender;
import ru.sber.transport.telemechanic.messaging.sender.OdometerSender;
import ru.sber.transport.telemechanic.messaging.sender.message.EwbClosedMessage;
import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;
import ru.sber.transport.telemechanic.service.*;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.*;
import static ru.sber.transport.telemechanic.exception.LitreageValidationException.*;

@ExtendWith(MockitoExtension.class)
class EwbServiceImplTest {
    
    @Mock
    private KorusClient korusClient;
    @Mock
    private SQGenerator sqGenerator;
    @Mock
    private EwbMapper ewbMapper;
    @Mock
    private TelemedicineMapper telemedicineMapper;
    @Mock
    private RequestMapper requestMapper;
    @Mock
    private EwbRepository ewbRepository;
    @Mock
    private EwbTitleRepository ewbTitleRepository;
    @InjectMocks
    private EwbServiceImpl ewbService;
    @Mock
    private KorusProperties korusProperties;
    @Mock
    private FileService fileService;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private TransportService transportService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private SignatureVerifier signatureVerifier;
    @Mock
    private OdometerSender odometerSender;
    @Captor
    private ArgumentCaptor<OdometerHistoryValueMessage> odometerHistoryValueMessageArgumentCaptor;
    @Mock
    private RequestService requestService;
    @Mock
    private CheckService checkService;
    @Mock
    private CheckPhotoService checkPhotoService;
    @Mock
    private CheckHelper checkHelper;
    @Mock
    private FifthTitleMapper fifthTitleMapper;
    @Mock
    private EwbRegistryDynamicRepository ewbRegistryDynamicRepository;
    @Mock
    private EwbFilenameService ewbFilenameService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private FleetOwnerOrganizationService fleetOwnerOrganizationService;
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private CentralOrganizationHelper centralOrganizationHelper;
    @Mock
    private DispatcherService dispatcherService;
    @Mock
    private OrganizationAddressService organizationAddressService;
    @Mock
    private DriverService driverService;
    @Mock
    private EwbFirstTitleMapper ewbFirstTitleMapper;
    @Mock
    private DrivingLicenseService drivingLicenseService;
    @Mock
    private OrganizationMedicalLicenseService organizationMedicalLicenseService;
    @Mock
    private RequestHistoryRepository requestHistoryRepository;
    @Mock
    private MedicRequestHistoryRepository medicRequestHistoryRepository;
    @Mock
    private EwbHistoryRepository ewbHistoryRepository;
    @Mock
    private EwbValidationService ewbValidationService;
    @Mock
    private DepartmentTimeZoneService departmentTimeZoneService;
    @Captor
    private ArgumentCaptor<Ewb> ewbArgumentCaptor;
    @Mock
    private Clock clock;
    @Mock
    private EwbClosedSender ewbClosedSender;
    
    public static final LocalDate CURRENT_DATE = LocalDate.of(2023, 2, 10);
    public final Clock fixedClock = Clock.fixed(CURRENT_DATE.atStartOfDay().toInstant(ZoneOffset.UTC),
                                                ZoneId.of(ZoneOffset.UTC.getId()));
    private static final List<EwbStatus> HAVE_ACTIVE_EWB_CHECK_STATUSES =
            List.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS, ON_THE_LINE, IN_GARAGE);
    
    @Test
    void getToken() {
        var expected = new TokenDto("token");
        doReturn("login").when(korusProperties).login();
        doReturn("password").when(korusProperties).password();
        when(korusClient.auth(any())).thenReturn(expected);
        var actual = ewbService.auth();
        assertNotNull(actual);
        assertEquals(expected, actual);
    }
    
    @Test
    void getUUID() {
        var expected = new UuidDto(UUID.fromString("F7AEC7E7-0BF4-4924-A637-A7572AFA506F"));
        doReturn(ResponseEntity.ok(expected)).when(korusClient).createNewQr();
        var actual = ewbService.getUUID();
        assertNotNull(actual);
        assertEquals(expected, actual);
    }
    
    @Test
    @SneakyThrows
    void generateFirstTitle() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var request = Instancio.of(FirstTitleRequest.class)
                               .set(Select.field(FirstTitleRequest::startDate), LocalDate.now())
                               .set(Select.field(FirstTitleRequest::finishDate), LocalDate.now().plusDays(2))
                               .create();
        var organizationId = UUID.randomUUID();
        
        var organization = Instancio.of(Organization.class)
                                    .set(Select.field(Organization::getId), organizationId)
                                    .create();
        var employee = Instancio.of(Employee.class)
                                .set(Select.field(Employee::getOrganization), organization)
                                .create();
        var dispatcher = Instancio.of(Dispatcher.class)
                                  .set(Select.field(Dispatcher::getEmployee), employee)
                                  .create();
        var organizationAddress = Instancio.create(OrganizationAddress.class);
        var dispatcherOrganization = Instancio.create(GetOrganizationDispatcherResponse.class);
        var driver = Instancio.of(Driver.class)
                              .set(Select.field(Driver::getEmployee), employee)
                              .create();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getOrganizations), Set.of(organization))
                                 .create();
        var userId = UUID.randomUUID();
        doReturn(dispatcher).when(dispatcherService).getByEmployeeIdAndActive(userId);
        doNothing().when(fleetOwnerOrganizationService).validateFleetOwnerOrganization(dispatcher.getEmployee().getOrganization().getId());
        doReturn(organizationAddress).when(organizationAddressService).get(dispatcher.getEmployee().getOrganization().getId());
        doReturn(dispatcherOrganization).when(dispatcherService).getSelfOrganizationInfo(userId);
        doReturn(Instancio.create(Driver.class)).when(driverService).getActiveDriverById(request.driverId());
        doReturn(Instancio.create(Transport.class)).when(transportService).getTransportInUseById(request.transportId());
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> ewbService.generateFirstTitle(request, userId))
                .withMessage("Дата окончания действия путевого листа должна быть равна Дате начала либо Дате начала плюс один день");
        
        var request2 = Instancio.of(FirstTitleRequest.class)
                                .set(Select.field(FirstTitleRequest::startDate), LocalDate.now(fixedClock))
                                .set(Select.field(FirstTitleRequest::finishDate), LocalDate.now(fixedClock))
                                .create();
        doReturn(driver).when(driverService).getActiveDriverById(request2.driverId());
        doReturn(transport).when(transportService).getTransportInUseById(request2.transportId());
        doReturn(Set.of(new EwbContractDetails(InspectionType.TECHNIC,
                                               LocalDate.now(fixedClock).minusYears(10),
                                               LocalDate.now(fixedClock).minusYears(5))))
                .when(ewbTariffService).validateEwbTariff(request2.tariffDepartmentId());
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> ewbService.generateFirstTitle(request2, userId))
                .withMessage("Даты ЭПЛ не попадают в диапазон дат договора: %s - %s".formatted(
                        LocalDate.now(fixedClock).minusYears(10).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        LocalDate.now(fixedClock).minusYears(5).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        doReturn(Set.of(new EwbContractDetails(
                                InspectionType.TECHNIC,
                                LocalDate.now(fixedClock).minusYears(10),
                                LocalDate.now(fixedClock).plusYears(10))
                       )).when(ewbTariffService).validateEwbTariff(request2.tariffDepartmentId());
        doReturn(organization.getDigitId()).when(employeeService).getDigitIdByUserId(userId);
        doReturn("PL-0001-0000001").when(sqGenerator).getNextId(Prefix.PL, organization.getDigitId());
        doReturn("file-name").when(ewbFilenameService).generateFilename(EwbTitleType.FIRST, dispatcherOrganization.organization().id(),
                                                                        request2.tariffDepartmentId());
        doReturn(createFile()).when(ewbFirstTitleMapper).firstTitleRequestToFile(
                "PL-0001-0000001",
                "file-name",
                request2,
                dispatcherOrganization,
                driver,
                transport,
                organizationAddress,
                dispatcher,
                LocalDateTime.now(fixedClock)
                );
        var actual = ewbService.generateFirstTitle(request2, userId);
        assertThat(actual).isNotNull();
        assertThat(actual.humanReadableId()).isEqualTo("PL-0001-0000001");
    }
    
    @Test
    @SneakyThrows
    void generateSecondTitle() {
        var userId = UUID.randomUUID();
        var titleForm1 = Instancio.create(SecondTitleForm.class);
        var titleForm2 = Instancio.create(SecondTitleForm.class);
        var titleForm3 = Instancio.create(SecondTitleForm.class);
        var titleForm4 = Instancio.create(SecondTitleForm.class);
        var titleForm5 = Instancio.create(SecondTitleForm.class);
        var ewb1 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var ewb2 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var ewb3 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE.plusDays(1))
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var ewb4 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var employee = Instancio.create(Employee.class);
        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
        var title = Instancio.of(EwbTitle.class)
                             .set(field(EwbTitle::getType), EwbTitleType.FIRST)
                             .create();
        var secondTitleFile = Instancio.create(SecondTitleFile.class);
        var fileData = Instancio.create(FileData.class);
        var fileName = UUID.randomUUID().toString();
        var drivingLicense = Instancio.create(DrivingLicense.class);
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(Optional.of(ewb1)).when(ewbRepository).findById(titleForm1.ewbId());
        doReturn(Optional.empty()).when(ewbRepository).findById(titleForm2.ewbId());
        doReturn(Optional.of(ewb2)).when(ewbRepository).findById(titleForm3.ewbId());
        doReturn(Optional.of(ewb3)).when(ewbRepository).findById(titleForm4.ewbId());
        doReturn(Optional.of(ewb4)).when(ewbRepository).findById(titleForm5.ewbId());
        doReturn(employee).when(employeeService).getByUserId(userId);
        doReturn(organizationMedicalLicense).when(organizationMedicalLicenseService).getMedicalLicense(ewb1.getTariffDepartmentId());
        doReturn(organizationMedicalLicense).when(organizationMedicalLicenseService).getMedicalLicense(ewb2.getTariffDepartmentId());
        doReturn(organizationMedicalLicense).when(organizationMedicalLicenseService).getMedicalLicense(ewb3.getTariffDepartmentId());
        doReturn(organizationMedicalLicense).when(organizationMedicalLicenseService).getMedicalLicense(ewb4.getTariffDepartmentId());
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb1.getId(), EwbTitleType.SECOND);
        doReturn(Optional.of(title)).when(ewbTitleRepository).findByEwbIdAndType(ewb2.getId(), EwbTitleType.SECOND);
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb3.getId(), EwbTitleType.SECOND);
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb4.getId(), EwbTitleType.SECOND);
        doReturn(Optional.of(title)).when(ewbTitleRepository).findByEwbIdAndType(ewb1.getId(), EwbTitleType.FIRST);
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb4.getId(), EwbTitleType.FIRST);
        doReturn(fileData).when(fileService).get(title.getSignatureS3FileName());
        doReturn(drivingLicense).when(drivingLicenseService).get(ewb1.getDriverLicenseId());
        doReturn(secondTitleFile).when(ewbMapper).secondTitleFormToFile(
                any(UUID.class),
                any(LocalDateTime.class),
                any(OrganizationMedicalLicense.class),
                any(Driver.class),
                any(DrivingLicense.class),
                any(EwbTitle.class),
                anyString(),
                any(Employee.class));
        doReturn(fileName).when(ewbFilenameService).generateFilename(EwbTitleType.SECOND,
                                                                     ewb1.getOrganization().getId(),
                                                                     ewb1.getTariffDepartmentId());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        var actual = ewbService.generateSecondTitle(titleForm1, userId);
        assertThat(actual.fileName()).isEqualTo(fileName);
        assertThat(actual.content()).isNotEmpty();
        var actualMedicRequest = ewb1.getMedicRequest();
        assertThat(actualMedicRequest.getSystPressure()).isEqualTo(titleForm1.request().systPressure());
        assertThat(actualMedicRequest.getDyastPressure()).isEqualTo(titleForm1.request().dyastPressure());
        assertThat(actualMedicRequest.getPulse()).isEqualTo(titleForm1.request().pulse());
        assertThat(actualMedicRequest.getTemperature()).isEqualTo(titleForm1.request().temperature());
        assertThat(actualMedicRequest.getBloodAlcohol()).isEqualTo(titleForm1.request().bloodAlcohol());
        assertThat(actualMedicRequest.getComment()).isEqualTo(titleForm1.request().comment());
        assertThat(ewb1.getMedic()).isEqualTo(employee);
        assertThat(ewb1.getOrganizationMedicalLicenseId()).isEqualTo(organizationMedicalLicense.getId());
        assertThat(ewb1.getMedicDecisionTime()).isEqualTo(LocalDateTime.now(fixedClock));
        assertThatThrownBy(() -> ewbService.generateSecondTitle(titleForm2, userId))
                .isInstanceOf(EwbNotFoundException.class)
                .hasMessage("ЭПЛ с идентификатором id=%s не найден!", titleForm2.ewbId());
        assertThatThrownBy(() -> ewbService.generateSecondTitle(titleForm3, userId))
                .isInstanceOf(EwbTitleAlreadyExistsException.class)
                .hasMessage("У ЭПЛ с id=%s уже есть Второй титул!", ewb2.getId());
        assertThatThrownBy(() -> ewbService.generateSecondTitle(titleForm4, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Дата принятия решения должна совпадать с датой начала рейса ЭПЛ!");
        assertThatThrownBy(() -> ewbService.generateSecondTitle(titleForm5, userId))
                .isInstanceOf(EwbGenerateException.class)
                .hasMessage("При формировании xml произошла ошибка!");
    }
    
    @Test
    void searchSelfOrganization() {
        var request = new EwbSearchSelfOrganizationRequestDto("A77", "A77",
                                                              Set.of(EwbStatus.ON_THE_LINE, EwbStatus.IN_GARAGE),
                                                              new DateRange(LocalDateTime.MIN, LocalDateTime.MAX),
                                                              new DateRange(LocalDateTime.MIN, LocalDateTime.MAX),
                                                              new PageSettingDto(0, 3));
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(any(UUID.class));
        when(centralOrganizationHelper.map(any(), any(Supplier.class), any(Supplier.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(1, Supplier.class).get());
        when(ewbMapper.ewbToEwbSearchResponseDtoWithOrganizationName(any(), any())).thenReturn(Instancio.create(EwbSearchResponseDto.class));
        doReturn(new PageImpl<>(List.of(Instancio.create(Ewb.class),
                                        Instancio.create(Ewb.class),
                                        Instancio.create(Ewb.class),
                                        Instancio.create(Ewb.class)), Pageable.ofSize(4), 10)
                ).when(ewbRepository).findAll(ArgumentMatchers.<Specification<Ewb>>any(), any(Pageable.class));
        var result = ewbService.searchSelfOrganization(request, UUID.randomUUID());
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(4, result.getContent().size());
        
        when(centralOrganizationHelper.map(any(), any(Supplier.class), any(Supplier.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(2, Supplier.class).get());
        when(ewbMapper.ewbToEwbSearchResponseDtoWithOrganizationName(any(), any()))
                .thenReturn(Instancio.create(EwbSearchResponseDto.class));
        result = ewbService.searchSelfOrganization(request, UUID.randomUUID());
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(4, result.getContent().size());
    }
    
    @Test
    void searchAllOrganizations() {
        var request = new EwbSearchAllOrganizationsRequestDto("A77", "A77", UUID.randomUUID(),
                                                              Set.of(EwbStatus.ON_THE_LINE, EwbStatus.IN_GARAGE),
                                                              new DateRange(LocalDateTime.MIN, LocalDateTime.MAX),
                                                              new DateRange(LocalDateTime.MIN, LocalDateTime.MAX),
                                                              new PageSettingDto(0, 3));
        when(centralOrganizationHelper.map(any(), any(Supplier.class), any(Supplier.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(1, Supplier.class).get());
        when(ewbMapper.ewbToEwbSearchResponseDtoWithOrganizationName(any(), any())).thenReturn(Instancio.create(EwbSearchResponseDto.class));
        doReturn(new PageImpl<>(List.of(Instancio.create(Ewb.class),
                                        Instancio.create(Ewb.class),
                                        Instancio.create(Ewb.class),
                                        Instancio.create(Ewb.class)), Pageable.ofSize(4), 10)
                ).when(ewbRepository).findAll(ArgumentMatchers.<Specification<Ewb>>any(), any(Pageable.class));
        var result = ewbService.searchAllOrganizations(request);
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(4, result.getContent().size());
        
        when(centralOrganizationHelper.map(any(), any(Supplier.class), any(Supplier.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(2, Supplier.class).get());
        when(ewbMapper.ewbToEwbSearchResponseDtoWithOrganizationName(any(), any()))
                .thenReturn(Instancio.create(EwbSearchResponseDto.class));
        result = ewbService.searchAllOrganizations(request);
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(4, result.getContent().size());
    }
    
    @Test
    void getEwb() {
        doReturn(Optional.of(
                Instancio.of(Ewb.class)
                        .set(field(Ewb::getTimeZone), "UTC+03:00")
                        .create()
                            )).when(ewbRepository).findById(any(UUID.class));
        doReturn(Instancio.create(GetEwbDto.class)).when(ewbMapper).ewbToGetEwbDto(any(Ewb.class));
        doReturn(true).when(centralOrganizationHelper).isCentral(any(UUID.class));
        
        var uuid = UUID.randomUUID();
        var result = ewbService.getEwb(uuid);
        assertNotNull(result);
        
        doReturn(Optional.empty()).when(ewbRepository).findById(any(UUID.class));
        var exception = assertThrows(EntityNotFoundException.class, () -> ewbService.getEwb(uuid));
        assertEquals(String.format("Data not found: Entity: %s, ID: %s", Ewb.class.getSimpleName(), uuid), exception.getMessage());
    }
    
    @Test
    void getEwb_WithMedicContractor() {
        var id = UUID.randomUUID();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getTimeZone), "UTC+03:00")
                           .set(field(Ewb::getMedic), null)
                           .set(field(Ewb::getMedicContractor), Instancio.of(MedicContractor.class)
                                                                         .set(field(MedicContractor::getFullName), "АЛЕКСЕЙ АЛЕКСЕЕВИЧ")
                                                                         .create())
                           .create();
        var mappedEwb = Instancio.create(GetEwbDto.class);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(id);
        doReturn(mappedEwb).when(ewbMapper).ewbToGetEwbDto(ewb);
        
        var actual = ewbService.getEwb(id);
        
        assertThat(actual.getMedic())
                .extracting(
                        GetEwbDto.Medic::getFirstName,
                        GetEwbDto.Medic::getLastName,
                        GetEwbDto.Medic::getPatronymic,
                        GetEwbDto.Medic::getPosition,
                        GetEwbDto.Medic::isMedicSuccess,
                        GetEwbDto.Medic::getDecisionTime
                           )
                .containsExactly(
                        "АЛЕКСЕЕВИЧ",
                        "АЛЕКСЕЙ",
                        "",
                        ewb.getMedicContractor().getPosition(),
                        ewb.getStatus().isMedicSuccess(),
                        ewb.getMedicDecisionTime().plusHours(3)
                                );
    }
    
    @Test
    void shouldReturnEwbByRequestId() {
        var requestId = UUID.randomUUID();
        doReturn(Optional.empty()).when(ewbRepository).findByRequestId(any(UUID.class));
        var exception = assertThrows(EwbNotFoundException.class, () -> ewbService.getEwbByRequestId(requestId));
        assertEquals(EwbNotFoundException.EWB_REQUEST_MSG_FORMAT.formatted(requestId), exception.getMessage());
        doReturn(Optional.of(Instancio.create(Ewb.class))).when(ewbRepository).findByRequestId(any(UUID.class));
        var actual = ewbService.getEwbByRequestId(requestId);
        assertNotNull(actual);
    }
    
    @Test
    @SneakyThrows
    void sendAndSaveFirstTitle() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var request = Instancio.of(FirstTitleDto.class)
                               .set(Select.field(FirstTitleDto::content), "content")
                               .set(Select.field(FirstTitleDto::signature), "signature")
                               .create();
        var userId = UUID.randomUUID();
        
        var driver = Instancio.create(Driver.class);
        var transport = Instancio.create(Transport.class);
        var employee = Instancio.create(Employee.class);
        
        doReturn(driver).when(driverService).getActiveDriverById(request.firstTitleForm().driverId());
        doReturn(transport).when(transportService).getTransportInUseById(request.firstTitleForm().transportId());
        doReturn(employee).when(employeeService).getByUserId(userId);
        doNothing().when(signatureVerifier).verify(any(), any(), any());
        doReturn(Instancio.create(KorusEwbTitleResponse.class)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(any(), any(), any());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doReturn(Instancio.create(Dispatcher.class)).when(dispatcherService).getByEmployeeIdAndActive(userId);
        doReturn(Optional.of(Instancio.create(Department.class))).when(departmentService).get(request.firstTitleForm().tariffDepartmentId());
        doReturn(Instancio.create(Ewb.class)).when(ewbRepository).save(any(Ewb.class));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(Optional.of(Instancio.create(Ewb.class))).when(ewbRepository).findById(any(UUID.class));
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(any(UUID.class),
                                                                                                            any(EwbTitleType.class));
        doReturn(Instancio.create(EwbTitle.class)).when(ewbTitleRepository).save(any(EwbTitle.class));
        ewbService.sendAndSaveFirstTitle(request, userId);
        verify(korusClient, times(1)).sendTitle(any());
    }
    
    @Test
    @SneakyThrows
    void sendAndSaveSecondTitle() {
        var employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.create(Driver.class);
        var bytes = Base64.getEncoder().encodeToString("file".getBytes());
        var request1 = Instancio.of(SendAndSaveTitleRequest.class)
                                .set(field(SendAndSaveTitleRequest::file), bytes)
                                .create();
        var request2 = Instancio.of(SendAndSaveTitleRequest.class)
                                .set(field(SendAndSaveTitleRequest::file), bytes)
                                .create();
        var uuid = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ewb = Instancio.create(Ewb.class)
                           .setDriver(driver
                                              .setEmployee(employee));
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(employee).when(employeeService).getByUserId(any(UUID.class));
        doNothing().when(signatureVerifier).verify(any(), any(), any());
        doReturn(Instancio.create(KorusEwbTitleResponse.class)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(any(), any(), any());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(Optional.empty()).when(ewbRepository).findById(request2.ewbId());
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(request1.ewbId());
        var request = Instancio.create(Request.class);
        doReturn(request).when(requestService).createEmpty(any(CreateRequestDto.class), employeeCaptor.capture(), anyBoolean());
        ewbService.sendAndSaveSecondTitle(request1, userId);
        assertThat(employeeCaptor.getValue().getId()).isEqualTo(ewb.getDriver().getEmployee().getId());
        assertThat(ewb.getStatus()).isEqualTo(EwbStatus.TELEMECH_IN_PROGRESS);
        assertThat(ewb.getMedicRequest().getStatus()).isEqualTo(TelemedicineStatus.DONE);
        assertThat(ewb.getRequest().getOrganizationId()).isEqualTo(request.getOrganizationId());
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> ewbService.sendAndSaveSecondTitle(request2, uuid))
                .withMessage("Data not found: Entity: Ewb, ID: %s", request2.ewbId());
    }
    
    @Test
    @DisplayName("Получение заявки ЭПЛ")
    void getEwbRequest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var userId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var timeZone = "UTC+03:00";
        
        var employee = Instancio.of(Employee.class)
                                .set(field(Employee::getId), userId)
                                .create();
        var department = Instancio.of(Department.class)
                                  .set(field(Department::getId), departmentId)
                                  .create();
        employee.setDepartment(department);
        
        var declinedEwb = Instancio.create(Ewb.class);
        var inProgressEwb = Instancio.create(Ewb.class);
        var expiredInPorgressEwb = Instancio.create(Ewb.class);
        declinedEwb = declinedEwb.setCreationTime(CURRENT_DATE.atStartOfDay())
                                 .setStatus(EwbStatus.MEDIC_DECLINED);
        expiredInPorgressEwb = expiredInPorgressEwb.setCreationTime(CURRENT_DATE.atStartOfDay())
                                                   .setStatus(EwbStatus.TELEMECH_IN_PROGRESS);
        inProgressEwb = inProgressEwb.setCreationTime(CURRENT_DATE.atStartOfDay().plusHours(10))
                                     .setStatus(EwbStatus.TELEMECH_IN_PROGRESS);
        
        doReturn(employee).when(employeeService).getByUserId(userId);
        doReturn(timeZone).when(departmentTimeZoneService).getTimeZoneByDepartmentId(departmentId);
        doReturn(List.of(declinedEwb, expiredInPorgressEwb, inProgressEwb))
                .when(ewbRepository).findByDriverIdAndStatusIn(any(UUID.class), any(LocalDate.class), anySet(), anySet());
        doReturn(Instancio.create(GetEwbRequestDto.class)).when(ewbMapper).ewbToGetEwbRequestDto(any(Ewb.class));
        doReturn(Instancio.create(ChecksTreeDto.ChecksTree.class)).when(checkHelper).createChecksTree(anySet());
        var actual = ewbService.getEwbRequest(userId);
        assertNotNull(actual);
        verify(ewbRepository, never()).save(any(Ewb.class));
        
        doReturn(List.of(Instancio.create(Ewb.class)))
                .when(ewbRepository).findByDriverIdAndStatusIn(any(UUID.class), any(LocalDate.class), anySet(), anySet());
        actual = ewbService.getEwbRequest(userId);
        assertNotNull(actual);
        verify(ewbRepository, never()).save(any(Ewb.class));
        
        doReturn(Instancio.create(GetEwbRequestDto.class).withTelemechanic(null)).when(ewbMapper).ewbToGetEwbRequestDto(any(Ewb.class));
        doReturn(List.of(Instancio.create(Ewb.class))).when(ewbRepository).findByDriverIdAndStatusIn(any(UUID.class), any(LocalDate.class), anySet(),
                                                                                                     anySet());
        actual = ewbService.getEwbRequest(userId);
        assertNotNull(actual);
        assertNull(actual.telemechanic());
        
        doReturn(Collections.emptyList()).when(ewbRepository).findByDriverIdAndStatusIn(any(UUID.class), any(LocalDate.class), anySet(), anySet());
        actual = ewbService.getEwbRequest(userId);
        assertNull(actual);
    }
    
    @Test
    @DisplayName("Получение заявки ЭПЛ с часовым поясом UTC+11:00")
    void getEwbRequest_WithTimeZoneUTCPlus11() {
        var localDateTime = LocalDateTime.of(2026, 3, 25, 13, 0, 0);
        var utcFixedClock = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        doReturn(utcFixedClock.getZone()).when(clock).getZone();
        doReturn(utcFixedClock.instant()).when(clock).instant();
        
        var userId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var timeZone = "UTC+11:00";
        
        var employee = Instancio.of(Employee.class)
                                .set(field(Employee::getId), userId)
                                .create();
        var department = Instancio.of(Department.class)
                                  .set(field(Department::getId), departmentId)
                                  .create();
        employee.setDepartment(department);
        
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getCreationTime), LocalDate.of(2026, 3, 26).atStartOfDay())
                           .create();
        
        var localDateArgumentCaptor = ArgumentCaptor.forClass(LocalDate.class);
        
        doReturn(employee).when(employeeService).getByUserId(userId);
        doReturn(timeZone).when(departmentTimeZoneService).getTimeZoneByDepartmentId(departmentId);
        doReturn(List.of(ewb)).when(ewbRepository).findByDriverIdAndStatusIn(any(UUID.class), any(LocalDate.class), anySet(), anySet());
        doReturn(Instancio.create(GetEwbRequestDto.class)).when(ewbMapper).ewbToGetEwbRequestDto(any(Ewb.class));
        doReturn(Instancio.create(ChecksTreeDto.ChecksTree.class)).when(checkHelper).createChecksTree(anySet());
        
        var actual = ewbService.getEwbRequest(userId);
        verify(employeeService).getByUserId(eq(userId));
        verify(departmentTimeZoneService).getTimeZoneByDepartmentId(eq(departmentId));
        verify(ewbRepository).findByDriverIdAndStatusIn(eq(userId), localDateArgumentCaptor.capture(), anySet(), anySet());
        
        
        assertThat(actual).isNotNull();
        assertThat(localDateArgumentCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 26));
    }
    
    @Test
    @DisplayName("Занесение показаний одометра при выпуске на линию")
    void addOdometerValueOut() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        var transportId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(Select.field(Transport::getMileage), 1000)
                                 .create();
        var ewbId = UUID.randomUUID();
        var ewb = new Ewb()
                .setRequest(new Request())
                .setId(ewbId)
                .setStatus(EwbStatus.TELEMECH_IN_PROGRESS)
                .setTransport(transport);
        ewb.getRequest().setId(UUID.randomUUID());
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doNothing().when(odometerSender).send(odometerHistoryValueMessageArgumentCaptor.capture());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(any(UUID.class), any(CheckType.class), any(UUID.class));
        doReturn(new Check(UUID.randomUUID(), CheckType.ODOMETER, CheckStatus.DONE, 1, Instancio.create(Request.class), null))
                .when(checkService).changeCheckStatus(any(UUID.class), any(CheckType.class), any(CheckStatus.class));
        doReturn(new CheckResponse(CheckStatus.DONE, CheckType.OIL_LEVEL))
                .when(checkService).getNextCheck(any(UUID.class), any(CheckStatus.class), anyBoolean());
        
        ewbService.addOdometerValue(new OdometerValue(ewbId, 1010), UUID.randomUUID());
        
        assertThat(ewb.getOdometerOut())
                .isEqualTo(1010);
        
        assertThat(ewb.getOdometerIn())
                .isNull();
        
        assertThat(odometerHistoryValueMessageArgumentCaptor.getValue())
                .extracting(OdometerHistoryValueMessage::transportId, OdometerHistoryValueMessage::value)
                .containsExactly(transportId, 1010);
    }
    
    @ParameterizedTest
    @EnumSource(value = EwbStatus.class, mode = EnumSource.Mode.EXCLUDE, names = { "TELEMECH_IN_PROGRESS" })
    void addOdometerNotAvailableStatuses(EwbStatus status) {
        var ewb = new Ewb()
                .setId(UUID.randomUUID())
                .setStatus(status)
                .setRequest(Instancio.create(Request.class));
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(any(UUID.class), any(CheckType.class), any(UUID.class));
        
        var request = new OdometerValue(ewb.getId(), 1000);
        var userId = UUID.randomUUID();
        assertThatThrownBy(() -> ewbService.addOdometerValue(request, userId))
                .isInstanceOf(OdmeterValueNotAcceptedException.class)
                .hasMessage("Показания одометра могут быть внесены только для ЭПЛ в статусе \"Прохождение телемеханика\"");
        
        verify(odometerSender, never()).send(any());
    }
    
    
    @Test
    void addOdometerNotAcceptableOdometerValues() {
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getMileage), 1001)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(Select.field(Transport::getOrganizations), Set.of(Instancio.create(Organization.class)))
                                 .create();
        var ewb = new Ewb()
                .setId(UUID.randomUUID())
                .setTransport(transport)
                .setStatus(EwbStatus.TELEMECH_IN_PROGRESS)
                .setRequest(Instancio.create(Request.class));
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(any(UUID.class), any(CheckType.class), any(UUID.class));
        var request = new OdometerValue(ewb.getId(), 1000);
        var userId = UUID.randomUUID();
        assertThatThrownBy(() -> ewbService.addOdometerValue(request, userId))
                .isInstanceOf(OdmeterValueNotAcceptedException.class)
                .hasMessage("Текущие показания одометра больше, чем вносимые");
        verify(odometerSender, never()).send(any());
    }
    
    @ParameterizedTest
    @EnumSource(value = EwbStatus.class, mode = EnumSource.Mode.INCLUDE, names = { "ON_THE_LINE" })
    void addOdometerValueIn(EwbStatus status) {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        var ewbClosedMessage = Instancio.create(EwbClosedMessage.class);
        doReturn(ewbClosedMessage).when(ewbMapper).ewbToEwbClosedMessage(any(Ewb.class));
        var transportId = UUID.randomUUID();
        var ewbId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getFuelTankVolume), 40)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(Select.field(Transport::getOrganizations), Set.of(Instancio.create(Organization.class)))
                                 .create();
        var ewb = new Ewb()
                .setId(ewbId)
                .setStatus(status)
                .setOdometerOut(1000)
                .setRequest(Instancio.create(Request.class))
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doNothing().when(odometerSender).send(odometerHistoryValueMessageArgumentCaptor.capture());
        
        ewbService.closeEwb(new EwbCloseRequest(ewbId, 1011, 40), UUID.randomUUID());
        
        verify(ewbClosedSender, times(1)).send(any(EwbClosedMessage.class));
        assertThat(ewb.getOdometerIn())
                .isEqualTo(1011);
        assertThat(odometerHistoryValueMessageArgumentCaptor.getValue())
                .extracting(OdometerHistoryValueMessage::transportId, OdometerHistoryValueMessage::value)
                .containsExactly(transportId, 1011);
    }
    
    @ParameterizedTest
    @EnumSource(value = EwbStatus.class, mode = EnumSource.Mode.EXCLUDE, names = { "ON_THE_LINE" })
    void addOdometerValueInWrongStatuses(EwbStatus status) {
        var transportId = UUID.randomUUID();
        var ewbId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getFuelTankVolume), 40)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(Select.field(Transport::getOrganizations), Set.of(Instancio.create(Organization.class)))
                                 .create();
        var ewb = new Ewb()
                .setId(ewbId)
                .setStatus(status)
                .setOdometerOut(1000)
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        
        var request = new EwbCloseRequest(ewbId, 1011, 60);
        var userId = UUID.randomUUID();
        assertThatThrownBy(() -> ewbService.closeEwb(request, userId))
                .isInstanceOf(OdmeterValueNotAcceptedException.class)
                .hasMessage("Показания одометра могут быть внесены только для ЭПЛ в статусе \"На линии\"");
        
        verify(odometerSender, never()).send(any());
    }
    
    @ParameterizedTest
    @MethodSource
    void closeEwbFailedCases(
            Ewb ewb, int odometerValue, int fuelLitreage,
            Class<? extends Exception> expectedException, String expectedExceptionMessage
                            ) {
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        var request = new EwbCloseRequest(ewb.getId(), odometerValue, fuelLitreage);
        assertThatThrownBy(() -> ewbService.closeEwb(request, UUID.randomUUID()))
                .isInstanceOf(expectedException)
                .hasMessage(expectedExceptionMessage);
        verify(odometerSender, never()).send(any());
    }
    
    static Stream<Arguments> closeEwbFailedCases() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getFuelTankVolume), 40)
                                 .create();
        return Stream.of(
                Arguments.of(new Ewb()
                                     .setId(UUID.randomUUID())
                                     .setStatus(EwbStatus.ON_THE_LINE),
                             10000,
                             60,
                             OdmeterValueNotAcceptedException.class,
                             "Необходимо внести показания одометра при выпуске на линию"),
                Arguments.of(new Ewb()
                                     .setId(UUID.randomUUID())
                                     .setStatus(EwbStatus.ON_THE_LINE)
                                     .setOdometerOut(10001),
                             10000,
                             60,
                             OdmeterValueNotAcceptedException.class,
                             "Показания одометра при выпуске на линию меньше предоставленных показаний"),
                Arguments.of(new Ewb()
                                     .setId(UUID.randomUUID())
                                     .setStatus(EwbStatus.ON_THE_LINE)
                                     .setOdometerOut(7499),
                             10000,
                             60,
                             OdmeterValueNotAcceptedException.class,
                             "Показания одометра при возвращении в гараж должны отличаться от значений при выпуске на линию не более чем на 2500 км"),
                Arguments.of(new Ewb()
                                     .setId(UUID.randomUUID())
                                     .setStatus(EwbStatus.ON_THE_LINE)
                                     .setTransport(transport)
                                     .setOdometerOut(7499),
                             7500,
                             60,
                             LitreageValidationException.class,
                             "Значение не может быть больше объема топливного бака автомобиля: 40 л.")
                        );
    }
    
    @Test
    void shouldDeclineEwbWithRequest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var requestId = UUID.randomUUID();
        var requestDto = Instancio.create(DeclinedTelemechRequest.class);
        var userId = UUID.randomUUID();
        var ewb = Instancio.create(Ewb.class);
        ewb.getRequest().setStatus(RequestStatus.ON_THE_LINE);
        
        doReturn(Optional.empty()).when(ewbRepository).findEwbByRequestId(requestId);
        var ewbNotFoundException = assertThrows(EwbNotFoundException.class, () -> ewbService.declineEwb(requestId, requestDto, userId));
        assertEquals(EwbNotFoundException.EWB_REQUEST_MSG_FORMAT.formatted(requestId), ewbNotFoundException.getMessage());
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findEwbByRequestId(requestId);
        var illegalStateResponseException = assertThrows(IllegalStateResponseException.class,
                                                         () -> ewbService.declineEwb(requestId, requestDto, userId));
        assertEquals("Невозможно изменить заявку в статусе %s".formatted(ewb.getRequest().getStatus()), illegalStateResponseException.getMessage());
        
        ewb.getRequest().setStatus(RequestStatus.WARNING);
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        assertDoesNotThrow(() -> ewbService.declineEwb(requestId, requestDto, userId));
    }
    
    @Test
    @SneakyThrows
    void validationFirstTitle() {
        var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        var schemaFile = new StreamSource(new File("src/test/resources/ewb/titles/first/title.xsd"));
        var schema = factory.newSchema(schemaFile);
        
        var validator = schema.newValidator();
        var context = JAXBContext.newInstance(ru.sber.transport.telemechanic.dto.ewb.xjb.File.class);
        var unmarshaller = context.createUnmarshaller();
        var firstTitleFile = (ru.sber.transport.telemechanic.dto.ewb.xjb.File)
                unmarshaller.unmarshal(new File("src/test/resources/ewb/titles/first/title_for_validation.xml"));
        
        var source = new JAXBSource(context, firstTitleFile);
        
        assertDoesNotThrow(() -> validator.validate(source));
    }
    
    @Test
    @SneakyThrows
    void validationSecondTitle() {
        var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        var schemaFile = new StreamSource(new File("src/test/resources/ewb/titles/second/title.xsd"));
        var schema = factory.newSchema(schemaFile);
        
        var validator = schema.newValidator();
        var context = JAXBContext.newInstance(SecondTitleFile.class);
        var unmarshaller = context.createUnmarshaller();
        var secondTitleFile = (SecondTitleFile) unmarshaller.unmarshal(new File("src/test/resources/ewb/titles/second/title.xml"));
        
        var source = new JAXBSource(context, secondTitleFile);
        
        assertDoesNotThrow(() -> validator.validate(source));
    }
    
    @Test
    @SneakyThrows
    void validationThirdTitle() {
        var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        var schemaFile = new StreamSource(new File("src/test/resources/ewb/titles/third/title.xsd"));
        var schema = factory.newSchema(schemaFile);
        
        var validator = schema.newValidator();
        var context = JAXBContext.newInstance(ThirdTitleFile.class);
        var unmarshaller = context.createUnmarshaller();
        var thirdTitleFile = (ThirdTitleFile) unmarshaller.unmarshal(new File("src/test/resources/ewb/titles/third/title.xml"));
        
        var source = new JAXBSource(context, thirdTitleFile);
        
        assertDoesNotThrow(() -> validator.validate(source));
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Формирование третьего титула ЭПЛ")
    void generateThirdTitleTest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var titleType = EwbTitleType.THIRD;
        var request = new TelemechOutTitlesRequest(UUID.randomUUID(), LocalDateTime.now(fixedClock));
        var userId = UUID.randomUUID();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getTimeZone), "UTC+03:00")
                           .set(field(Ewb::getStartDate), request.decisionTime().minusYears(1).toLocalDate())
                           .create();
        
        doReturn(Optional.empty()).when(ewbRepository).findByRequestId(any(UUID.class));
        var ewbNotFoundException = assertThrows(EwbNotFoundException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("ЭПЛ с идентификатором заявки requestId=%s не найден!".formatted(request.requestId()), ewbNotFoundException.getMessage());
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findByRequestId(any(UUID.class));
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        var badRequestException =
                assertThrows(BadRequestException.class, () -> ewbService.generateTelemechOutTitles(EwbTitleType.FIRST, request, userId));
        assertEquals("%s не может быть сформирован!".formatted(EwbTitleType.FIRST.getName()), badRequestException.getMessage());
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class)))
                .when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), titleType);
        EwbTitleAlreadyExistsException ewbTitleAlreadyExistsException = assertThrows(EwbTitleAlreadyExistsException.class, () ->
                ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("У ЭПЛ с id=%s уже есть Третий титул!".formatted(ewb.getId()), ewbTitleAlreadyExistsException.getMessage());
        
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), titleType);
        badRequestException = assertThrows(BadRequestException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("Дата принятия решения должна совпадать с датой начала рейса ЭПЛ!", badRequestException.getMessage());
        
        ewb.setStartDate(request.decisionTime().toLocalDate());
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.FIRST);
        var ewbGenerateException = assertThrows(EwbGenerateException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("При формировании xml произошла ошибка!", ewbGenerateException.getMessage());
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.FIRST);
        doReturn(new FileData("some type", "data".getBytes())).when(fileService).get(any(String.class));
        doReturn(Instancio.create(ThirdTitleFile.class)).when(ewbMapper).ewbInfoToFile(any(EwbInfo.class), any(LocalDateTime.class));
        var actual = ewbService.generateTelemechOutTitles(titleType, request, userId);
        assertNotNull(actual);
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Отправка и сохранение третьего титула ЭПЛ")
    void sendAndSaveThirdTitleTest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var ewbId = UUID.randomUUID();
        var request = new SendAndSaveTitleRequest("fileName", Base64.getEncoder().encodeToString("file".getBytes()),
                                                  "signature", ewbId, LocalDateTime.now(fixedClock));
        var userId = UUID.randomUUID();
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewbId, EwbTitleType.THIRD);
        var ewbTitleAlreadyExistsException = assertThrows(EwbTitleAlreadyExistsException.class, () ->
                ewbService.sendAndSaveTelemechOutTitle(request, EwbTitleType.THIRD, userId));
        assertEquals("У ЭПЛ с id=%s уже есть %s!".formatted(ewbId, EwbTitleType.THIRD.getName()), ewbTitleAlreadyExistsException.getMessage());
        
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doNothing().when(signatureVerifier).verify(any(), anyString(), anyString());
        doReturn(Instancio.create(KorusEwbTitleResponse.class)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(any(InputStream.class), anyString(), anyString());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(Optional.of(Instancio.create(Ewb.class))).when(ewbRepository).findById(any(UUID.class));
        doReturn(Instancio.create(EwbTitle.class)).when(ewbTitleRepository).save(any(EwbTitle.class));
        when(ewbTitleRepository.findByEwbIdAndType(any(UUID.class), any(EwbTitleType.class))).thenReturn(Optional.empty())
                                                                                             .thenReturn(
                                                                                                     Optional.of(Instancio.create(EwbTitle.class)));
        var actual = ewbService.sendAndSaveTelemechOutTitle(request, EwbTitleType.THIRD, userId);
        assertNotNull(actual);
        verify(requestService).updateRequestThirdTitleSent(any(Request.class), any(UUID.class));
        assertEquals(EwbTitleType.FOURTH, actual.nextTitleType());
    }
    
    @Test
    @SneakyThrows
    void validationFourthTitle() {
        var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        var schemaFile = new StreamSource(new File("src/test/resources/ewb/titles/fourth/title.xsd"));
        var schema = factory.newSchema(schemaFile);
        
        var validator = schema.newValidator();
        var context = JAXBContext.newInstance(FourthTitleFile.class);
        var unmarshaller = context.createUnmarshaller();
        var fourthTitleFile = (FourthTitleFile) unmarshaller.unmarshal(new File("src/test/resources/ewb/titles/fourth/title.xml"));
        
        var source = new JAXBSource(context, fourthTitleFile);
        
        assertDoesNotThrow(() -> validator.validate(source));
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Формирование четвертого титула ЭПЛ")
    void generateFourthTitleTest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var titleType = EwbTitleType.FOURTH;
        var request = new TelemechOutTitlesRequest(UUID.randomUUID(), LocalDateTime.now(fixedClock));
        var userId = UUID.randomUUID();
        var employee = Instancio.create(Employee.class);
        var ewb = Instancio.of(Ewb.class)
                .set(field(Ewb::getTimeZone), "UTC+03:00")
                .set(field(Ewb::getStartDate), request.decisionTime().minusYears(1).toLocalDate())
                .create();
        var telemechOut = Instancio.create(Employee.class);
        telemechOut.setId(userId);
        
        doReturn(Optional.empty()).when(ewbRepository).findByRequestId(any(UUID.class));
        var ewbNotFoundException = assertThrows(EwbNotFoundException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("ЭПЛ с идентификатором заявки requestId=%s не найден!".formatted(request.requestId()), ewbNotFoundException.getMessage());
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findByRequestId(any(UUID.class));
        doReturn(employee).when(employeeService).getByUserId(userId);
        var badRequestException =
                assertThrows(BadRequestException.class, () -> ewbService.generateTelemechOutTitles(EwbTitleType.FIRST, request, userId));
        assertEquals("%s не может быть сформирован!".formatted(EwbTitleType.FIRST.getName()), badRequestException.getMessage());
        
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), titleType);
        badRequestException = assertThrows(BadRequestException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("Дата принятия решения должна совпадать с датой начала рейса ЭПЛ!", badRequestException.getMessage());
        
        ewb.setStartDate(request.decisionTime().toLocalDate());
        badRequestException = assertThrows(BadRequestException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("ЭПЛ не может быть сформирован! Сотрудник ответственный за выпуск на линию не совподает!", badRequestException.getMessage());
        
        
        ewb.setTelemechOut(telemechOut);
        employee.setId(userId);
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.THIRD);
        var ewbGenerateException = assertThrows(EwbGenerateException.class, () -> ewbService.generateTelemechOutTitles(titleType, request, userId));
        assertEquals("При формировании xml произошла ошибка!", ewbGenerateException.getMessage());
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.THIRD);
        doReturn(new FileData("some type", "data".getBytes())).when(fileService).get(any(String.class));
        doReturn(Instancio.create(FourthTitleFile.class)).when(ewbMapper).ewbInfoToFourthTitleFile(any(EwbInfo.class));
        var actual = ewbService.generateTelemechOutTitles(titleType, request, userId);
        assertNotNull(actual);
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Отправка и сохранение четвертого титула ЭПЛ")
    void sendAndSaveFourthTitleTest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        var ewbCaptor = ArgumentCaptor.forClass(Ewb.class);
        
        var ewbId = UUID.randomUUID();
        var request = new SendAndSaveTitleRequest("fileName", Base64.getEncoder().encodeToString("file".getBytes()), "signature",
                                                  ewbId, LocalDateTime.now(fixedClock));
        var userId = UUID.randomUUID();
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewbId, EwbTitleType.FOURTH);
        var ewbTitleAlreadyExistsException = assertThrows(EwbTitleAlreadyExistsException.class, () ->
                ewbService.sendAndSaveTelemechOutTitle(request, EwbTitleType.FOURTH, userId));
        assertEquals("У ЭПЛ с id=%s уже есть %s!".formatted(ewbId, EwbTitleType.FOURTH.getName()), ewbTitleAlreadyExistsException.getMessage());
        
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doNothing().when(signatureVerifier).verify(any(), anyString(), anyString());
        doReturn(Instancio.create(KorusEwbTitleResponse.class)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(any(InputStream.class), anyString(), anyString());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(Optional.of(Instancio.create(Ewb.class))).when(ewbRepository).findById(any(UUID.class));
        doReturn(Instancio.create(EwbTitle.class)).when(ewbTitleRepository).save(any(EwbTitle.class));
        when(ewbTitleRepository.findByEwbIdAndType(any(UUID.class), any(EwbTitleType.class))).thenReturn(Optional.empty())
                                                                                             .thenReturn(
                                                                                                     Optional.of(Instancio.create(EwbTitle.class)));
        doReturn(Instancio.create(Ewb.class)).when(ewbRepository).save(ewbCaptor.capture());
        doReturn(Instancio.create(EwbClosedMessage.class)).when(ewbMapper).ewbToEwbClosedMessage(any(Ewb.class));
        doNothing().when(ewbClosedSender).send(any(EwbClosedMessage.class));

        var actual = ewbService.sendAndSaveTelemechOutTitle(request, EwbTitleType.FOURTH, userId);
        verify(ewbClosedSender, times(1)).send(any(EwbClosedMessage.class));
        assertNotNull(actual);
        assertEquals(EwbTitleType.FIFTH, actual.nextTitleType());
        var savedEwb = ewbCaptor.getValue();
        assertEquals(EwbStatus.ON_THE_LINE, savedEwb.getStatus());
        assertEquals(RequestStatus.ON_THE_LINE, savedEwb.getRequest().getStatus());
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Валидация пятого титула")
    void validationFifthTitle() {
        var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        var schemaFile = new StreamSource(new File("src/test/resources/ewb/titles/fifth/title.xsd"));
        var schema = factory.newSchema(schemaFile);
        
        var validator = schema.newValidator();
        var context = JAXBContext.newInstance(FifthTitleFile.class);
        var unmarshaller = context.createUnmarshaller();
        var fifthTitleFile = (FifthTitleFile) unmarshaller.unmarshal(new File("src/test/resources/ewb/titles/fifth/title.xml"));
        
        var source = new JAXBSource(context, fifthTitleFile);
        
        assertDoesNotThrow(() -> validator.validate(source));
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Формирование пятого титула ЭПЛ")
    void generateFifthTitleTest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var request = new FifthTitleRequest(UUID.randomUUID(), LocalDateTime.now(fixedClock));
        var userId = UUID.randomUUID();
        var ewb = Instancio.create(Ewb.class);
        ewb.setStartDate(LocalDate.of(2000, 1, 1));
        ewb.setStatus(EwbStatus.ON_THE_LINE);
        
        doReturn(Optional.empty()).when(ewbRepository).findById(request.id());
        var ewbNotFoundException = assertThrows(EwbNotFoundException.class, () -> ewbService.generateFifthTitle(request, userId));
        assertEquals(EwbNotFoundException.MSG_FORMAT.formatted((request.id())), ewbNotFoundException.getMessage());
        
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(request.id());
        doReturn(ewb.getAuthor()).when(employeeService).getByUserId(userId);
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.FIFTH);
        var ewbTitleAlreadyExistsException = assertThrows(EwbTitleAlreadyExistsException.class, () -> ewbService.generateFifthTitle(request, userId));
        assertEquals("У ЭПЛ с id=%s уже есть Пятый титул!".formatted(ewb.getId()), ewbTitleAlreadyExistsException.getMessage());
        
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.FIFTH);
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> ewbService.generateFifthTitle(request, userId));
        assertEquals("Невозможно сформировать пятый титул для ЭПЛ в статусе %s!".formatted(ewb.getStatus()), badRequestException.getMessage());
        
        ewb.setStatus(EwbStatus.IN_GARAGE);
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.FOURTH);
        var ewbGenerateException = assertThrows(EwbGenerateException.class, () -> ewbService.generateFifthTitle(request, userId));
        assertEquals("При формировании xml произошла ошибка!", ewbGenerateException.getMessage());
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), EwbTitleType.FOURTH);
        doReturn(new FileData("some type", "data".getBytes())).when(fileService).get(any(String.class));
        doReturn(Instancio.create(FifthTitleFile.class)).when(fifthTitleMapper).ewbInfoToFifthTitleFile(any(EwbInfo.class));
        doReturn("some_name").when(ewbFilenameService)
                             .generateFilename(EwbTitleType.FIFTH, ewb.getOrganization().getId(), ewb.getTariffDepartmentId());
        var actual = assertDoesNotThrow(() -> ewbService.generateFifthTitle(request, userId));
        
        assertNotNull(actual);
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Отправка и сохранение пятого титула ЭПЛ")
    void sendAndSaveFifthTitleTest() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        var ewbClosedMessage = Instancio.create(EwbClosedMessage.class);
        doReturn(ewbClosedMessage).when(ewbMapper).ewbToEwbClosedMessage(any(Ewb.class));
        var ewbCaptor = ArgumentCaptor.forClass(Ewb.class);
        
        var ewbId = UUID.randomUUID();
        var request = new SendAndSaveTitleRequest("fileName", Base64.getEncoder().encodeToString("file".getBytes()), "signature",
                                                  ewbId, LocalDateTime.now(fixedClock));
        var userId = UUID.randomUUID();
        
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewbId, EwbTitleType.FIFTH);
        var ewbTitleAlreadyExistsException =
                assertThrows(EwbTitleAlreadyExistsException.class, () -> ewbService.sendAndSaveFifthTitle(request, userId));
        assertEquals("У ЭПЛ с id=%s уже есть %s!".formatted(ewbId, EwbTitleType.FIFTH.getName()), ewbTitleAlreadyExistsException.getMessage());
        
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doNothing().when(signatureVerifier).verify(any(), anyString(), anyString());
        doReturn(Instancio.create(KorusEwbTitleResponse.class)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(any(InputStream.class), anyString(), anyString());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(Optional.of(Instancio.create(Ewb.class))).when(ewbRepository).findById(any(UUID.class));
        doReturn(Instancio.create(EwbTitle.class)).when(ewbTitleRepository).save(any(EwbTitle.class));
        when(ewbTitleRepository.findByEwbIdAndType(any(UUID.class), any(EwbTitleType.class))).thenReturn(Optional.empty())
                                                                                             .thenReturn(
                                                                                                     Optional.of(Instancio.create(EwbTitle.class)));
        doReturn(Instancio.create(Ewb.class)).when(ewbRepository).saveAndFlush(ewbCaptor.capture());
        ewbService.sendAndSaveFifthTitle(request, userId);
        var savedEwb = ewbCaptor.getValue();
        verify(ewbClosedSender, times(1)).send(any(EwbClosedMessage.class));
        assertEquals(EwbStatus.EWB_CLOSED, savedEwb.getStatus());
        assertEquals(RequestStatus.FINISHED, savedEwb.getRequest().getStatus());
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Получение QR-кода из Коруса")
    void getQrCodeTest() {
        var ewbId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ewbTitle = Instancio.create(EwbTitle.class);
        
        doNothing().when(ewbValidationService).validateEwbQrCode(ewbId, userId);
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewbId, EwbTitleType.FOURTH);
        var ewbTitleNotFoundException = assertThrows(EwbTitleNotFoundException.class, () -> ewbService.getQrCode(ewbId, userId));
        assertEquals("Для ЭПЛ=%s не найден %s".formatted(ewbId, EwbTitleType.FOURTH.getName()), ewbTitleNotFoundException.getMessage());
        
        doReturn(Optional.of(ewbTitle)).when(ewbTitleRepository).findByEwbIdAndType(ewbId, EwbTitleType.FOURTH);
        doReturn(null).when(korusClient).getChainDocs(ewbTitle.getChainId());
        var qrCodeNotExistsException = assertThrows(QrCodeNotExistsException.class, () -> ewbService.getQrCode(ewbId, userId));
        assertEquals("QR-код не готов", qrCodeNotExistsException.getMessage());
        
        doReturn(new ChainDocsDto(Collections.emptyList())).when(korusClient).getChainDocs(ewbTitle.getChainId());
        qrCodeNotExistsException = assertThrows(QrCodeNotExistsException.class, () -> ewbService.getQrCode(ewbId, userId));
        assertEquals("QR-код не готов", qrCodeNotExistsException.getMessage());
        
        doReturn(new ChainDocsDto(
                List.of(new ChainDocsDto.DocsDto(UUID.randomUUID(), ewbTitle.getChainId(), "NOT_QR"),
                        new ChainDocsDto.DocsDto(UUID.randomUUID(), UUID.randomUUID(), "ON_PTLAPERAK_QRCODE"),
                        new ChainDocsDto.DocsDto(UUID.randomUUID(), UUID.randomUUID(), "NOT_QR"))))
                .when(korusClient).getChainDocs(ewbTitle.getChainId());
        qrCodeNotExistsException = assertThrows(QrCodeNotExistsException.class, () -> ewbService.getQrCode(ewbId, userId));
        assertEquals("QR-код не готов", qrCodeNotExistsException.getMessage());
        
        doReturn(new ChainDocsDto(
                List.of(new ChainDocsDto.DocsDto(UUID.randomUUID(), ewbTitle.getChainId(), "ON_PTLAPERAK_QRCODE"))
        )).when(korusClient).getChainDocs(ewbTitle.getChainId());
        try (var archiveAsBytes = getClass()
                .getClassLoader()
                .getResourceAsStream("ewb/qr/Status_20241204_092357_57e284f4-b208-11ef-850c-39ad2505c047.xml")) {
            assertNotNull(archiveAsBytes);
            byte[] bytes = archiveAsBytes.readAllBytes();
            var baos = new ByteArrayOutputStream();
            try (var zos = new ZipOutputStream(baos)) {
                zos.putNextEntry(new ZipEntry("%s.xml".formatted("Status_20241204_092357_57e284f4-b208-11ef-850c-39ad2505c047")));
                zos.write(bytes);
                zos.closeEntry();
            }
            doReturn(baos.toByteArray()).when(korusClient).getDocArchive(any(UUID.class));
            var actual = ewbService.getQrCode(ewbId, userId);
            assertNotNull(actual);
            assertEquals("R0lGODlhwgHCARAAACH/C05FVFNDQVBFMi4wAwEAAAAh+QQAWgAAACwAAAAAwgHCAYD///8AAAAC/4SPqcvtD6OctNqLs968+w" +
                         "+G4kiW5omm6sq27gvH8kzX9o3n+s73/g8MCofEovGITCqXzKbzCY1Kp9Sq9YrNarfcrvcLDovH5LL5jE6r1+y2+w2Py+f0uv2Oz+v3/L7" +
                         "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",
                         actual.qr());
            baos.close();
        }
    }
    
    @Test
    @DisplayName("Получение реестра ЭПЛ")
    void getEwbRegistryAllOrganizations() {
        var request = Instancio.create(EwbRegistryAllOrganizationsRequest.class)
                               .withPageSetting(null)
                               .withSortSetting(null);
        var response = Instancio.create(EwbRegistryResponse.class);
        
        doReturn(new EwbSearchDto(List.of(response), 1))
                .when(ewbRegistryDynamicRepository).findEwbRegistry(any(PageRequest.class),
                                                                    anySet(),
                                                                    anyString(),
                                                                    anyString(),
                                                                    any(UUID.class),
                                                                    anySet(),
                                                                    any(DateRange.class));
        
        var actual = ewbService.searchRegistryForAllOrganizations(request);
        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(1, actual.getContent().size());
        var actualElement = actual.getContent().get(0);
        assertEquals(response.ewb().humanReadableId(), actualElement.ewb().humanReadableId());
    }
    
    @Test
    @DisplayName("Получение реестра ЭПЛ")
    void getEwbRegistrySelfOrganization() {
        var request = Instancio.create(EwbRegistrySelfOrganizationRequest.class)
                               .withPageSetting(null)
                               .withSortSetting(null);
        var response = Instancio.create(EwbRegistryResponse.class);
        var userId = UUID.randomUUID();
        
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doReturn(new EwbSearchDto(List.of(response), 1))
                .when(ewbRegistryDynamicRepository).findEwbRegistry(any(PageRequest.class),
                                                                    anySet(),
                                                                    anyString(),
                                                                    anyString(),
                                                                    any(UUID.class),
                                                                    isNull(),
                                                                    any(DateRange.class));
        
        var actual = ewbService.searchRegistryForSelfOrganization(request, userId);
        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(1, actual.getContent().size());
        var actualElement = actual.getContent().get(0);
        assertEquals(response.ewb().humanReadableId(), actualElement.ewb().humanReadableId());
    }
    
    @Test
    void haveActiveEwb() {
        var departmentIds = Instancio.createList(UUID.class);
        var wrongDepartmentIds = Instancio.createList(UUID.class);
        doReturn(true)
                .when(ewbRepository)
                .ewbExistsByTariffDepartmentsAndStatuses(departmentIds, HAVE_ACTIVE_EWB_CHECK_STATUSES, LocalDate.now(fixedClock));
        doReturn(false)
                .when(ewbRepository)
                .ewbExistsByTariffDepartmentsAndStatuses(wrongDepartmentIds, HAVE_ACTIVE_EWB_CHECK_STATUSES, LocalDate.now(fixedClock));
        assertTrue(ewbService.haveActiveEwb(departmentIds, LocalDate.now(fixedClock)));
        assertFalse(ewbService.haveActiveEwb(wrongDepartmentIds, LocalDate.now(fixedClock)));
    }
    
    @Test
    void getEwbDetailed() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        
        var userId = UUID.randomUUID();
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getId), UUID.randomUUID())
                              .create();
        var ewb = Instancio.create(Ewb.class);
        var requestDate = LocalDate.now(fixedClock);
        
        doReturn(driver).when(driverService).getByEmployeeId(userId);
        doReturn(Optional.of(ewb)).when(ewbRepository).findCurrentOnTheLineByDriverId(driver.getId(), requestDate);
        doReturn("UTC+03:00").when(departmentTimeZoneService).getTimeZoneByDepartmentId(any(UUID.class));
        when(centralOrganizationHelper.map(eq(ewb.getOrganization().getOrganizationGroupId()), any(Supplier.class), any(Supplier.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(1, Supplier.class).get());
        var getEwbDetailedDto = Instancio.create(GetEwbDetailedDto.class);
        doReturn(getEwbDetailedDto).when(ewbMapper).ewbToGetEwbDetailedDto(eq(ewb), eq(driver),
                                                                           eq(ewb.getOrganization().getOfficialName()),
                                                                           eq(ewb.getOrganization().getTin()),
                                                                           eq(ewb.getOrganization().getMsrn()),
                                                                           any());
        var actualEwbDetailed = ewbService.getEwbDetailed(userId);
        
        verify(driverService).getByEmployeeId(userId);
        verify(ewbRepository).findCurrentOnTheLineByDriverId(driver.getId(), requestDate);
        assertThat(actualEwbDetailed).usingRecursiveAssertion().isEqualTo(getEwbDetailedDto);
        
        when(centralOrganizationHelper.map(eq(ewb.getOrganization().getOrganizationGroupId()), any(Supplier.class), any(Supplier.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(2, Supplier.class).get());
        var centralName = "centralName";
        var centralTin = "centralTin";
        var centralMsrn = "centralMsrn";
        var centralPhone = "centralPhone";
        doReturn(centralName).when(centralOrganizationHelper).getCentralName();
        doReturn(centralTin).when(centralOrganizationHelper).getCentralTin();
        doReturn(centralMsrn).when(centralOrganizationHelper).getCentralMsrn();
        doReturn(centralPhone).when(centralOrganizationHelper).getCentralPhone();
        var getEwbDetailedDtoCentral = Instancio.of(GetEwbDetailedDto.class)
                                                .set(field(GetEwbDetailedDto::organizationName), centralName)
                                                .set(field(GetEwbDetailedDto::tin), centralTin)
                                                .set(field(GetEwbDetailedDto::msrn), centralMsrn)
                                                .set(field(GetEwbDetailedDto::phoneNumber), centralPhone)
                                                .create();
        doReturn(getEwbDetailedDtoCentral).when(ewbMapper).ewbToGetEwbDetailedDto(ewb,
                                                                                  driver,
                                                                                  centralName,
                                                                                  centralTin,
                                                                                  centralMsrn,
                                                                                  centralPhone);
        var actualEwbDetailedCentral = ewbService.getEwbDetailed(userId);
        assertThat(actualEwbDetailedCentral).usingRecursiveAssertion().isEqualTo(getEwbDetailedDtoCentral);
    }
    
    @Test
    @DisplayName("Отмена ЭПЛ")
    void cancelEwb() {
        var request = Instancio.create(EwbCancelRequestDto.class);
        var userId = UUID.randomUUID();
        var ewbId = UUID.randomUUID();
        doReturn(Optional.empty()).when(ewbRepository).findById(any(UUID.class));
        
        assertThatExceptionOfType(EwbNotFoundException.class)
                .isThrownBy(() -> ewbService.cancelEwb(ewbId, request, userId))
                .withMessage("ЭПЛ с идентификатором id=%s не найден!".formatted(ewbId));
        var organization = Instancio.of(Organization.class).set(field(Organization::getId), UUID.randomUUID()).create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getId), ewbId)
                           .set(field(Ewb::getOrganization), Instancio.create(Organization.class))
                           .set(field(Ewb::getStatus), IN_GARAGE)
                           .create();
        var employee = Instancio.of(Employee.class)
                                .set(field(Employee::getId), userId)
                                .set(field(Employee::getOrganization), organization)
                                .create();
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(any(UUID.class));
        doReturn(employee).when(employeeService).getByUserId(any(UUID.class));
        
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> ewbService.cancelEwb(ewbId, request, userId))
                .withMessage("Не возможна отмена ЭПЛ в статусе %s".formatted(IN_GARAGE));
        ewb.setStatus(MEDIC_IN_PROGRESS);
        doReturn(Instancio.create(EwbHistory.class)).when(ewbMapper).ewbToEwbHistory(ewb, EWB_CANCELLED, request.comment(), employee);
        doReturn(Instancio.create(MedicRequestHistory.class))
                .when(telemedicineMapper)
                .medicRequestToMedicRequestHistory(ewb.getMedicRequest(), TelemedicineStatus.EXPIRED, request.comment(), employee);
        doReturn(Instancio.create(RequestHistory.class))
                .when(requestMapper)
                .requestToRequestHistory(ewb.getRequest(), RequestStatus.EXPIRED, request.comment(), employee);
        doReturn(Instancio.create(Ewb.class)).when(ewbRepository).save(ewbArgumentCaptor.capture());
        ewbService.cancelEwb(ewbId, request, userId);
        
        assertThat(ewbArgumentCaptor.getValue().getStatus()).isEqualTo(EWB_CANCELLED);
    }
    
    @Test
    @DisplayName("Авто обновление статуса, ЭПЛ не найдены")
    void statusAutoUpdateWhenEwbNotFound() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(List.of()).when(ewbRepository).findAllByStatusInAndFinishDate(anySet(), any(LocalDate.class));
        
        ewbService.statusAutoUpdate();
        verify(ewbRepository, never()).saveAll(anyList());
        verifyNoInteractions(ewbHistoryRepository);
    }
    
    @Test
    @DisplayName("Авто обновление статуса ЭПЛ: успех")
    void statusAutoUpdateSuccess() {
        var captor = ArgumentCaptor.forClass(List.class);
        var comment = "Статус заявки изменен пользователем: Система (Планировщик)";
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), EWB_CREATED)
                           .set(field(Ewb::getFinishDate), LocalDate.now(fixedClock).minusDays(1))
                           .create();
        var ewbHistory = Instancio.of(EwbHistory.class)
                                  .set(field(EwbHistory::getEwbId), ewb.getId())
                                  .set(field(EwbHistory::getStatus), EXPIRED)
                                  .set(field(EwbHistory::getOldStatus), EWB_CREATED)
                                  .set(field(EwbHistory::getComment), comment)
                                  .set(field(EwbHistory::getInitiator), null)
                                  .create();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(List.of(ewb)).when(ewbRepository).findAllByStatusInAndFinishDate(anySet(), any(LocalDate.class));
        doReturn(ewbHistory).when(ewbMapper).ewbToEwbHistory(ewb, EXPIRED, comment, null);
        
        ewbService.statusAutoUpdate();
        verify(ewbRepository, times(1)).setEwbListExpired(anyList());
        verify(ewbHistoryRepository, times(1)).saveAll(captor.capture());
        
        var historyArg = (EwbHistory) captor.getValue().get(0);
        assertThat(historyArg).isNotNull();
        assertThat(historyArg.getEwbId()).isEqualTo(ewb.getId());
        assertThat(historyArg.getOldStatus()).isEqualTo(EWB_CREATED);
        assertThat(historyArg.getStatus()).isEqualTo(EXPIRED);
        assertThat(historyArg.getComment()).isEqualTo(comment);
        assertThat(historyArg.getInitiator()).isNull();
    }
    
    @Test
    @DisplayName("Ввод остатка топлива")
    void litreageOut() {
        var userId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .create();
        var ewbId = UUID.randomUUID();
        var ewb = new Ewb()
                .setRequest(Instancio.of(Request.class)
                                     .set(Select.field(Request::getStatus), RequestStatus.IN_PROGRESS)
                                     .create())
                .setDriver(Instancio.of(Driver.class)
                                    .set(Select.field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                                     .set(Select.field(Employee::getId), userId)
                                                                                     .create())
                                    .create())
                .setId(ewbId)
                .setStatus(EwbStatus.TELEMECH_IN_PROGRESS)
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        doReturn(new Check(UUID.randomUUID(), CheckType.LITREAGE, CheckStatus.DONE, 1, Instancio.create(Request.class), null))
                .when(checkService).changeCheckStatus(any(UUID.class), any(CheckType.class), any(CheckStatus.class));
        doReturn(new CheckResponse(CheckStatus.DONE, CheckType.OIL_LEVEL))
                .when(checkService).getNextCheck(any(UUID.class), any(CheckStatus.class), anyBoolean());
        ewbService.litreageOut(new EwbLitreageOutRequest(ewbId, ewb.getTransport().getFuelTankVolume()), userId);
        verify(ewbRepository).findById(ewbId);
        verify(checkPhotoService).validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        verify(checkService).changeCheckStatus(ewb.getRequest().getId(), CheckType.LITREAGE, CheckStatus.DONE);
        verify(checkService).getNextCheck(ewb.getRequest().getId(), CheckStatus.DONE, true);
        assertThat(ewb.getFuelLitreageOut()).isEqualTo(ewb.getTransport().getFuelTankVolume());
        assertThat(ewb.getFuelLitreageIn()).isNull();
    }
    
    @Test
    @DisplayName("Ввод остатка топлива не водителем")
    void litreageOutNotDriver() {
        var userId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .create();
        var ewbId = UUID.randomUUID();
        var ewb = new Ewb()
                .setRequest(Instancio.of(Request.class)
                                     .set(Select.field(Request::getStatus), RequestStatus.IN_PROGRESS)
                                     .create())
                .setDriver(Instancio.of(Driver.class)
                                    .set(Select.field(Driver::getEmployee), Instancio.create(Employee.class))
                                    .create())
                .setId(ewbId)
                .setStatus(EwbStatus.TELEMECH_IN_PROGRESS)
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        var request = new EwbLitreageOutRequest(ewbId, ewb.getTransport().getFuelTankVolume());
        assertThatExceptionOfType(LitreageValidationException.class)
                .isThrownBy(() -> ewbService.litreageOut(request, userId))
                .withMessage(ONLY_EWB_DRIVER_CAN_PROVIDE_LITREAGE_OUT_MSG);
    }
    
    @Test
    @DisplayName("Ввод остатка топлива с заявкой не в статусе IN_PROGRESS")
    void litreageOutRequestNotInProgress() {
        var userId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .create();
        var ewbId = UUID.randomUUID();
        var ewb = new Ewb()
                .setRequest(Instancio.of(Request.class)
                                     .set(Select.field(Request::getStatus), RequestStatus.CANCELED)
                                     .create())
                .setDriver(Instancio.of(Driver.class)
                                    .set(Select.field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                                     .set(Select.field(Employee::getId), userId)
                                                                                     .create())
                                    .create())
                .setId(ewbId)
                .setStatus(EwbStatus.TELEMECH_IN_PROGRESS)
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        var request = new EwbLitreageOutRequest(ewbId, ewb.getTransport().getFuelTankVolume());
        assertThatExceptionOfType(LitreageValidationException.class)
                .isThrownBy(() -> ewbService.litreageOut(request, userId))
                .withMessage(REQUEST_IN_PROGRESS_IS_ABSENT_MSG);
    }
    
    @Test
    @DisplayName("Ввод остатка топлива с некорректным статусом ЭПЛ")
    void litreageOutEwbIncorrectStatus() {
        var userId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .create();
        var ewbId = UUID.randomUUID();
        var ewb = new Ewb()
                .setRequest(Instancio.of(Request.class)
                                     .set(Select.field(Request::getStatus), RequestStatus.IN_PROGRESS)
                                     .create())
                .setDriver(Instancio.of(Driver.class)
                                    .set(Select.field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                                     .set(Select.field(Employee::getId), userId)
                                                                                     .create())
                                    .create())
                .setId(ewbId)
                .setStatus(EWB_CANCELLED)
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        var request = new EwbLitreageOutRequest(ewbId, ewb.getTransport().getFuelTankVolume());
        assertThatExceptionOfType(LitreageValidationException.class)
                .isThrownBy(() -> ewbService.litreageOut(request, userId))
                .withMessage(INCORRECT_EWB_STATUS_MSG);
    }
    
    @Test
    @DisplayName("Ввод остатка топлива со значением больше объёма бака")
    void litreageOutWithGreaterValueThanTankVolume() {
        var userId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var transport = Instancio.of(Transport.class)
                                 .set(Select.field(Transport::getId), transportId)
                                 .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                 .create();
        var ewbId = UUID.randomUUID();
        var ewb = new Ewb()
                .setRequest(Instancio.of(Request.class)
                                     .set(Select.field(Request::getStatus), RequestStatus.IN_PROGRESS)
                                     .create())
                .setDriver(Instancio.of(Driver.class)
                                    .set(Select.field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                                     .set(Select.field(Employee::getId), userId)
                                                                                     .create())
                                    .create())
                .setId(ewbId)
                .setStatus(TELEMECH_IN_PROGRESS)
                .setTransport(transport);
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewb.getId());
        doReturn(Instancio.create(Check.class)).when(checkPhotoService).validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        var request = new EwbLitreageOutRequest(ewbId, ewb.getTransport().getFuelTankVolume() + 1);
        assertThatExceptionOfType(LitreageValidationException.class)
                .isThrownBy(() -> ewbService.litreageOut(request, userId))
                .withMessage(LITREAGE_OUT_GREATER_THAN_POSSIBLE_VALUE_MSG.formatted(ewb.getTransport().getFuelTankVolume()));
    }
    
    @Test
    void shouldReturnMedicalCheckupModelWithExamInfo() throws IOException {
        var titleUUID = UUID.randomUUID();
        var mockTitle = Instancio.create(EwbTitle.class);
        doReturn(Optional.of(mockTitle)).when(ewbTitleRepository).findById(titleUUID);
        var file = Instancio.create(FileData.class);
        doReturn(file).when(fileService).get(anyString());
        
        var actualResult = ewbService.getStraightSecondTitle(titleUUID);
        
        assertThat(actualResult.data()).isNotNull();
        assertThat(actualResult.signature()).isNotNull();
        assertThat(actualResult.name()).isNotNull();
        assertThat(actualResult.withExamInfo()).isTrue();
    }
    
    @Test
    void shouldReturnMedicalCheckupModelWithoutExamInfo() throws IOException {
        var titleUUID = UUID.randomUUID();
        var mockTitle = Instancio.of(EwbTitle.class)
                                 .create();
        mockTitle.getEwb().setMedicRequest(null);
        doReturn(Optional.of(mockTitle)).when(ewbTitleRepository).findById(titleUUID);
        
        var actualResult = ewbService.getStraightSecondTitle(titleUUID);
        
        assertThat(actualResult.data()).isNull();
        assertThat(actualResult.signature()).isNull();
        assertThat(actualResult.name()).isNotNull();
        assertThat(actualResult.withExamInfo()).isFalse();
    }
    
    @Test
    void shouldThrowEwbTitleNotFoundExceptionWhenTitleIsMissing() {
        var titleUUID = UUID.randomUUID();
        given(ewbTitleRepository.findById(titleUUID)).willReturn(Optional.empty());
        
        Throwable thrown = catchThrowable(() -> ewbService.getStraightSecondTitle(titleUUID));
        
        assertThat(thrown).isInstanceOf(EwbTitleNotFoundException.class);
    }
    
    private static ru.sber.transport.telemechanic.dto.ewb.xjb.File createFile() {
        var file = new ru.sber.transport.telemechanic.dto.ewb.xjb.File();
        file.setDocument(new Document());
        file.getDocument().setInformationDate("15.11.2024");
        file.getDocument().setInformationTime("18:00:45");
        file.getDocument().setRoute(new Route());
        file.getDocument().getRoute().setOwner(new Owner());
        file.getDocument().getRoute().getOwner().setOwnerDetails(new OwnerDetails());
        file.getDocument().getRoute().getOwner().getOwnerDetails().setOrganizationInfo(new OrganizationInfo());
        file.getDocument().getRoute().getOwner().setAddress(new Address());
        file.getDocument().getRoute().getOwner().getAddress().setAddressRf(new AddressRf());
        file.getDocument().getRoute().getOwner().setContact(new Contact());
        file.getDocument().setSigningPersonInfo(new SigningPersonInfo());
        return file;
    }
}
