package ru.sber.transport.request_checks.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DateTimeException;
import java.time.ZoneId;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Проверка работы TimeUtils")
class TimeUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "UTC",
        "GMT",
        "GMT+2",
        "GMT-03:00",
        "Europe/Moscow",
        "America/New_York",
        "Asia/Tokyo",
        "+02:00",
        "-05:00",
        "Z"
    })
    void parseZoneIdValidZoneIdReturnsZoneId(String validZoneId) {
        val result = TimeUtils.parseZoneId(validZoneId);
        assertNotNull(result);
        assertEquals(ZoneId.of(validZoneId), result);
    }

    @Test
    void parseZoneIdNullArgumentThrowsIllegalArgumentExceptionWithMessage() {
        val exception = assertThrows(
            IllegalArgumentException.class,
            () -> TimeUtils.parseZoneId(null)
        );
        assertEquals("timeZone must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "Invalid/Zone",
        "Europe-Moscow",
        "123",
        "UTC+25:00",
        "GMT+19:00"
    })
    void parseZoneIdInvalidZoneIdThrowsIllegalArgumentException(String invalidZoneId) {
        val exception = assertThrows(
            IllegalArgumentException.class,
            () -> TimeUtils.parseZoneId(invalidZoneId)
        );
        assertTrue(exception.getMessage().contains("Invalid time zone ID: " + invalidZoneId));
        assertNotNull(exception.getCause());
        assertInstanceOf(DateTimeException.class, exception.getCause());
    }

}

