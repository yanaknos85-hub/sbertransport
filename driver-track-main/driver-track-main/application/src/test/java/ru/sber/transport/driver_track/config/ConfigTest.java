package ru.sber.transport.driver_track.config;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.model.ConsentCheckModel;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.repository.DriverRepository;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка конфигурации подписания ПДн")
public class ConfigTest {

    @Test
    @DisplayName("Создание бина функции получения признака подписания ПДн")
    void getConsentFunctionTest() {
        var driverRepository = mock(DriverRepository.class);
        var consentFunctionConfig = new Config();
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                Instancio.create(Boolean.class), Instancio.create(UUID.class), Instancio.create(Boolean.class),
                Instancio.create(Boolean.class));

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        var func = consentFunctionConfig.getConsentFunction(driverRepository);

        var result = func.apply(new ConsentCheckModel(UUID.randomUUID(), Collections.emptyList()));

        assertEquals(driver.getConsent(), result);
    }
}
