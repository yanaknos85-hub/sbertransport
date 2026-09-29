package ru.sber.transport.driver_track.mapper;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка маппера водителей")
public class DriverMapperTest {

    @Test
    @DisplayName("Обновление")
    void updateTest() {
        var message = Instancio.create(DriverMessage.class);
        var record = new DriverMessageRecord();
        var mapper = new DriverMapperImpl();

        mapper.update(record, message);

        assertNull(record.getId());
        assertEquals(message.active(), record.getActive());
        assertEquals(message.online(), record.getOnline());
        assertEquals(message.activeTripId(), record.getActiveTripId());
        assertEquals(message.consent(), record.getConsent());
    }
}
