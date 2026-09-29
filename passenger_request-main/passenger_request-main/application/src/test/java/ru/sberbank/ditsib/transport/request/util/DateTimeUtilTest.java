package ru.sberbank.ditsib.transport.request.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sberbank.ditsib.transport.request.util.DateTimeUtil.EUROPE_MOSCOW;

class DateTimeUtilTest {

    @Test
    void toLocalDateTime_shouldConvertEpochSecondsToLocalDateTimeInGivenZone() {
        long epochSeconds = 1717000000L; // Пример: 28 мая 2024 г., 10:26:40 UTC
        ZoneId zoneId = ZoneId.of(EUROPE_MOSCOW); // UTC+3

        LocalDateTime result = DateTimeUtil.toLocalDateTime(epochSeconds, zoneId);

        LocalDateTime expected = Instant.ofEpochSecond(epochSeconds)
                .atZone(zoneId)
                .toLocalDateTime();

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "GMT+3,   +03:00",
            "GMT +3,  +03:00",
            "GMT-5,   -05:00",
            "GMT +0,  Z",
            "GMT+0,   Z",
            "GMT-1,   -01:00",
            "GMT +12, +12:00",
            "GMT -8,  -08:00",
            "GMT+03:00,  +03:00",
            "GMT-05:00,  -05:00",
            "GMT+02:30,  +02:30",
    })
    void parseTimeZone_shouldParseValidGmtFormats(String input, String expectedOffset) {
        ZoneId result = DateTimeUtil.parseTimeZone(input);

        ZoneId expectedZone = ZoneId.of(expectedOffset.equals("Z") ? "UTC" : expectedOffset);
        if (expectedOffset.equals("Z")) {
            assertThat(result).isEqualTo(ZoneOffset.UTC);
        } else {
            assertThat(result).isEqualTo(expectedZone);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    void parseTimeZone_shouldReturnMoscowOnNullEmptyOrBlank(String input) {
        ZoneId result = DateTimeUtil.parseTimeZone(input);

        assertThat(result).isEqualTo(ZoneId.of(EUROPE_MOSCOW));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GMT+X",
            "GMT++3",
            "GMT-",
            "GMT++",
            "XYZ",
            "GMT+123:45"
    })
    void parseTimeZone_shouldReturnMoscowOnInvalidFormat(String input) {
        ZoneId result = DateTimeUtil.parseTimeZone(input);

        assertThat(result).isEqualTo(ZoneId.of(EUROPE_MOSCOW));
    }

    @Test
    void parseTimeZone_shouldBeCaseInsensitiveForGmt() {
        String input = "gmt+4";

        ZoneId result = DateTimeUtil.parseTimeZone(input);

        assertThat(result).isEqualTo(ZoneOffset.ofHours(4));
    }
}