package ru.sberbank.ditsib.transport.tariff.util;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.tariff.dto.VatType;
import ru.sberbank.ditsib.transport.tariff.exceptions.VatValueException;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_trip_registry_check")
@DisplayName("Тест утилиты нормализации налоговых ставок")
class VatTypeValidatorTest {

    @ParameterizedTest
    @MethodSource("validValues")
    void getByValueValidReturnsCorrectVatTypeTest(Integer input, VatType expected) {
        var result = VatTypeValidator.getByValue(input);

        assertEquals(result, expected);
        assertEquals(result.getPercentValue(), input);
    }

    static Stream<Arguments> validValues() {
        return Arrays.stream(VatType.values())
                .map(vt -> Arguments.of(vt.getPercentValue(), vt));
    }


    @ParameterizedTest
    @ValueSource(ints = {15, 25, 100, -1})
    void getByValueUnknownValueThrowsIllegalArgumentExceptionTest(Integer invalidValue) {
        assertThatThrownBy(() -> VatTypeValidator.getByValue(invalidValue))
                .isInstanceOf(VatValueException.class)
                .hasMessage("Wrong vat value: [%s]".formatted(invalidValue));
    }
}
