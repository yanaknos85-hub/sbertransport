package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.files.PersonalPaymentDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.PaymentData;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@AutoConfigureMockMvc
@DisplayName("Проверка формирования данных для Excel-файла по ЛТ выплата")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
public class PersonalPaymentReportResolverTest extends KafkaTest {
    
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
    DataExporter<PersonalPaymentDTO> dataResolver;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("Выгрузка реестра заявок по ЛТ (выплата)")
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
        
        var filters = RequestForPersonalReportDTO.builder()
                                                 .organizationId(organizationId)
                                                 .orderPaymentFormationStartRange(
                                                         RequestReportDTO.DateRange.builder()
                                                                                   .start(creationDateTime.minusDays(3))
                                                                                   .end(creationDateTime.plusDays(3))
                                                                                   .build()
                                                                                 )
                                                 .build();
        parameters.put("filters",
                       new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8))));
        
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .organizationId(organizationId)
                                 .creationTime(creationDateTime)
                                 .orderPaymentFormationStartDate(creationDateTime)
                                 .changeDate(changeDateTime)
                                 .status(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name())
                                 .timeZone("GMT+04")
                                 .tariff(tariff)
                                 .employeeDriverId(driver.getId())
                                 .passenger(driver)
                                 .sharedRideOwner(true)
                                 .coopTrip(true)
                                 .paymentData(PaymentData.builder().paymentPriceMain(100L).paymentTypeCodeMain(PaymentTypeCode.CODE_4661.getCode())
                                                         .build())
                                 .build();
        requestRepository.save(request);
        
        List<PersonalPaymentDTO> result = cut.exportData(parameters, new FakeAuthentication());
        
        assertEquals(1, result.size());
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
