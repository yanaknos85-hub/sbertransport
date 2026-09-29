package ru.sberbank.ditsib.transport.tariff.util;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@DisplayName("Проверка утилиты")
@ActiveProfiles("test")
public class ConvertUtilsTest {
    
    @DisplayName("Проверка конвертации таймзоны")
    @Test
    void test_date() {
        var test = OffsetDateTime.parse("2024-11-14T02:59:59.999+07:00").withOffsetSameLocal(ConvertUtils.toOffset(ZoneId.of("GMT+3")));
        assertThat(test.toString()).isEqualTo("2024-11-14T02:59:59.999+03:00");
    }
    
}
