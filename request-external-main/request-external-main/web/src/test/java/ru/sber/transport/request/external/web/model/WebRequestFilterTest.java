package ru.sber.transport.request.external.web.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Feature;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.web.model.State;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка фильтра")
class WebRequestFilterTest {

    @Test
    @DisplayName("Тестирование статусов")
    void test_states() {
        final var actual = WebRequestFilter.builder().status(List.of(State.CANCELLED)).build().states();

        assertThat(actual).containsExactly(ru.sber.transport.request.external.model.State.CANCELLED);
    }

}