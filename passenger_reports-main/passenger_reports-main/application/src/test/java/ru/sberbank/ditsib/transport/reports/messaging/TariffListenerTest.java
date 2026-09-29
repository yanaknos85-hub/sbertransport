package ru.sberbank.ditsib.transport.reports.messaging;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.dao.PublicTariffRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedTest;
import ru.sberbank.ditsib.transport.reports.model.tariff.PublicTariff;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка получения тарифа из кафки")
@Disabled
public class TariffListenerTest extends SharedTest {
    
    @Autowired
    private PublicTariffRepository publicTariffRepository;

    @Test
    @DisplayName("Новый тариф")
    void handleNewTariffTest(){
        String humanReadableId = "PT-000-011";
        TariffMessage tariffMessage = TariffMessage.builder()
                        .id(publicTariff.getId())
                        .humanReadableId(humanReadableId)
                        .transportTypeId(TransportTypeEnum.PUBLIC.getId())
                        .region(publicTariff.getRegion()).build();

        produceMessage("service.tariff", MessageBuilder.withPayload(tariffMessage).build());
        PublicTariff tariff = publicTariffRepository.findById(tariffMessage.getId()).get();

        assertEquals(tariff.getId(), tariffMessage.getId());
        assertEquals(tariff.getHumanReadableId(), tariffMessage.getHumanReadableId());
        assertEquals(tariff.getRegion(), tariffMessage.getRegion());
    }

    @Test
    @DisplayName("Редактирование тарифа")
    void handleUpdateTariffTest(){
        String humanReadableId = "PT-000-011";
        publicTariffRepository.deleteAll();
        publicTariffRepository.save(publicTariff);

        TariffMessage tariffMessage = TariffMessage.builder()
                .id(publicTariff.getId())
                .humanReadableId(humanReadableId)
                .transportTypeId(TransportTypeEnum.PUBLIC.getId())
                .region(publicTariff.getRegion()).build();

        produceMessage("service.tariff", MessageBuilder.withPayload(tariffMessage).build());
        PublicTariff tariff = publicTariffRepository.findById(tariffMessage.getId()).get();

        assertEquals(tariff.getId(), tariffMessage.getId());
        assertEquals(tariff.getHumanReadableId(), tariffMessage.getHumanReadableId());
        assertEquals(tariff.getRegion(), tariffMessage.getRegion());
    }

}
