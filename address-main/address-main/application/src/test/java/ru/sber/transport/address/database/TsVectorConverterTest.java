package ru.sber.transport.address.database;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка работы конвертера")
class TsVectorConverterTest {

    private final TsVectorConverter converter = new TsVectorConverter();

    @DisplayName("Конвертация в")
    @Test
    void test_convert_to() {
        var vector = new TsVector(" ");
        vector.addValue("1");
        vector.addValue("2");
        vector.addValue("3");
        vector.addValue("4");
        vector.addValue("5");

        assertThat(converter.to(vector)).asString().isEqualTo("'1 2 3 4 5'");
    }

}