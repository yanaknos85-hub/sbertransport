package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.dto.files.PersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.RequestRating;
import ru.sberbank.ditsib.transport.reports.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@AutoConfigureMockMvc
@DisplayName("Проверка формирования данных для Excel-файла по ЛТ")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class PersonalReportResolverTest extends KafkaTest {
    
    @Autowired
    PersonalTariffRepository personalTariffRepository;
    
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
    
    @Autowired
    DataExporter<PersonalReportDTO> dataResolver;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("Выгрузка реестра заявок по ЛТ")
    @Disabled("Требуется актуализация")
    void exportData() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .sharedRideOwner(true)
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        
        assertEquals(result.get(0).getStatusCode(), "201 - Отмена пользователем", "В Excel-файле код статуса нужно выводить с расшифровкой");
        
        assertEquals("06.08.2023 17:00:00", result.get(0).getPaymentDate(), "Время установки конечного статуса должно быть уже с учетом тайм-зоны");
        
        assertEquals(tariffHumareadableid, result.get(0).getTariff(), "В Excel-файле нужно отображать номер тарифа");
        
        assertEquals("Иванов Петр Васильевич", result.get(0).getDriverFIO(), "В Excel-файле нужно отображать ФИО водителя");
        
        assertEquals("Водитель", result.get(0).getDriverOrPassenger(), "Признак Водитель/Пассажир для инициатора заявки должен быть = Водитель");
    }
    
    @Test
    @DisplayName("Индивидуальная поездка. Признак Водитель/Пассажир всегда = Водитель")
    void driver_not_coop_trip_shared_rideowner_is_false() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .coopTrip(false)
                                 .sharedRideOwner(false)
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        assertEquals("Водитель", result.get(0).getDriverOrPassenger(),
                     "Признак Водитель/Пассажир для индивидуальной заявки при sharedRideOwner = false должен быть = Водитель");
    }
    
    @Test
    @DisplayName("Индивидуальная поездка. Признак Водитель/Пассажир всегда = Водитель")
    void driver_not_coop_trip_shared_rideowner_is_null() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .coopTrip(false)
                                 .sharedRideOwner(null)
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        assertEquals("Водитель", result.get(0).getDriverOrPassenger(),
                     "Признак Водитель/Пассажир для индивидуальной заявки при sharedRideOwner = null должен быть = Водитель");
    }
    
    @Test
    @DisplayName("Индивидуальная поездка. Признак Водитель/Пассажир всегда = Водитель")
    void driver_not_coop_trip_shared_rideowner_is_true() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .coopTrip(false)
                                 .sharedRideOwner(false)
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        assertEquals("Водитель", result.get(0).getDriverOrPassenger(),
                     "Признак Водитель/Пассажир для индивидуальной заявки при sharedRideOwner = true должен быть = Водитель");
    }
    
    @Test
    @DisplayName("Совместная поездка. Признак Водитель/Пассажир для инициатора поездки = Водитель")
    void driver_coop_trip_shared_rideowner_is_true() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .coopTrip(true)
                                 .sharedRideOwner(true)
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        assertEquals("Водитель", result.get(0).getDriverOrPassenger(),
                     "Признак Водитель/Пассажир для совместной поездки при sharedRideOwner = true должен быть = Водитель");
    }
    
    @Test
    @DisplayName("Совместная поездка. Признак Водитель/Пассажир для не-инициатора поездки = Пассажир")
    void driver_coop_trip_shared_rideowner_is_false() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .coopTrip(true)
                                 .sharedRideOwner(false)
                                 .requestRating(RequestRating.builder().rating(0).build())
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        assertEquals("Пассажир", result.get(0).getDriverOrPassenger(),
                     "Признак Водитель/Пассажир для совместной поездки при sharedRideOwner = false должен быть = Пассажир");
    }
    
    @Test
    @DisplayName("Экономия")
    void economy() throws JsonProcessingException {
        var cut = dataResolver;
        
        String tariffHumareadableid = "TF-0001-00001234";
        PersonalTariff tariff = PersonalTariff
                .builder()
                .id(UUID.randomUUID())
                .humanReadableId(tariffHumareadableid)
                .build();
        tariff = personalTariffRepository.save(tariff);
        
        
        Employee driver = Employee
                .builder()
                .id(UUID.randomUUID())
                .lastName("Иванов")
                .firstName("Петр")
                .patronymic("Васильевич")
                .build();
        driver = employeeRepository.save(driver);
        
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 8, 6, 12, 0, 0);
        LocalDateTime changeDateTime = LocalDateTime.of(2023, 8, 6, 13, 0, 0);
        
        UUID organizationId = UUID.randomUUID();
        
        Map<String, Object> parameters = new HashMap<>();
        var start = LocalDateTime.parse("2023-08-01T00:00:00");
        var end = LocalDateTime.parse("2023-08-06T23:59:59");
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .orderPaymentFormationStartRange(dateRange)
                                                 .organizationId(organizationId)
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .statusCode(201)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_PAYMENT_DONE.name())
                                 .passengerCount(2)
                                 .expected(ExpectedData.builder().cost(25000.0).build())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .coopTrip(true)
                                 .sharedRideOwner(false)
                                 .build();
        requestRepository.save(request);
        
        List<PersonalReportDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
        assertThat(result.get(0).getFinancialImpact()).isNotNull();
        assertThat(result.get(0).getFinancialImpactJoined()).isNotNull();
    }
    
    private static class FakeAuthentication extends JwtAuthenticationToken {
        
        public FakeAuthentication() {
            super(Jwt.withTokenValue("token").header("algo", "none").jti(UUID.randomUUID().toString()).build());
        }
        
        @Override
        public Object getCredentials() {
            return null;
        }
        
        @Override
        public Object getDetails() {
            return null;
        }
        
        @Override
        public Object getPrincipal() {
            return null;
        }
        
        @Override
        public boolean isAuthenticated() {
            return false;
        }
        
        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        
        }
        
        @Override
        public String getName() {
            return UUID.randomUUID().toString();
        }
    }
}