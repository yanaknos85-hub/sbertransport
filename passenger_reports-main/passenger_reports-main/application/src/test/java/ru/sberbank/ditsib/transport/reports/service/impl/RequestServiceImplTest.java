package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.magenta.OrderKpi;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRideKPI;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@AutoConfigureMockMvc
@DisplayName("Проверка нового поиска заявок")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class RequestServiceImplTest extends KafkaTest {
    
    @Autowired
    RequestRepository requestRepository;
    
    @Autowired
    DepartmentRepository departmentRepository;
    
    @Autowired
    OrganizationRepository organizationRepository;
    
    @Autowired
    PositionRepository positionRepository;
    
    @Autowired
    EmployeeRepository employeeRepository;
    
    @Autowired
    RequestService requestService;
    
    @Autowired
    TripPurposeRepository purposeRepository;
    
    @Autowired
    SharedRequestKpiRepository sharedRequestKpiRepository;
    
    @Autowired
    OrderKpiRepository orderKpiRepository;
    
    @Autowired
    SharedRideRepository sharedRideRepository;
    
    @Autowired
    AddressRepository addressRepository;
    
    @Autowired
    WaypointRepository waypointRepository;
    
    UUID departmentEvenId = null;
    UUID departmentOddId = null;
    
    final LocalDateTime creationDateEven = LocalDateTime.of(2023, 7, 22, 12, 0);
    final LocalDateTime creationDateOdd = LocalDateTime.of(2023, 7, 23, 12, 0);
    final LocalDateTime desiredDateEven = LocalDateTime.of(2023, 7, 22, 12, 0);
    final LocalDateTime desiredDateOdd = LocalDateTime.of(2023, 7, 23, 12, 0);
    final LocalDateTime changeDateEven = LocalDateTime.of(2023, 7, 24, 12, 0);
    final LocalDateTime changeDateOdd = LocalDateTime.of(2023, 7, 25, 12, 0);
    final LocalDateTime approvalDateEven = LocalDateTime.of(2023, 7, 24, 12, 0);
    final LocalDateTime approvalDateOdd = LocalDateTime.of(2023, 7, 25, 12, 0);
    final LocalDateTime orderPaymentFormationFinishingDateEven = LocalDateTime.of(2023, 7, 24, 12, 0);
    final LocalDateTime orderPaymentFormationFinishingDateOdd = LocalDateTime.of(2023, 7, 25, 12, 0);
    final LocalDateTime orderPaymentFormationStartDateEven = LocalDateTime.of(2023, 7, 24, 12, 0);
    final LocalDateTime orderPaymentFormationStartDateOdd = LocalDateTime.of(2023, 7, 25, 12, 0);
    final String statusEven = TripRequestStatus.PERSONAL_APPROVED.name();
    final String statusOdd = TripRequestStatus.PERSONAL_PAYMENT_DONE.name();
    final UUID organizationEvenId = UUID.fromString("cdb8ab05-eec5-4b3b-a09b-0f9ae9d3101b");
    final UUID organizationOddId = UUID.fromString("8b1a952c-4911-46bd-86a0-fbe026dabd42");
    UUID sharedRideIdForSearch = null;
    
    void beforeEach() {
        var departments = departmentRepository.findByDepartmentName("DepartmentEven");
        if (departments != null && !departments.isEmpty()) {
            departmentEvenId = departments.get(0).getId();
        }
        departments = departmentRepository.findByDepartmentName("DepartmentOdd");
        if (departments != null && !departments.isEmpty()) {
            departmentOddId = departments.get(0).getId();
        }
        if (departmentEvenId != null || departmentOddId != null) {
            return;
        }
        
        Integer balanceUnitEven = 1300;
        Integer balanceUnitOdd = 4400;
        
        Organization organizationEven = Organization.builder()
                                                    .id(organizationEvenId)
                                                    .officialName("organizationEven")
                                                    .build();
        organizationEven = organizationRepository.save(organizationEven);
        
        Organization organizationOdd = Organization.builder()
                                                   .id(organizationOddId)
                                                   .officialName("organizationOdd")
                                                   .build();
        organizationOdd = organizationRepository.save(organizationOdd);
        
        
        departmentEvenId = UUID.randomUUID();
        Department departmentEven = Department.builder()
                                              .id(departmentEvenId)
                                              .departmentName("DepartmentEven")
                                              .organizationId(organizationEvenId)
                                              .build();
        departmentEven = departmentRepository.save(departmentEven);
        
        departmentOddId = UUID.randomUUID();
        Department departmentOdd = Department.builder()
                                             .id(departmentOddId)
                                             .departmentName("DepartmentOdd")
                                             .organizationId(organizationOddId)
                                             .build();
        departmentOdd = departmentRepository.save(departmentOdd);
        
        ItinerantType itinerantTypeEven = ItinerantType.NONE;
        ItinerantType itinerantTypeOdd = ItinerantType.FULL;
        
        var positionEvenId = UUID.randomUUID();
        Position positionEven = Position.builder()
                                        .id(positionEvenId)
                                        .name("positionEven")
                                        .build();
        positionEven = positionRepository.save(positionEven);
        
        var positionOddId = UUID.randomUUID();
        Position positionOdd = Position.builder()
                                       .id(positionOddId)
                                       .name("positionOdd")
                                       .build();
        positionOdd = positionRepository.save(positionOdd);
        
        var passengerEvenId = UUID.randomUUID();
        Employee passengerEven = Employee.builder()
                                         .id(passengerEvenId)
                                         .costCenter("CostCenterEven")
                                         .department(departmentEven)
                                         .firstName("Иван")
                                         .lastName("Петров")
                                         .patronymic("Сидорович")
                                         .itinerantType(itinerantTypeEven)
                                         .marriageCertificateNumber("marriageCertificateNumberEven")
                                         .mobilePhone("79252222222")
                                         .organization(organizationEven)
                                         .humanReadableId("US-0001-00000222")
                                         .position(positionEven)
                                         .build();
        passengerEven = employeeRepository.save(passengerEven);
        
        var passengerOddId = UUID.randomUUID();
        Employee passengerOdd = Employee.builder()
                                        .id(passengerOddId)
                                        .costCenter("CostCenterOdd")
                                        .department(departmentOdd)
                                        .firstName("Алексей")
                                        .lastName("Садыков")
                                        .patronymic("Васильевич")
                                        .itinerantType(itinerantTypeOdd)
                                        .marriageCertificateNumber("marriageCertificateNumberOdd")
                                        .mobilePhone("79251111111")
                                        .organization(organizationOdd)
                                        .humanReadableId("US-0001-00000111")
                                        .position(positionOdd)
                                        .build();
        passengerOdd = employeeRepository.save(passengerOdd);
        
        ExpectedData expectedDataEven = ExpectedData.builder()
                                                    .cost(100.00)
                                                    .distance(5.0)
                                                    .time(Duration.of(10, ChronoUnit.MINUTES))
                                                    .build();
        
        ExpectedData expectedDataOdd = ExpectedData.builder()
                                                   .cost(2000.00)
                                                   .distance(95.0)
                                                   .time(Duration.of(60, ChronoUnit.MINUTES))
                                                   .build();
        
        var purposeEvenId = UUID.randomUUID();
        TripPurpose purposeEven = TripPurpose.builder()
                                             .id(purposeEvenId)
                                             .purpose("purposeEven")
                                             .organization(organizationEvenId)
                                             .build();
        purposeEven = purposeRepository.save(purposeEven);
        
        var purposeOddId = UUID.randomUUID();
        TripPurpose purposeOdd = TripPurpose.builder()
                                            .id(purposeOddId)
                                            .purpose("purposeOdd")
                                            .organization(organizationOddId)
                                            .build();
        purposeOdd = purposeRepository.save(purposeOdd);
        
        var tariffId = UUID.randomUUID();
        Request requestEven = null;
        UUID requestEvenId = null;
        
        Request requestOdd = null;
        UUID requestOddId = null;
        
        Address addressStart = Address.builder()
                                      .id(UUID.randomUUID())
                                      .country("countryStart")
                                      .region("regionStart")
                                      .city("cityStart")
                                      .street("streetStart")
                                      .house("houseStart")
                                      .building("buildingStart")
                                      .structure("structureStart")
                                      .existInVspGosbTbRegistry(false)
                                      .build();
        addressStart = addressRepository.save(addressStart);
        
        Address addressEnd = Address.builder()
                                    .id(UUID.randomUUID())
                                    .country("countryEnd")
                                    .region("regionEnd")
                                    .city("cityEnd")
                                    .street("streetEnd")
                                    .house("houseEnd")
                                    .building("buildingEnd")
                                    .structure("structureEnd")
                                    .existInVspGosbTbRegistry(false)
                                    .build();
        addressEnd = addressRepository.save(addressEnd);
        
        for (int i = 0; i < 95; i++) {
            
            List<Waypoint> waypoints = new ArrayList<>();
            waypoints.add(Waypoint.builder()
                                  .id(UUID.randomUUID())
                                  .orderingIndex(1)
                                  .address(addressStart)
                                  .checkinAutomatic(null)
                                  .build());
            waypoints.add(Waypoint.builder()
                                  .id(UUID.randomUUID())
                                  .orderingIndex(2)
                                  .address(addressEnd)
                                  .checkinAutomatic(true)
                                  .build());
            Integer balanceUnit = null;
            if (i > 1) {
                balanceUnit = i % 2 == 0 ? balanceUnitEven : balanceUnitOdd;
            }
            Request request =
                    Request.builder()
                           .id(UUID.randomUUID())
                           .changeDate(i % 2 == 0 ? changeDateEven : changeDateOdd)
                           .humanReadableId("OT-0001-" + i)
                           .passenger(i % 2 == 0 ? passengerEven : passengerOdd)
                           .transportType(TransportTypeEnum.PERSONAL.getName())
                           .creationTime(i % 2 == 0 ? creationDateEven : creationDateOdd)
                           .approvalDate(i % 2 == 0 ? approvalDateEven : approvalDateOdd)
                           .orderPaymentFormationFinishingDate(
                                   i % 2 == 0 ? orderPaymentFormationFinishingDateEven : orderPaymentFormationFinishingDateOdd)
                           .orderPaymentFormationStartDate(i % 2 == 0 ? orderPaymentFormationStartDateEven : orderPaymentFormationStartDateOdd)
                           .status(i % 2 == 0 ? statusEven : statusOdd)
                           .expected(i % 2 == 0 ? expectedDataEven : expectedDataOdd)
                           .waypoints(waypoints)
                           .purpose(i % 2 == 0 ? purposeEven : purposeOdd)
                           .desiredDate(i % 2 == 0 ? desiredDateEven : desiredDateOdd)
                           .coopTrip(i % 4 < 2)
                           .requestRating(RequestRating.builder().rating(i % 5 + 1).build())
                           .organizationId(i % 2 == 0 ? organizationEvenId : organizationOddId)
                           .balanceUnit(balanceUnit)
                           .build();
            request = requestRepository.save(request);
            Request finalRequest = request;
            waypoints.forEach(w -> w.setRequest(finalRequest));
            waypointRepository.saveAll(waypoints);
            
            if (i % 4 == 0) {
                requestEven = request;
                requestEvenId = request.getId();
            }
            if (i % 4 == 1) {
                requestOdd = request;
                requestOddId = request.getId();
                
                var sharedRideKPI = SharedRideKPI.builder()
                                                 .id(UUID.randomUUID())
                                                 .totalCost(100.0)
                                                 .totalDistanceKm(5.0)
                                                 .totalTimeMin(10)
                                                 .build();
                sharedRideKPI.getOrdersKpi().clear();
                sharedRideKPI = sharedRequestKpiRepository.save(sharedRideKPI);
                
                var orderKPIEven = OrderKpi.builder()
                                           .id(UUID.randomUUID())
                                           .kpiId(sharedRideKPI.getId())
                                           .requestId(requestEvenId)
                                           .costSharePart(0.5)
                                           .rideTimeMin(10)
                                           .savings(50.0)
                                           .savingsPct(50.0)
                                           .orderDistanceKm(5)
                                           .build();
                orderKpiRepository.saveAndFlush(orderKPIEven);
                sharedRideKPI.getOrdersKpi().add(orderKPIEven);
                
                var orderKPIOdd = OrderKpi.builder()
                                          .id(UUID.randomUUID())
                                          .kpiId(sharedRideKPI.getId())
                                          .requestId(requestOddId)
                                          .costSharePart(0.5)
                                          .rideTimeMin(10)
                                          .savings(50.0)
                                          .savingsPct(50.0)
                                          .orderDistanceKm(5)
                                          .build();
                orderKpiRepository.saveAndFlush(orderKPIOdd);
                sharedRideKPI.getOrdersKpi().add(orderKPIOdd);
                
                var sharedRide = SharedRide.builder()
                                           .id(UUID.randomUUID())
                                           .passengers(2)
                                           .tariffId(tariffId)
                                           .active(true)
                                           .kpi(sharedRideKPI)
                                           .coopTaxiTrip(null)
                                           .build();
                
                sharedRide = sharedRideRepository.save(sharedRide);
                if (i == 1) {
                    sharedRideIdForSearch = sharedRide.getId();
                }
                requestEven.setSharedRide(sharedRide);
                requestEven.setRideId(sharedRide.getId());
                requestOdd.setSharedRide(sharedRide);
                requestOdd.setRideId(sharedRide.getId());
                List<Request> requestList = new ArrayList<>();
                requestList.add(requestEven);
                requestList.add(requestOdd);
                requestRepository.saveAll(requestList);
            }
        }
    }
    
    @Order(1)
    @Test
    @DisplayName("Проверка пагинации без фильтров (ЛТ)")
    void findPersonalRequests_pagination_wo_filter() {
        
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(9);
        pageSetting.setSize(10);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        // Проверка исходных условий
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(5, result.getContent().size(), "Без фильтров на 9-й странице в тесте должно быть 5 заявок");
        assertEquals(95, result.getTotalElements(), "Без фильтров в тесте должно быть 95 заявок");
    }
    
    @Order(2)
    @Test
    @DisplayName("Проверка фильтра по humanReadableId заявки (ЛТ)")
    void findPersonalRequests_filter_on_humanreadableid() {
        // Проверка фильтра по humanreadableid заявки
        
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(20);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setRequestHumanId("OT-0001-1");
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(11, result.getContent().size(), "Поиск должен идти по like: OT-0001-1, OT-0001-10, OT-0001-11, ...OT-0001-19 - всего 11 заявок");
        assertEquals(11, result.getTotalElements(), "Поиск должен идти по like: OT-0001-1, OT-0001-10, OT-0001-11, ...OT-0001-19 - всего 11 заявок");
    }
    
    @Order(3)
    @Test
    @DisplayName("Проверка фильтра по подразделению пассажира (ЛТ)")
    void findPersonalRequests_filter_on_departments() {
        // Проверка фильтра по подразделению
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        HashSet<UUID> departments = new HashSet<>();
        departments.add(departmentEvenId);
        requestDTO.setEmployeeDepartmentSet(departments);
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с пассажирами четного подразделения");
        
        departments.clear();
        departments.add(departmentOddId);
        requestDTO.setEmployeeDepartmentSet(departments);
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявок с пассажирами нечетного подразделения");
    }
    
    @Test
    @DisplayName("Проверка фильтра по дате создания (ЛТ)")
    void findPersonalRequests_filter_on_creationDate() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setCreationDate(RequestReportDTO.DateRange.builder()
                                                             .start(creationDateEven.withHour(0).withMinute(0).withSecond(0))
                                                             .end(creationDateEven.withHour(23).withMinute(59).withSecond(59))
                                                             .build());
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с пассажирами с четной датой создания");
        
        requestDTO.setCreationDate(RequestReportDTO.DateRange.builder()
                                                             .start(creationDateOdd.withHour(0).withMinute(0).withSecond(0))
                                                             .end(creationDateOdd.withHour(23).withMinute(59).withSecond(59))
                                                             .build());
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявок с пассажирами с четной датой создания");
    }
    
    @Test
    @DisplayName("Проверка фильтра по дате согласования (ЛТ)")
    void findPersonalRequests_filter_on_approvalDate() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setApproveDate(RequestReportDTO.DateRange.builder()
                                                            .start(approvalDateEven.withHour(0).withMinute(0).withSecond(0))
                                                            .end(approvalDateEven.withHour(23).withMinute(59).withSecond(59))
                                                            .build());
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с пассажирами с четной датой согласования");
        
        requestDTO.setApproveDate(RequestReportDTO.DateRange.builder()
                                                            .start(approvalDateOdd.withHour(0).withMinute(0).withSecond(0))
                                                            .end(approvalDateOdd.withHour(23).withMinute(59).withSecond(59))
                                                            .build());
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявок с пассажирами с четной датой согласования");
    }
    
    @Test
    @DisplayName("Проверка фильтра по дате начала формирования приказа на выплату (ЛТ)")
    void findPersonalRequests_filter_on_orderPaymentFormationStartDate() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setOrderPaymentFormationStartRange(RequestReportDTO.DateRange.builder()
                                                                                .start(orderPaymentFormationStartDateEven.withHour(0).withMinute(0)
                                                                                                                         .withSecond(0))
                                                                                .end(orderPaymentFormationStartDateEven.withHour(23).withMinute(59)
                                                                                                                       .withSecond(59))
                                                                                .build());
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с пассажирами с четной датой начала формирования приказа на " +
                                                     "выплату");
        
        requestDTO.setOrderPaymentFormationStartRange(RequestReportDTO.DateRange.builder()
                                                                                .start(orderPaymentFormationStartDateOdd.withHour(0).withMinute(0)
                                                                                                                        .withSecond(0))
                                                                                .end(orderPaymentFormationStartDateOdd.withHour(23).withMinute(59)
                                                                                                                      .withSecond(59))
                                                                                .build());
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявок с пассажирами с четной датой начала формирования приказа на " +
                                                     "выплату");
    }
    
    @Test
    @DisplayName("Проверка фильтра по дате окончания формирования приказа на выплату (ЛТ)")
    void findPersonalRequests_filter_on_orderPaymentFormationFinishingDate() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setOrderPaymentFormationFinishingDate(RequestReportDTO.DateRange.builder()
                                                                                   .start(orderPaymentFormationFinishingDateEven.withHour(0)
                                                                                                                                .withMinute(0)
                                                                                                                                .withSecond(0))
                                                                                   .end(orderPaymentFormationFinishingDateEven.withHour(23)
                                                                                                                              .withMinute(59)
                                                                                                                              .withSecond(59))
                                                                                   .build());
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с пассажирами с четной датой окончания формирования приказа на " +
                                                     "выплату");
        
        requestDTO.setOrderPaymentFormationFinishingDate(RequestReportDTO.DateRange.builder()
                                                                                   .start(orderPaymentFormationFinishingDateOdd.withHour(0)
                                                                                                                               .withMinute(0)
                                                                                                                               .withSecond(0))
                                                                                   .end(orderPaymentFormationFinishingDateOdd.withHour(23)
                                                                                                                             .withMinute(59)
                                                                                                                             .withSecond(59))
                                                                                   .build());
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявок с пассажирами с четной датой окончания формирования приказа на " +
                                                     "выплату");
    }
    
    @Test
    @DisplayName("Проверка фильтра на статус (ЛТ)")
    void findPersonalRequests_filter_on_status() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        HashSet<TripRequestStatus> tripRequestStatuses = new HashSet<>();
        tripRequestStatuses.add(TripRequestStatus.valueOf(statusEven));
        requestDTO.setRequestStatusSet(tripRequestStatuses);
        
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок (четные) с со статусом " + statusEven);
    }
    
    @Test
    @DisplayName("Проверка фильтра на расстояние (ЛТ)")
    void findPersonalRequests_filter_on_expectedDistance() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        RequestReportDTO.DoubleRange expectedDistance = new RequestReportDTO.DoubleRange();
        expectedDistance.setStart(4.0);
        expectedDistance.setEnd(6.0);
        requestDTO.setExpectedDistance(expectedDistance);
        
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок (с расстоянием = 5km)");
    }
    
    @Test
    @DisplayName("Проверка фильтра по цели (ЛТ)")
    void findPersonalRequests_filter_on_purpose() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        var purposes = purposeRepository.findByPurpose("purposeEven");
        assertTrue(!purposes.isEmpty());
        var purposeEven = purposes.get(0);
        
        HashSet<TripPurposeDTO> purposeSet = new HashSet<>();
        purposeSet.add(TripPurposeDTO.builder()
                                     .id(purposeEven.getId())
                                     .purpose(purposeEven.getPurpose())
                                     .build());
        requestDTO.setPurposeSet(purposeSet);
        
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с целью purposeEven");
    }
    
    @Test
    @DisplayName("Проверка фильтра по желаемой дате поездки (ЛТ)")
    void findPersonalRequests_filter_on_desiredDate() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setDesiredDateRange(
                RequestReportDTO.DateRange.builder()
                                          .start(desiredDateEven.withHour(0).withMinute(0).withSecond(0))
                                          .end(desiredDateEven.withHour(23).withMinute(59).withSecond(59))
                                          .build());
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с желаемой датой поездки = " + desiredDateEven);
        
        requestDTO.setDesiredDateRange(
                RequestReportDTO.DateRange.builder()
                                          .start(desiredDateOdd.withHour(0).withMinute(0).withSecond(0))
                                          .end(desiredDateOdd.withHour(23).withMinute(59).withSecond(59))
                                          .build());
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявок с желаемой датой поездки = " + desiredDateOdd);
    }
    
    @Test
    @DisplayName("Проверка фильтра по признаку совместной поездки (ЛТ)")
    void findPersonalRequests_filter_on_coopTrip() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setCoopTrip(true);
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявок с coopTrip = true");
    }
    
    @Test
    @DisplayName("Проверка фильтра по sharedRide (ЛТ)")
    void findPersonalRequests_filter_on_sharedRide() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setSharedRideId(sharedRideIdForSearch);
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(2, result.getContent().size(), "Всего должно быть 2 заявки с sharedRideId = " + sharedRideIdForSearch);
    }
    
    @Test
    @DisplayName("Проверка фильтра по рейтингу (ЛТ)")
    void findPersonalRequests_filter_on_rating() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        HashSet<Integer> ratingMarkSet = new HashSet<>();
        ratingMarkSet.add(5);
        ratingMarkSet.add(4);
        requestDTO.setRatingMarkSet(ratingMarkSet);
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(38, result.getContent().size(), "Всего должно быть 38 заявки с оценкой 4 или 5");
    }
    
    @Test
    @DisplayName("Проверка фильтра по организации (ЛТ)")
    void findPersonalRequests_filter_on_organization() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        requestDTO.setOrganizationId(organizationEvenId);
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(48, result.getContent().size(), "Всего должно быть 48 заявки с organizationEvenId = " + organizationEvenId);
    }
    
    @Test
    @DisplayName("Проверка фильтра по балансовой единице (ЛТ)")
    void findPersonalRequests_filter_on_balanceUnit() {
        beforeEach();
        
        RequestReportDTO.PageSetting pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(999);
        
        RequestForPersonalReportDTO requestDTO = new RequestForPersonalReportDTO();
        requestDTO.setPageSetting(pageSetting);
        
        HashSet<Integer> balanceUnitSet = new HashSet<>();
        balanceUnitSet.add(1300);
        balanceUnitSet.add(9900);
        requestDTO.setBalanceUnitSet(balanceUnitSet);
        
        var result = requestService.findPersonalRequests(requestDTO);
        assertEquals(47, result.getContent().size(), "Всего должно быть 47 заявки с фильтром balanceUnit: [1300, 9900]");
        
        balanceUnitSet.clear();
        balanceUnitSet.add(4400);
        balanceUnitSet.add(9900);
        requestDTO.setBalanceUnitSet(balanceUnitSet);
        
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(46, result.getContent().size(), "Всего должно быть 46 заявки с фильтром balanceUnit: [4400, 9900]");
        
        balanceUnitSet.clear();
        balanceUnitSet.add(1300);
        balanceUnitSet.add(4400);
        balanceUnitSet.add(9900);
        requestDTO.setBalanceUnitSet(balanceUnitSet);
        
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(93, result.getContent().size(), "Всего должно быть 93 заявок с фильтром balanceUnit: [1300, 4400, 9900]");
        
        balanceUnitSet.clear();
        requestDTO.setBalanceUnitSet(balanceUnitSet);
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(95, result.getContent().size(), "Всего должно быть 95 заявок с фильтром balanceUnit: []");
        
        requestDTO.setBalanceUnitSet(null);
        result = requestService.findPersonalRequests(requestDTO);
        assertEquals(95, result.getContent().size(), "Всего должно быть 95 заявок без фильтра по balanceUnit");
        
    }
    
    @Test
    @DisplayName("Нарушении КС подачи ТС отображается как 'Да'")
    void deadlineViolation_test() {
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.TAXI.name())
                                 .deadlineState(DeadlineState.RED)
                                 .humanReadableId("OT-TT-00001234")
                                 .build();
        
        requestRepository.save(request);
        
        RequestForTaxiReportDTO requestDTO = new RequestForTaxiReportDTO();
        requestDTO.setRequestHumanId("OT-TT-00001234");
        var result = requestService.findTaxiRequests(requestDTO);
        
        assertEquals("Да", result.getContent().get(0).getDeadlineViolation(), "При нарушении КС необходимо показывать слово 'Да'");
    }
    
    @Test
    @DisplayName("Успешная выборка, если заявок меньше, чем 50000")
    void findTaxiRequests() {
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < 49000; i++) {
            Request request =
                    Request.builder()
                           .id(UUID.randomUUID())
                           .changeDate(changeDateEven)
                           .humanReadableId("OT-0002-" + i)
                           .transportType(TransportTypeEnum.TAXI.getName())
                           .creationTime(LocalDateTime.now())
                           .approvalDate(LocalDateTime.now())
                           .orderPaymentFormationFinishingDate(orderPaymentFormationFinishingDateOdd)
                           .orderPaymentFormationStartDate(orderPaymentFormationStartDateOdd)
                           .status(statusOdd)
                           .desiredDate(LocalDateTime.now())
                           .requestRating(RequestRating.builder().rating(i % 5 + 1).build())
                           .organizationId(organizationOddId)
                           .build();
            requests.add(request);
        }
        
        requestRepository.saveAllAndFlush(requests);
        
        var filters = RequestForTaxiReportDTO.builder()
                                             .organizationId(organizationOddId)
                                             .build();
        
        assertDoesNotThrow(() -> requestService.findTaxiRequests(filters));
    }
}