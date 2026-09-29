package ru.sberbank.ditsib.transport.vehicle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;
import ru.sberbank.ditsib.transport.vehicle.database.dao.*;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.database.projection.TransportShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.GetIndicatorValueDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransportInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.OrganizationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchWithStructureRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.VehicleCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDtoV2;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchWithStructureResponseDto;
import ru.sberbank.ditsib.transport.vehicle.exception.AccessiblePositionNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.exception.TransportNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.mapper.*;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.TransportSender;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.vehicle.service.impl.TransportServiceImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransportServiceTest {
    @Spy
    private TransportMapper transportMapper = new TransportMapperImpl(
            new OrganizationMapperImpl(),
            new DepartmentMapperImpl(),
            new FuelTypeMapperImpl()
    );
    @Spy
    private ReportMapper reportMapper = Mappers.getMapper(ReportMapper.class);
    private TransportServiceImpl transportService;

    @Mock
    private TransportRepository transportRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SubtypeRepository subtypeRepository;
    @Mock
    private TelematicsRepository telematicsRepository;
    @Mock
    private FuelConsumptionRepository fuelConsumptionRepository;
    @Mock
    private OdometerValueRepository odometerValueRepository;
    @Mock
    private OdometerHistoryRepository odometerHistoryRepository;
    @Mock
    private BrandService brandService;
    @Mock
    private ModelService modelService;
    @Spy
    private OdometerHistoryMapper odometerHistoryMapper = new OdometerHistoryMapperImpl();
    @Mock
    private IndicatorMapper indicatorMapper;
    @Mock
    private TransportSender transportSender;
    @Captor
    private ArgumentCaptor<Transport> transportArgumentCaptor;
    @Captor
    private ArgumentCaptor<OdometerHistory> odometerHistoryArgumentCaptor;
    @Captor
    private ArgumentCaptor<OdometerValue> odometerValueArgumentCaptor;
    @Mock
    private Clock clock;
    @Mock
    private AccessiblePositionRepository accessiblePositionRepository;

    private static final Clock FIXED_CLOCK = Clock.fixed(LocalDateTime.of(2023, 1, 17, 0, 0)
                                                                      .toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));


    @BeforeEach
    void hackJunit5Bug() {
        odometerHistoryMapper.setObjectMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        transportService = new TransportServiceImpl(clock, departmentRepository, transportRepository, transportMapper, employeeService,
                                                    organizationService,
                                                    departmentService, vehicleRepository, subtypeRepository, telematicsRepository,
                                                    fuelConsumptionRepository, odometerValueRepository, odometerHistoryRepository,
                                                    accessiblePositionRepository, odometerHistoryMapper, reportMapper, indicatorMapper,
                                                    transportSender);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void createFailTest(String testCase, TransportCreateDto createDto, Runnable mockingChanger, Class<Exception> expectedException, String expectedMessage) {
        var userId = UUID.randomUUID();
        var organizationIds = createDto.organizations().stream().map(OrganizationRequestDto::organizationId).collect(Collectors.toSet());
        var departmentIds = createDto.organizations().stream().map(OrganizationRequestDto::departmentId).collect(Collectors.toSet());

        var roles = Set.of(Role.ROLE_ADMIN_DATA_MASTER.name());
        
        List<Organization> organizations = Stream.generate(() -> Instancio.of(Organization.class).create())
                                                 .limit(organizationIds.size())
                                                 .toList();
        List<Department> departments = Stream.generate(() -> Instancio.of(Department.class).create())
                                                   .limit(departmentIds.size())
                                                   .toList();
        List<Object[]> mockDepToOrgPairs = new ArrayList<>();
        for (var organization : createDto.organizations()) {
            mockDepToOrgPairs.add(new Object[]{organization.departmentId(), organization.organizationId()});
        }
        
        doReturn(mockDepToOrgPairs).when(departmentRepository).findDepOrgPairsByIds(any());
        doReturn(0L).when(transportRepository).count(any(Specification.class));
        doReturn(List.of(new AccessiblePosition(createDto.accessiblePositionId(), "Главарь банды"))).when(accessiblePositionRepository).findAll();
        doReturn(Instancio.create(Employee.class)).when(employeeService).getByUserId(userId);
        doReturn(organizations).when(organizationService).getAllByIds(organizationIds);
        doReturn(departments).when(departmentService).getAllById(departmentIds);
        doReturn(Optional.of(Instancio.create(Subtype.class))).when(subtypeRepository).findById(createDto.vehicle().subtypeId());
        doReturn(Optional.of(Instancio.create(Telematics.class))).when(telematicsRepository).findById(createDto.vehicle().telematicsId());
        doReturn(Optional.of(Instancio.create(Vehicle.class))).when(vehicleRepository).findById(createDto.vehicle().id());
        doNothing().when(transportSender).send(any(Transport.class), anyBoolean());

        mockingChanger.run();

        var exception = assertThrows(expectedException, () -> transportService.create(createDto));
        assertThat(exception)
                .hasMessage(expectedMessage);
        verify(transportRepository, never()).saveAndFlush(any());
        verify(transportRepository, never()).save(any());
    }

    private Stream<Arguments> createFailTest() {
        var vehicleDto = Instancio.of(VehicleCreateDto.class)
                .set(Select.field(VehicleCreateDto::vinCode), "012318964809885")
                .set(Select.field(VehicleCreateDto::assetNumber), "0718695691")
                .set(Select.field(VehicleCreateDto::stateNumber), "B168ТС57")
                .create();
        var createDto = Instancio.of(TransportCreateDto.class)
                .set(Select.field(TransportCreateDto::contractorId), null)
                .set(Select.field(TransportCreateDto::autoparkId), null)
                .set(Select.field(TransportCreateDto::vehicle), vehicleDto)
                .create();

        return Stream.of(
                Arguments.of(
                        "Vehicle not found",
                        createDto,
                        (Runnable) () -> {
                            Mockito.reset(vehicleRepository);
                            doReturn(Optional.empty()).when(vehicleRepository).findById(createDto.vehicle().id());
                        },
                        EntityNotFoundException.class,
                        "Data not found: Entity: Vehicle, ID: %s".formatted(createDto.vehicle().id())
                ),
                Arguments.of(
                        "Telematics not found",
                        createDto,
                        (Runnable) () -> {
                            Mockito.reset(telematicsRepository);
                            doReturn(Optional.empty()).when(telematicsRepository).findById(createDto.vehicle().telematicsId());
                        },
                        EntityNotFoundException.class,
                        "Data not found: Entity: Telematics, ID: %s".formatted(createDto.vehicle().telematicsId())
                ),
                Arguments.of(
                        "Subtype not found",
                        createDto,
                        (Runnable) () -> {
                            Mockito.reset(subtypeRepository);
                            doReturn(Optional.empty()).when(subtypeRepository).findById(createDto.vehicle().subtypeId());
                        },
                        EntityNotFoundException.class,
                        "Data not found: Entity: Subtype, ID: %s".formatted(createDto.vehicle().subtypeId())
                ),
                Arguments.of(
                        "Unique constraint violation",
                        createDto,
                        (Runnable) () -> {
                            Mockito.reset(transportRepository);
                            var transport = List.of(Instancio.of(Transport.class)
                                    .set(Select.field(Transport::getVinCode), createDto.vehicle().vinCode())
                                    .set(Select.field(Transport::getAssetNumber), createDto.vehicle().assetNumber())
                                    .set(Select.field(Transport::getStateNumber), createDto.vehicle().stateNumber())
                                    .set(Select.field(Transport::getStatus), TransportStatus.IN_USE)
                                    .create());
                            doReturn(transport).when(transportRepository).findAll(any(Specification.class));
                        },
                        EntityAlreadyExistsException.class,
                        "Транспортное средство с VIN номером %s, номером основного средства %s, гос. номером %s уже существует"
                                .formatted(createDto.vehicle().vinCode(), createDto.vehicle().assetNumber(), createDto.vehicle().stateNumber())
                ),
                Arguments.of(
                        "Accessible position not found",
                        createDto,
                        (Runnable) () -> {
                            Mockito.reset(transportRepository);
                            doReturn(List.of()).when(accessiblePositionRepository).findAll();
                        },
                        AccessiblePositionNotFoundException.class,
                        "Не найдена в справочнике позиция с идентификатором " + createDto.accessiblePositionId()
                )
        );
    }

    @Test
    @DisplayName("Получение показания одометра")
    void getIndicatorValue() {
        var transportId = UUID.randomUUID();
        var odometerValue1 = Instancio.create(OdometerValue.class);
        odometerValue1.setYear(LocalDateTime.now().getYear());
        odometerValue1.setMonth(LocalDateTime.now().getMonth());
        var odometerValue2 = Instancio.create(OdometerValue.class);
        odometerValue2.setYear(LocalDateTime.now().getYear());
        odometerValue2.setMonth(LocalDateTime.now().minusMonths(1).getMonth());
        doReturn(List.of(odometerValue1, odometerValue2)).when(odometerValueRepository).findByTransportId(transportId);
        doReturn(new GetIndicatorValueDto(odometerValue1.getValue())).when(indicatorMapper).odometerValueToGetIndicatorValueDto(any(OdometerValue.class));
        var result = assertDoesNotThrow(() -> transportService.getIndicatorValue(transportId));
        assertEquals(odometerValue1.getValue(), result.value());

        doReturn(emptyList()).when(odometerValueRepository).findByTransportId(transportId);
        assertThrows(EntityNotFoundException.class, () -> transportService.getIndicatorValue(transportId));
    }

    @Test
    @DisplayName("Поиск транспортного средства по гос. номеру с учетом автомобилей по штатной структуре")
    void searchOrganization() {
        var employee = Instancio.create(Employee.class);
        var departmentIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var searchDto = new TransportSearchWithStructureRequestDto(new PageSettingDto(0, 10), Instancio.create(String.class));
        var responseDto1 = Instancio.create(TransportSearchWithStructureResponseDto.class);
        var responseDto2 = Instancio.create(TransportSearchWithStructureResponseDto.class);
        var factory = new SpelAwareProxyProjectionFactory();
        var projection1 = factory.createProjection(TransportShortProjection.class);
        projection1.setId(responseDto1.id());
        projection1.setModel(responseDto1.model());
        projection1.setBrand(responseDto1.brand());
        projection1.setStateNumber(responseDto1.stateNumber());
        var projection2 = factory.createProjection(TransportShortProjection.class);
        projection2.setId(responseDto2.id());
        projection2.setModel(responseDto2.model());
        projection2.setBrand(responseDto2.brand());
        projection2.setStateNumber(responseDto2.stateNumber());
        var projections = List.of(projection1, projection2);
        var pageable = PageRequest.of(searchDto.page().page(), searchDto.page().size());
        var projectionsPage = new PageImpl<>(projections, pageable, 2);
        var responseDtoList = List.of(responseDto1, responseDto2);
        when(employeeService.getByUserId(employee.getUserId())).thenReturn(employee);
        when(departmentService.getParentDepartments(employee.getDepartment().getId())).thenReturn(departmentIds);
        when(transportRepository.findTransportWithStructure(departmentIds, searchDto.searchText(), pageable)).thenReturn(projectionsPage);
        when(transportMapper.listTransportShortProjectionToListTransportSearchWithStructureResponseDto(projections)).thenReturn(responseDtoList);
        var result = transportService.searchWithStructure(searchDto, employee.getUserId());
        assertEquals(responseDtoList, result.getContent());
        assertEquals(0, result.getNumber());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getNumberOfElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    @DisplayName("Не выводить в выборку автомобили, выведенные из эксплуатации")
    void searchOrganizationWithTransportNotInUse() {
        var employee = Instancio.create(Employee.class);
        var departmentIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var searchDto = new TransportSearchWithStructureRequestDto(new PageSettingDto(0, 10), Instancio.create(String.class));
        List<TransportShortProjection> projections = emptyList();
        var pageable = PageRequest.of(searchDto.page().page(), searchDto.page().size());
        var projectionsPage = new PageImpl<>(projections, pageable, 0);
        List<TransportSearchWithStructureResponseDto> responseDtoList = emptyList();
        when(employeeService.getByUserId(employee.getUserId())).thenReturn(employee);
        when(departmentService.getParentDepartments(employee.getDepartment().getId())).thenReturn(departmentIds);
        when(transportRepository.findTransportWithStructure(departmentIds, searchDto.searchText(), pageable)).thenReturn(projectionsPage);
        when(transportMapper.listTransportShortProjectionToListTransportSearchWithStructureResponseDto(projections)).thenReturn(responseDtoList);
        var result = transportService.searchWithStructure(searchDto, employee.getUserId());
        assertEquals(responseDtoList, result.getContent());
        assertEquals(0, result.getNumber());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void getTransportByStateNumber() {
        doReturn(Optional.of(Instancio.create(Transport.class))).when(transportRepository).findTransportByStateNumber(anyString());
        var mappedTransport = Instancio.create(TransportInfoDto.class);
        doReturn(mappedTransport).when(transportMapper).transportToTransportInfoDto(any(Transport.class));
        var actual = transportService.getTransportByStateNumber("A777AA77");
        assertNotNull(actual);
        assertEquals(mappedTransport.id(), actual.id());
        assertEquals(mappedTransport.stateNumber(), actual.stateNumber());
        assertEquals(mappedTransport.brand(), actual.brand());
        assertEquals(mappedTransport.model(), actual.model());
        assertEquals(mappedTransport.transportType(), actual.transportType());
        assertEquals(mappedTransport.status(), actual.status());

        doReturn(Optional.empty()).when(transportRepository).findTransportByStateNumber(anyString());
        var exception = assertThrows(TransportNotFoundException.class, () -> transportService.getTransportByStateNumber("A777AA77"));
        assertEquals("Транспорт с гос.номером 'A777AA77' не найден", exception.getMessage());
    }

    @Test
    void checkTransportConsumptionRates() {
        doReturn(Optional.of(Instancio.create(Transport.class))).when(transportRepository).findById(any(UUID.class));
        var transportId = UUID.randomUUID();
        var result = transportService.getById(transportId);
        assertNotNull(result.engine().cityConsumptionRate());
        assertNotNull(result.engine().countryConsumptionRate());
        assertNotNull(result.engine().hybridConsumptionRate());
    }

    @Test
    void getMileage() {
        var odometerHistory = Instancio.create(OdometerHistory.class);
        var uuid = UUID.randomUUID();
        doReturn(Optional.of(odometerHistory)).when(odometerHistoryRepository).findById(uuid);
        var actual = transportService.getMileage(uuid);
        assertEquals(odometerHistory.getValue(), actual);

        doReturn(Optional.empty()).when(odometerHistoryRepository).findById(uuid);
        var exception = assertThrows(EntityNotFoundException.class, () -> transportService.getMileage(uuid));
        assertEquals(String.format("Data not found: Entity: %s, ID: %s", OdometerHistory.class.getSimpleName(), uuid), exception.getMessage());

    }

    @Test
    void addOdometerHistory() {
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        var transportId = UUID.randomUUID();
        var value = 10250;
        var creatorUserId = UUID.randomUUID();
        var creationTime = LocalDateTime.now(clock);
        var attributes = Map.<String, Object>of("EWB_ID", "422579f1-501a-40c3-bdb0-ee0f7f396a1c", "DIRECTION", "OUT");

        var message = new OdometerHistoryValueMessage(transportId, value, creatorUserId, creationTime, attributes);
        doNothing().when(transportSender).send(transportArgumentCaptor.capture(), eq(false));
        var transport = Instancio.create(Transport.class);
        transport.setId(transportId);
        doReturn(Optional.of(transport)).when(transportRepository).findById(transportId);
        when(odometerHistoryRepository.save(odometerHistoryArgumentCaptor.capture()))
                .thenAnswer(invocationOnMock -> invocationOnMock.<OdometerHistory>getArgument(0));
        when(odometerValueRepository.save(odometerValueArgumentCaptor.capture()))
                .thenAnswer(invocationOnMock -> invocationOnMock.<OdometerValue>getArgument(0));

        transportService.addIndicatorsHistory(message);

        assertThat(transportArgumentCaptor.getValue())
                .isNotNull()
                .extracting(Transport::getId, Transport::getCurrentMileage)
                .containsExactly(transportId, value);

        assertThat(odometerValueArgumentCaptor.getValue())
                .isNotNull()
                .extracting(OdometerValue::getTransportId, OdometerValue::getValue)
                .containsExactly(transportId, value);

        assertThat(odometerHistoryArgumentCaptor.getValue())
                .isNotNull()
                .extracting(OdometerHistory::getTransportId, OdometerHistory::getValue)
                .containsExactly(transportId, value);
    }

    @Test
    void addOdometerHistoryOdometerValuePresent() {
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        var transportId = UUID.randomUUID();
        var value = 10250;
        var creatorUserId = UUID.randomUUID();
        var creationTime = LocalDateTime.now(clock);
        var attributes = Map.<String, Object>of("EWB_ID", "422579f1-501a-40c3-bdb0-ee0f7f396a1c", "DIRECTION", "OUT");

        var message = new OdometerHistoryValueMessage(transportId, value, creatorUserId, creationTime, attributes);
        doNothing().when(transportSender).send(transportArgumentCaptor.capture(), eq(false));
        var transport = Instancio.create(Transport.class);
        transport.setId(transportId);
        doReturn(Optional.of(transport)).when(transportRepository).findById(transportId);
        when(odometerHistoryRepository.save(odometerHistoryArgumentCaptor.capture()))
                .thenAnswer(invocationOnMock -> invocationOnMock.<OdometerHistory>getArgument(0));

        var odometerValue = Instancio.create(OdometerValue.class);
        odometerValue.setTransportId(transportId);
        odometerValue.setMonth(creationTime.getMonth());
        odometerValue.setYear(creationTime.getYear());
        doReturn(List.of(odometerValue))
                .when(odometerValueRepository).findByTransportId(transportId);
        when(odometerValueRepository.save(odometerValueArgumentCaptor.capture()))
                .thenAnswer(invocationOnMock -> invocationOnMock.<OdometerValue>getArgument(0));

        transportService.addIndicatorsHistory(message);

        assertThat(transportArgumentCaptor.getValue())
                .isNotNull()
                .extracting(Transport::getId, Transport::getCurrentMileage)
                .containsExactly(transportId, value);

        assertThat(odometerValueArgumentCaptor.getValue())
                .isNotNull()
                .extracting(OdometerValue::getTransportId, OdometerValue::getValue)
                .containsExactly(transportId, value);

        assertThat(odometerHistoryArgumentCaptor.getValue())
                .isNotNull()
                .extracting(OdometerHistory::getTransportId, OdometerHistory::getValue)
                .containsExactly(transportId, value);
    }


    @Test
    void addOdometerHistoryFailsTransportNotExists() {
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        var transportId = UUID.randomUUID();
        var value = 10250;
        var creatorUserId = UUID.randomUUID();
        var creationTime = LocalDateTime.now(clock);
        var attributes = Map.<String, Object>of("EWB_ID", "422579f1-501a-40c3-bdb0-ee0f7f396a1c", "DIRECTION", "OUT");

        var message = new OdometerHistoryValueMessage(transportId, value, creatorUserId, creationTime, attributes);
        var transport = Instancio.create(Transport.class);
        transport.setId(transportId);

        assertThatThrownBy(() -> transportService.addIndicatorsHistory(message))
                .isInstanceOf(EntityNotFoundException.class);

        verify(transportSender, never()).send(any(), anyBoolean());
    }
    
    @Test
    void createTransportReport() {
        var orgId = UUID.randomUUID();
        TransportReportProjection transportReportProjection = getTransportReportProjection();
    
        doReturn(List.of(transportReportProjection)).when(transportRepository).findAllByOrganizationId(orgId);
        
        var actual = transportService.getInfoForTransportReport(orgId);
        assertEquals(1, actual.size());
        var transportReportDto = actual.get(0);
        
        assertEquals(transportReportProjection.getOfficialName(), transportReportDto.officialName());
        assertEquals(transportReportProjection.getDepartmentName(), transportReportDto.departmentName());
        assertEquals(transportReportProjection.getCurrentMileage(), transportReportDto.currentMileage());
        assertEquals(transportReportProjection.getStatus(), transportReportDto.status());
        assertEquals(transportReportProjection.getYear(), transportReportDto.year());
        assertEquals(transportReportProjection.getVehicleType(), transportReportDto.vehicleType());
        assertEquals(transportReportProjection.getModelTitle(), transportReportDto.modelTitle());
        assertEquals(transportReportProjection.getBrandTitle(), transportReportDto.brandTitle());
        assertEquals(transportReportProjection.getCategoryTitle(), transportReportDto.categoryTitle());
        assertEquals(transportReportProjection.getManufacturer(), transportReportDto.manufacturer());
        assertEquals(transportReportProjection.getEcologicalClass(), transportReportDto.ecologicalClass());
        assertEquals(transportReportProjection.getEnginePower(), transportReportDto.enginePower());
        assertEquals(transportReportProjection.getEngineTypeTitle(), transportReportDto.engineTypeTitle());
        assertEquals(transportReportProjection.getEngineCapacity(), transportReportDto.engineCapacity());
        assertEquals(transportReportProjection.getFuelTankVolume(), transportReportDto.fuelTankVolume());
        assertEquals(transportReportProjection.getMudguardInstalled(), transportReportDto.mudguardInstalled());
        assertEquals(transportReportProjection.getSpareWheelHolderInstalled(), transportReportDto.spareWheelHolderInstalled());
        assertEquals(transportReportProjection.getWeight(), transportReportDto.weight());
        assertEquals(transportReportProjection.getMaxWeight(), transportReportDto.maxWeight());
        assertEquals(transportReportProjection.getHeight(), transportReportDto.height());
        assertEquals(transportReportProjection.getWidth(), transportReportDto.width());
        assertEquals(transportReportProjection.getLength(), transportReportDto.length());
        assertEquals(transportReportProjection.getServiceIntervalDays(), transportReportDto.serviceIntervalDays());
        assertEquals(transportReportProjection.getServiceIntervalMileage(), transportReportDto.serviceIntervalMileage());
        assertEquals(transportReportProjection.getServiceAuthorizationDays(), transportReportDto.serviceAuthorizationDays());
        assertEquals(transportReportProjection.getServiceAuthorizationMileage(), transportReportDto.serviceAuthorizationMileage());
    }

    @Test
    @DisplayName("searchWithBrandAndModel - успех: возвращает данные с брендом/моделью")
    void searchWithBrandAndModelSuccessTest() {
        var pageSetting = new PageSettingDto(0, 10);
        var requestDto = TransportSearchingRequestDto.builder()
                .page(pageSetting)
                .searchText("A123AA")
                .organizationId(UUID.randomUUID())
                .build();

        var transport1 = Instancio.of(Transport.class).create();
        var transport2 = Instancio.of(Transport.class).create();
        var transports = List.of(transport1, transport2);

        var transportsPage = new PageImpl<>(transports, PageRequest.of(0, 10), 2L);

        var expectedDto1 = new TransportSearchResponseDtoV2(
                UUID.fromString("91275069-9780-41b6-b4c8-d5e3363c621f"),
                "А010АА01", "SDFVFFDSDDF", TransportStatus.IN_USE,
                UUID.fromString("b29badca-4d4e-4705-881b-f04e0aac97ca"),
                UUID.fromString("3d30f41d-2186-4d04-b695-ffb6e6be7b87"),
                "Mercedes-Benz", "S500"
        );
        var expectedDto2 = new TransportSearchResponseDtoV2(
                UUID.fromString("91275069-9780-41b6-b4c8-d5e3363c6211"),
                "А010АА01", "SDFVFFDSDDF", TransportStatus.IN_USE,
                UUID.fromString("b29badca-4d4e-4705-881b-f04e0aac97ca"),
                UUID.fromString("3d30f41d-2186-4d04-b695-ffb6e6be7b87"),
                "Mercedes-Benz", "S500"
        );

        when(transportMapper.transportToTransportSearchWithBrandAndModelResponseDto(transport1))
                .thenReturn(expectedDto1);
        when(transportMapper.transportToTransportSearchWithBrandAndModelResponseDto(transport2))
                .thenReturn(expectedDto2);

        when(transportRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(transportsPage);

        var result = transportService.searchWithBrandAndModel(requestDto);

        assertThat(result.getContent()).containsExactly(expectedDto1, expectedDto2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getPageable()).isEqualTo(PageRequest.of(0, 10));

        verify(transportMapper, times(2)).transportToTransportSearchWithBrandAndModelResponseDto(any(Transport.class));
    }




    @Test
    @DisplayName("search - успех: возвращает данные с учетом списка контрагентов")
    void searchContractorIds() {
        var transport1 = Instancio.of(Transport.class).create();
        var transport2 = Instancio.of(Transport.class).create();
        var transports = List.of(transport1, transport2);

        var pageSetting = new PageSettingDto(0, 10);
        var requestDto = TransportSearchingRequestDto.builder()
                .page(pageSetting)
                .searchText("A123AA")
                .contractorIds(List.of(transport1.getContractorId(), transport2.getContractorId()))
                .build();


        var transportsPage = new PageImpl<>(transports, PageRequest.of(0, 10), 5L);

        var transportSearchResponseDto = new TransportSearchResponseDto(
                transport1.getId(),
                transport1.getStateNumber(),
                transport1.getVinCode(),
                transport1.getStatus(),
                transport1.getContractorId(),
                transport1.getAutoparkId()
        );
        var expectedDtos = List.of(transportSearchResponseDto);

        when(transportRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(transportsPage);
        when(transportMapper.transportToTransportResponseDtoList(transportsPage.getContent()))
                .thenReturn(expectedDtos);

        var result = transportService.search(requestDto);

        assertThat(result.getContent()).isEqualTo(expectedDtos);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getPageable()).isEqualTo(transportsPage.getPageable());

        verify(transportRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @NotNull
    private TransportReportProjection getTransportReportProjection() {
        var factory = new SpelAwareProxyProjectionFactory();
        var transportReportProjection = factory.createProjection(TransportReportProjection.class);
        transportReportProjection.setOfficialName("Дальневосточный банк (ДВБ)");
        transportReportProjection.setDepartmentName("Отдел трансп обеспечения УРМ г. Магадан");
        transportReportProjection.setCurrentMileage(6);
        transportReportProjection.setStatus("IN_USE");
        transportReportProjection.setYear(2007);
        transportReportProjection.setVehicleType("Легковой");
        transportReportProjection.setModelTitle("2114");
        transportReportProjection.setBrandTitle("Лада");
        transportReportProjection.setCategoryTitle("Легковые автомобили, небольшие грузовики (до 3,5 тонн)");
        transportReportProjection.setManufacturer("ВАЗ");
        transportReportProjection.setEcologicalClass("4");
        transportReportProjection.setEnginePower(240);
        transportReportProjection.setEngineTypeTitle("БЕНЗИН");
        transportReportProjection.setEngineCapacity(1400);
        transportReportProjection.setFuelTankVolume(50);
        transportReportProjection.setMudguardInstalled(true);
        transportReportProjection.setSpareWheelHolderInstalled(false);
        transportReportProjection.setWeight(700);
        transportReportProjection.setMaxWeight(900);
        transportReportProjection.setHeight(1400);
        transportReportProjection.setWidth(1650);
        transportReportProjection.setLength(4122);
        transportReportProjection.setServiceIntervalDays(10000);
        transportReportProjection.setServiceIntervalMileage(180);
        transportReportProjection.setServiceAuthorizationDays(10);
        transportReportProjection.setServiceAuthorizationMileage(2000);
        transportReportProjection.setYearManufactureBegin(2000);
        return transportReportProjection;
    }
    
}
