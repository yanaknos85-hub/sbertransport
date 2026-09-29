package ru.sberbank.ditsib.transport;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.MockedStatic.Verification;
import org.springframework.boot.SpringApplication;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.RequestApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка запуска")
class ApplicationTest {

    @Test
    void testApplication() {
        var mockStatic = mockStatic(SpringApplication.class);
        mockStatic.when((Verification) SpringApplication.run(RequestApplication.class, new String[] {})).thenReturn(null);
        RequestApplication.main();
        assertThat(SpringApplication.run(RequestApplication.class)).isNull();
        mockStatic.close();
    }
}
