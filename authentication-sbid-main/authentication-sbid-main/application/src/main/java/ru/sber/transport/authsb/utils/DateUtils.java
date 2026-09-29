package ru.sber.transport.authsb.utils;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Конвертирует ISO-строку с Z-таймстампом (например, "2031-02-20T08:31:41.794Z")
     * в LocalDateTime в UTC.
     */
    public static LocalDateTime fromIsoZonedDateTime(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            return null;
        }
        return Instant.parse(isoString)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}