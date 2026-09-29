package ru.sberbank.ditsib.transport.request.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff.messaging.PersonalTariffMessage;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.PersonalTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Тест слушателя Тарифов личного транспорта")
@Transactional
class PersonalTariffListenerTest extends KafkaTest {
    
    private final UUID PUBLIC_ID = TransportTypeEnum.PUBLIC.getId();
    private final String REGION1 = "Region1";
    private final String REGION2 = "Region2";
    private final UUID CONTRACT1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-bbbbbbbbbbb1");
    private final UUID CONTRACT2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb2");
    private final UUID TARIFF1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-aaaaaaaaaaa1");
    private final UUID TARIFF2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaaaaaa2");
    
    @Autowired
    private PersonalTariffRepository tariffRepository;
    
    @Autowired
    @Qualifier("personalTariffInput")
    private Consumer<Message<PersonalTariffMessage>> personalTariffInput;
    
    @AfterEach
    void clear() {
        tariffRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Создание тарифа - успех")
    void handleTest_createTariffs() {
        //сгенерируем новое сообщение о тарифе
        var message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, CONTRACT1_ID, false);
        personalTariffInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(tariffRepository.count()).isEqualTo(1);
        var tariff = tariffRepository.findAll().getFirst();
        checkTariff(tariff, message);
        
        //сгенерируем еще одно сообщение
        message = createTariffMessage(TARIFF2_ID, REGION2, PUBLIC_ID, CONTRACT2_ID, false);
        personalTariffInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(tariffRepository.count()).isEqualTo(2);
        var regionId = message.regionId();
        tariff = tariffRepository.findAll().stream()
                                 .filter(t -> t.getRegionId().equals(regionId))
                                 .findFirst()
                                 .orElse(null);
        checkTariff(tariff, message);
    }
    
    @Test
    @DisplayName("Редактирование тарифа - успех")
    void handleTest_updateTariff() {
        //сгенерируем новое сообщение о тарифе
        var message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, CONTRACT1_ID, false);
        personalTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        
        //сгенерируем сообщение на коррекцию тарифа
        message = createTariffMessage(TARIFF1_ID, REGION2, message.humanReadableId(),
                                      PUBLIC_ID, CONTRACT1_ID, false);
        personalTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        checkTariff(tariffRepository.findAll().get(0), message);
    }
    
    @Test
    @DisplayName("Удаление тарифа - успех")
    void handleTest_deleteTariff() {
        //сгенерируем новое сообщение о тарифе
        var message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, CONTRACT1_ID, false);
        personalTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(1);
        
        //сгенерируем сообщение на удаление тарифа
        message = createTariffMessage(TARIFF1_ID, REGION1, PUBLIC_ID, CONTRACT1_ID, true);
        personalTariffInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tariffRepository.count()).isEqualTo(0);
    }

    private PersonalTariffMessage createTariffMessage(
            UUID tariffId, String region, UUID transportTypeId, UUID contractId,
            boolean deleted
                                                     ) {
        return createTariffMessage(tariffId, region, null, transportTypeId, contractId, deleted);
    }
    
    private PersonalTariffMessage createTariffMessage(
            UUID tariffId, String region, String humanReadableId,
            UUID transportTypeId, UUID contractId, boolean deleted
                                                     ) {
        return Instancio.of(PersonalTariffMessage.class)
                .set(Select.field(PersonalTariffMessage::id), tariffId)
                .set(Select.field(PersonalTariffMessage::active), !deleted)
                .set(Select.field(PersonalTariffMessage::deleted), deleted)
                .set(Select.field(PersonalTariffMessage::rideCostPerMin), 2)
                .set(Select.field(PersonalTariffMessage::minRideDistanceCost), 2)
                .set(Select.field(PersonalTariffMessage::waitCostPerMin), 2)
                .set(Select.field(PersonalTariffMessage::waitCostPerMinIntermediate), 2)
                .set(Select.field(PersonalTariffMessage::timeIncluded), 59)
                .set(Select.field(PersonalTariffMessage::distanceIncluded), 99)
                .set(Select.field(PersonalTariffMessage::coefEngine1_6), 2)
                .set(Select.field(PersonalTariffMessage::coefEngine2_0_to_2_5), 2)
                .set(Select.field(PersonalTariffMessage::seasonalCoefficient), 2)
                .set(Select.field(PersonalTariffMessage::coefTraffic), 2)
                .set(Select.field(PersonalTariffMessage::coefMaterialAssets), 2)
                .set(Select.field(PersonalTariffMessage::seasonStart), LocalDate.now())
                .set(Select.field(PersonalTariffMessage::seasonEnd), LocalDate.now().plusDays(1))
                .set(Select.field(PersonalTariffMessage::transportType), TransportTypeEnum.fromId(transportTypeId).get().getName())
                .set(Select.field(PersonalTariffMessage::serviceType), TransportServiceType.EMPLOYEE_TRANSPORTATION.name())
                .set(Select.field(PersonalTariffMessage::humanReadableId), humanReadableId == null
                                                                           ? "TF-" + (int) (Math.random() * 1000) + "-" + (int) (Math.random() * 10)
                                                                           : humanReadableId)
                .create();
    }
    
    private void checkTariff(PersonalTariff tariff, PersonalTariffMessage message) {
        assertThat(tariff.getId()).isEqualTo(message.getId());
        assertThat(tariff.getHumanReadableId()).isEqualTo(message.humanReadableId());
//        assertThat(tariff.getRegion()).isEqualTo(message.region());
        assertThat(tariff.getTransportType().getName()).isEqualTo(message.transportType());
    }
}