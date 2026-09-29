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
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.PersonalCarMessage;
import ru.sberbank.ditsib.transport.reports.dao.PersonalCarRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedTest;
import ru.sberbank.ditsib.transport.reports.model.PersonalCar;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка получения данных автомобилей личного транспорта")
@MockitoBean(types = JwtDecoder.class)
class PersonalCarListenerTest extends SharedTest {
    @Autowired
    private Consumer<Message<PersonalCarMessage>> input;
    @Autowired
    private PersonalCarRepository personalCarRepository;
    
    @Test
    @DisplayName("Новый автомобиль")
    void handlePersonalCar_new() {
        personalCarRepository.deleteAllInBatch();
        
        var id = UUID.randomUUID();
        var message = PersonalCarMessage.builder()
                                        .id(id)
                                        .brandName("Жигули")
                                        .employeeId(testEmployee1.getId())
                                        .build();
        
        assertEquals(0, personalCarRepository.count());
        
        input.accept(MessageBuilder.withPayload(message).build());
        
        PersonalCar personalCar = personalCarRepository.findAll().get(0);
        assertEquals(1, personalCarRepository.count());
        assertEquals(id, personalCar.getId());
        assertEquals(message.getBrandName(), personalCar.getBrandName());
        assertEquals(testEmployee1.getId(), personalCar.getEmployee().getId());
    }
}
