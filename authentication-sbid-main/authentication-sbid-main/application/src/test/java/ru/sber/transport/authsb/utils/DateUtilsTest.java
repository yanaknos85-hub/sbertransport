package ru.sber.transport.authsb.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DateUtilsTest {

    @Test
    @DisplayName("Проверка нулового значения")
    void test1() {
        var result = DateUtils.fromIsoZonedDateTime(null);
        assertThat(result)
                .isNull();
    }

    @Test
    @DisplayName("Проверка пустой строки")
    void test2() {
        var result = DateUtils.fromIsoZonedDateTime("");
        assertThat(result)
                .isNull();
    }
}
