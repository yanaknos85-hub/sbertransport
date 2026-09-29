package ru.sber.transport.dispatcher.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.dto.NewDispatcherDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка маппера диспетчера")
class DispatcherMapperTest {

    private final DispatcherMapper dispatcherMapper = new DispatcherMapperImpl();

    @DisplayName("Проверка сброса подтверждения телефона диспетчера")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testUpdateDispatcherPhoneConfirmed(boolean phoneChanged) {
        // given
        var dispatcher = Instancio.of(Dispatcher.class)
                .set(field(Dispatcher::isPhoneConfirmed), true)
                .create();

        var newDispatcher = Instancio.of(NewDispatcherDto.class)
                .set(field(NewDispatcherDto::phone), phoneChanged ? "+79999999999" : dispatcher.getPhone())
                .create();

        // when
        dispatcherMapper.update(dispatcher, newDispatcher);

        // then
        assertThat(dispatcher.isPhoneConfirmed())
                .isEqualTo(!phoneChanged);
    }
}