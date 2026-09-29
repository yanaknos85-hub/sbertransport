package ru.sber.transport.driver_track.messaging.listener;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.support.GenericMessage;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.driver_track.service.DriverService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка слушателя кафки")
public class ListenerConfigTest {

    private final ListenerConfig listenerConfig = new ListenerConfig();

    @Test
    @DisplayName("Получение водителя")
    void driverInputTest() {
        var driverService = mock(DriverService.class);
        var payload = Instancio.create(DriverMessage.class);


        var messages = """
                {
                	"id": "309b6705-5046-4b4d-a70b-fcbf46ace3e6",
                	"lastName": "Тестовый",
                	"firstName": "Водитель",
                	"patronymic": null,
                	"passport": "2525 123456",
                	"contractorId": "901437e6-fab8-4994-900c-4ead3e09e0ed",
                	"active": true,
                	"rating": 500,
                	"driverLicenseNumber": "12 34 987654",
                	"cargoLicenceNumber": null,
                	"serviceLicenseNumber": "АС28555666",
                	"latitude": null,
                	"longitude": null,
                	"pointTime": null,
                	"timeZone": null,
                	"serving": false,
                	"online": true,
                	"activeTripId": null,
                	"activeShiftId": "b1845e13-e284-4111-b175-2f2fe5fd8071",
                	"licenseClasses": null,
                	"experience": "MORE_THEN_TEN",
                	"contactPhone": "+7(888)5551111",
                	"phoneConfirmed": false,
                	"email": "test@sbr.ru",
                	"consent": false,
                	"humanReadableId": "DR-0395-00000001",
                	"driverSpeciality": null
                }
                """;

        var message = new GenericMessage<>(payload);

        var func = listenerConfig.driverInput(driverService);

        func.accept(message);

        var captor = ArgumentCaptor.forClass(DriverMessage.class);
        verify(driverService).save(captor.capture());

        var savedDriver = captor.getValue();
        assertEquals(payload.getId(), savedDriver.getId());
        assertEquals(payload.active(), savedDriver.active());
        assertEquals(payload.online(), savedDriver.online());
        assertEquals(payload.activeTripId(), savedDriver.activeTripId());
        assertEquals(payload.consent(), savedDriver.consent());
    }

    @Test
    @DisplayName("Получение водителя через ссл")
    void driverInputSslTest() {
        var driverService = mock(DriverService.class);
        var payload = Instancio.create(DriverMessage.class);
        var message = new GenericMessage<>(payload);

        var func = listenerConfig.driverInputSsl(driverService);

        func.accept(message);

        var captor = ArgumentCaptor.forClass(DriverMessage.class);
        verify(driverService).save(captor.capture());

        var savedDriver = captor.getValue();
        assertEquals(payload.getId(), savedDriver.getId());
        assertEquals(payload.active(), savedDriver.active());
        assertEquals(payload.online(), savedDriver.online());
        assertEquals(payload.activeTripId(), savedDriver.activeTripId());
        assertEquals(payload.consent(), savedDriver.consent());
    }
}
