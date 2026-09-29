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
import ru.sberbank.ditsib.transport.messaging.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.reports.dao.ContractorRepository;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка получения контракторов")
@MockitoBean(types = JwtDecoder.class)
public class ContractorListenerTest extends KafkaTest {
    
    @Autowired
    private Consumer<Message<ContractorMessage>> contractorInput;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Test
    @DisplayName("Новый контрактор")
    void handleContractor_new() {
        var id = UUID.randomUUID();
        var message = ContractorMessage.builder()
                                       .id(id)
                                       .name("Контрактор 1")
                                       .deleted(false)
                                       .build();
        
        assertEquals(0, contractorRepository.count());
        
        contractorInput.accept(MessageBuilder.withPayload(message).build());
        
        assertEquals(1, contractorRepository.count());
        assertEquals(id, contractorRepository.findAll().get(0).getId());
    }
    
    @Test
    @DisplayName("Обновление контрактора")
    void handleContractor_update() {
        var id = UUID.randomUUID();
        var message = ContractorMessage.builder()
                                       .id(id)
                                       .name("Контрактор 1")
                                       .deleted(false)
                                       .build();
        
        assertEquals(0, contractorRepository.count());
        
        contractorInput.accept(MessageBuilder.withPayload(message).build());
        
        assertEquals(1, contractorRepository.count());
        assertEquals(id, contractorRepository.findAll().get(0).getId());
        assertEquals(message.getName(), contractorRepository.findAll().get(0).getName());
        
        message.setName("Контрактор 2");
        contractorInput.accept(MessageBuilder.withPayload(message).build());
        assertEquals(1, contractorRepository.count());
        assertEquals(id, contractorRepository.findAll().get(0).getId());
        assertEquals(message.getName(), contractorRepository.findAll().get(0).getName());
    }
}
