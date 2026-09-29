package ru.sberbank.ditsib.transport.reports.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.reports.dao.EmployeeRepository;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения сотдрудников")
@Transactional
@MockitoBean(types = JwtDecoder.class)
public class EmployeeListenerTest extends KafkaTest {
    
    @Autowired
    private Consumer<Message<EmployeeMessage>> employeeInput;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Test
    @DisplayName("Новый Сотрудник")
    void handleEmployee_new() {
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder()
                .id(id)
                .firstName("Иванов")
                .lastName("Иван")
                .personnelNumber("PN00098")
                .departmentId(UUID.randomUUID())
                .email("Email@mail.ma")
                .humanReadableId("HRI 32")
                .marriageCertificateNumber("1-MH №509534")
                .costCenter("NJD46344")
                .mobilePhone("+79134323343")
                .organizationId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .itinerantType("PARTIAL").build();
        
        employeeInput.accept(MessageBuilder.withPayload(message).build());

        var savedEmployee = employeeRepository.findById(id).orElseThrow();

        assertEquals(savedEmployee.getFirstName(), message.getFirstName());
        assertEquals(savedEmployee.getLastName(), message.getLastName());
        assertEquals(savedEmployee.getPersonnelNumber(), message.getPersonnelNumber());
        assertEquals(savedEmployee.getHumanReadableId(), message.getHumanReadableId());
        assertEquals(savedEmployee.getOrganization().getId(), message.getOrganizationId());
        assertEquals(savedEmployee.getItinerantType().name(), message.getItinerantType());
        assertEquals(savedEmployee.getMobilePhone(), message.getMobilePhone());
        assertEquals(savedEmployee.getPosition().getId(), message.getPositionId());

    }
}
