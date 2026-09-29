package ru.sberbank.ditsib.transport.reports.messaging;

import org.junit.jupiter.api.AfterEach;
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
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.ContractMessage;
import ru.sberbank.ditsib.transport.reports.dao.ContractRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения контрактов")
@Transactional
@MockitoBean(types = JwtDecoder.class)
public class ContractListenerTest extends KafkaTest {
    
    @Autowired
    private Consumer<Message<ContractMessage>> contractInput;
    
    @Autowired
    private ContractRepository contractRepository;
    
    @AfterEach
    void dropRepository() {
        contractRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Новый контракт")
    void handleContract_new() {
        var id = UUID.randomUUID();
        var message = ContractMessage.builder()
                                     .id(id)
                                     .contractorId(UUID.randomUUID())
                                     .active(true)
                                     .deleted(false)
                                     .uvhd("test")
                                     .contractNumber("test")
                                     .includeVat(true)
                                     .sum(1000L)
                                     .endDate(LocalDate.now())
                                     .startDate(LocalDate.now().minusDays(2))
                                     .transportType(TransportTypeEnum.TAXI.name())
                                     .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION.name())
                                     .creationTime(LocalDateTime.now().minusDays(2))
                                     .active(true)
                                     .userId(UUID.randomUUID())
                                     .region("test")
                                     .build();
        
        assertEquals(0, contractRepository.count());
        
        contractInput.accept(MessageBuilder.withPayload(message).build());
        
        assertEquals(1, contractRepository.count());
        assertEquals(id, contractRepository.findAll().get(0).getId());
    }
    
    @Test
    @DisplayName("Обновление контракта")
    void handleContract_update() {
        var id = UUID.randomUUID();
        var message = ContractMessage.builder()
                                     .id(id)
                                     .contractorId(UUID.randomUUID())
                                     .active(true)
                                     .deleted(false)
                                     .uvhd("test")
                                     .contractNumber("test")
                                     .includeVat(true)
                                     .sum(1000L)
                                     .endDate(LocalDate.now())
                                     .startDate(LocalDate.now().minusDays(2))
                                     .transportType(TransportTypeEnum.TAXI.name())
                                     .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION.name())
                                     .creationTime(LocalDateTime.now().minusDays(2))
                                     .active(true)
                                     .userId(UUID.randomUUID())
                                     .region("test")
                                     .build();
        
        assertEquals(0, contractRepository.count());
        
        contractInput.accept(MessageBuilder.withPayload(message).build());
        
        assertEquals(1, contractRepository.count());
        assertEquals(id, contractRepository.findAll().get(0).getId());
        assertEquals(message.isActive(), contractRepository.findAll().get(0).getActive());
        
        message.setActive(false);
        
        contractInput.accept(MessageBuilder.withPayload(message).build());
        assertEquals(1, contractRepository.count());
        assertEquals(id, contractRepository.findAll().get(0).getId());
        assertEquals(message.isActive(), contractRepository.findAll().get(0).getActive());
    }
}
