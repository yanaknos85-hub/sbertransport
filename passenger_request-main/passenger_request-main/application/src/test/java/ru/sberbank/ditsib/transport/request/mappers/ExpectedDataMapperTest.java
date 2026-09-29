package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппера ожидаемых данных")
class ExpectedDataMapperTest {
    
    private final ExpectedDataMapper mapper = new ExpectedDataMapperImpl();
    
    @Test
    @DisplayName("Модель в сообщение")
    void test_toMessage() {
        var expectedData = new ExpectedData();
        expectedData.setCost(0.5);
        expectedData.setDistance(1.5);
        expectedData.setTime(Duration.ofDays(1));
        
        var actual = mapper.toMessage(expectedData);
        
        assertThat(actual.getCost()).isEqualTo(expectedData.getCost());
        assertThat(actual.getDistance()).isEqualTo(expectedData.getDistance());
        assertThat(actual.getTime()).isEqualTo(expectedData.getTime());
    }
    
}