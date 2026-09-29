package ru.sberbank.ditsib.transport.reports.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.reports.dao.RequestForCargoRepository;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.Organization;
import ru.sberbank.ditsib.transport.reports.model.cargo.CargoDetail;
import ru.sberbank.ditsib.transport.reports.model.cargo.RequestForCargo;
import ru.sberbank.ditsib.transport.reports.service.impl.ReportPdfTtnServiceImpl;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sberbank.ditsib.transport.reports.service.impl.ReportPdfTtnServiceImpl.*;

@SuppressWarnings({ "SpringJavaInjectionPointsAutowiringInspection" })
@Slf4j
@AutoConfigureMockMvc
@DisplayName("Проверка выгрузки отчета ттн")
@EmbeddedPostgres
@SpringBootTest
@Transactional
@MockitoBean(types = JwtDecoder.class)
class ReportPdfTtnServiceTest extends KafkaTest {
    
    @Autowired
    private RequestForCargoRepository requestForCargoRepository;
    
    private RequestForCargo request;
    @Autowired
    private ReportPdfTtnServiceImpl service;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private OrganizationService organizationService;
    
    @Test
    @DisplayName("Поиск и формирование мапы параметров")
    void getTtnParametersTest() {
        var id = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        var org = Organization.builder().id(id2).address("ADDRESS").officialName("OFF_NAME").build();
        
        organizationService.save(org);
        var recipient = employeeService.save(Employee.builder().id(UUID.randomUUID())
                                                     .firstName("Ivan").lastName("Ivanovich")
                                                     .mobilePhone("89101111111")
                                                     .organization(org).build());
        var sender = employeeService.save(Employee.builder().id(UUID.randomUUID())
                                                  .firstName("Petr").lastName("Ivanovich")
                                                  .mobilePhone("89102222222")
                                                  .organization(org).build());
        var detail = CargoDetail.builder()
                                .id(id3)
                                .cargoName("nameCargo")
                                .cargoCategory("CAT")
                                .cargoType("TYPE").build();
        request = RequestForCargo.builder().id(id).active(true)
                                 .comment("Перевозка груза - 2 грузчика")
                                 .desiredDate(LocalDateTime.of(2022, 03, 01, 12, 12))
                                 .height(12.)
                                 .weight(34.)
                                 .width(45.)
                                 .length(34.)
                                 .volume(18360.)
                                 .humanReadableId("GFGFGFG")
                                 .occupiedPlacesCount(12)
                                 .cargoDetails(Collections.singletonList(detail))
                                 .recipient(recipient)
                                 .sender(sender)
                                 .senderOrganization("OrganisationSend")
                                 .recipientOrganization("OrganisationRecipient")
                                 .transportType("DEDICATED")
                                 .build();
        requestForCargoRepository.save(request);
        
        var result = service.fillParameters(id);
        assertEquals(result.get(NAME_CARGO), "0) CAT TYPE nameCargo;");
        assertEquals(result.size(), 10);
        assertEquals(result.get(NUMBER_CARGO), "12");
        assertEquals(result.get(SENDER), "Ivanovich Petr  89102222222");
        assertEquals(result.get(RECIPIENT), "Ivanovich Ivan  89101111111");
        assertEquals(result.get(RECIPIENT_ORG), "OrganisationRecipient ADDRESS");
        assertEquals(result.get(SENDER_ORG), "OrganisationSend ADDRESS");
        assertEquals(result.get(COMMENT), "Перевозка груза - 2 грузчика");
        assertEquals(result.get(WEIGHT_CARGO), "%dкг  ((%.2fм %.2fм %.2fм )%.2fм3".formatted(34, 0.34, 0.45, 0.12, 0.02));
        assertEquals(result.get(ORDER_DATE), "01.03.2022");
        assertEquals(result.get(ORDER_NUMBER), "GFGFGFG");
    }
}
