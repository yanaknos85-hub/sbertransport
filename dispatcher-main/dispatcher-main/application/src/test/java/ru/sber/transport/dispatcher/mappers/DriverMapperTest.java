package ru.sber.transport.dispatcher.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка маппера водителя")
class DriverMapperTest {

    private final DriverMapper driverMapper = new DriverMapperImpl(new AttributeMapperImpl(
            new ContractorMapperImpl(new BooleanMapperImpl(), new DispatcherMapperImpl())));

    @DisplayName("Проверка сброса подтверждения телефона водителя")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testUpdateDispatcherPhoneConfirmed(boolean phoneChanged) {
        // given
        var driver = Instancio.of(Driver.class)
                .set(field(Driver::isPhoneConfirmed), true)
                .create();

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(field(NewDriverDTO::contactPhone), phoneChanged ? "+79999999999" : driver.getContactPhone())
                .create();

        // when
        driverMapper.update(driver, newDriver);

        // then
        assertThat(driver.isPhoneConfirmed())
                .isEqualTo(!phoneChanged);
    }

}