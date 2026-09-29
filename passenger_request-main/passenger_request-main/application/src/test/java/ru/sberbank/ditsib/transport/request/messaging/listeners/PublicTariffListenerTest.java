package ru.sberbank.ditsib.transport.request.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff.messaging.PublicTariffMessage;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.PublicTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.PublicTariff;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Тест слушателя Тарифов общественного транспорта")
@Transactional
class PublicTariffListenerTest extends KafkaTest {
    
    private final UUID PUBLIC_ID = TransportTypeEnum.PUBLIC.getId();
    private final String REGION1 = "Region1";
    private final String REGION2 = "Region2";
    private final UUID TARIFF1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-aaaaaaaaaaa1");
    private final UUID TARIFF2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaaaaaa2");
    
    @Autowired
    private PublicTariffRepository tariffRepository;
    
    @Autowired
    private Consumer<Message<PublicTariffMessage>> publicTariffInput;
    
    @AfterEach
    void clear() {
        tariffRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Создание тарифа - успех")
    void handleTest_createTariffs() {
        //сгенерируем новое сообщение о тарифе
        var message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, false);
        publicTariffInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(tariffRepository.count()).isEqualTo(1);
        var tariff = tariffRepository.findAll().getFirst();
        checkTariff(tariff, message);
        
        //сгенерируем еще одно сообщение
        message = createTariffMessage(TARIFF2_ID, REGION2, PUBLIC_ID, false);
        publicTariffInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(tariffRepository.count()).isEqualTo(2);
        tariff = tariffRepository.findAll().stream().filter(t -> t.getRegion().equals(REGION2)).findFirst().orElse(null);
        checkTariff(tariff, message);
    }
    
    @Test
    @DisplayName("Редактирование тарифа - успех")
    void handleTest_updateTariff() {
        //сгенерируем новое сообщение о тарифе
        var message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, false);
        publicTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        
        //сгенерируем сообщение на коррекцию тарифа
        message = createTariffMessage(TARIFF1_ID, REGION2, message.humanReadableId(), PUBLIC_ID, false);
        publicTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        checkTariff(tariffRepository.findAll().get(0), message);
    }
    
    @Test
    @DisplayName("Удаление тарифа - успех")
    void handleTest_deleteTariff() {
        //сгенерируем новое сообщение о тарифе
        var message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, false);
        publicTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        
        //сгенерируем сообщение на удаление тарифа
        message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, true);
        publicTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(0);
    }

    private PublicTariffMessage createTariffMessage(
            UUID tariffId, String region, UUID transportTypeId, boolean deleted
                                                   ) {
        return createTariffMessage(tariffId, region, null, transportTypeId, deleted);
    }
    
    private PublicTariffMessage createTariffMessage(
            UUID tariffId, String region, String humanReadableId,
            UUID transportTypeId, boolean deleted
                                                   ) {
        return new PublicTariffMessage(tariffId, humanReadableId == null
                                                 ? "TF-" + (int) (Math.random() * 1000) + "-" + (int) (Math.random() * 10)
                                                 : humanReadableId, UUID.randomUUID(), TransportServiceType.EMPLOYEE_TRANSPORTATION.name(),
                                       UUID.randomUUID(), region, UUID.randomUUID(), TransportTypeEnum.fromId(transportTypeId).get().getName(),
                                       !deleted, 2, 2, 2, 2,
                                       2, true, true, true, true,
                                       true, 2, 2, 2,
                                       2, 2, 2, true,
                                       true, true, true,
                                       true, true, deleted);
    }
    
    private void checkTariff(PublicTariff tariff, PublicTariffMessage message) {
        assertThat(tariff.getId()).isEqualTo(message.getId());
        assertThat(tariff.getHumanReadableId()).isEqualTo(message.humanReadableId());
        assertThat(tariff.getRegion()).isEqualTo(message.region());
        assertThat(tariff.getTransportType().getName()).isEqualTo(message.transportType());
    }
}