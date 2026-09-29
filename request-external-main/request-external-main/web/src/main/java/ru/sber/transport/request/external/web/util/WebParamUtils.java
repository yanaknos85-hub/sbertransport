package ru.sber.transport.request.external.web.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WebParamUtils {

    /**
     * Преобразует входное значение в список объектов заданного типа.
     *
     * @param value     исходное значение (может быть null, списком, строкой или другим типом)
     * @param converter функция для преобразования строки в целевой тип
     * @return список преобразованных значений (пустой список если value null или пустой)
     */
    public static <T> List<T> parseList(Object value, Function<String, T> converter) {
        if (value == null) {
            return List.of();
        }

        if (value instanceof List) {
            return ((List<?>) value).stream().map(String::valueOf).map(converter).toList();
        }

        var stringValue = String.valueOf(value).trim();
        if (stringValue.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(stringValue.split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .map(converter)
                .toList();
    }

    /**
     * Преобразует объект в экземпляр {@link OffsetDateTime}.
     *
     * @param dateValue объект для преобразования, может быть:
     *                  {@code null}, существующим {@code OffsetDateTime} или строкой в формате ISO-8601
     * @return преобразованный {@code OffsetDateTime} или {@code null}, если был передан {@code null}
     */
    public static OffsetDateTime parseOffsetDateTime(Object dateValue) {
        if (dateValue == null) {
            return null;
        }

        if (dateValue instanceof OffsetDateTime dateTime) {
            return dateTime;
        }

        if (dateValue instanceof String stringValue) {
            try {
                return OffsetDateTime.parse(stringValue);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Expected ISO-8601 format", e);
            }
        }

        throw new IllegalArgumentException("Unsupported date type: " + dateValue.getClass());
    }


    /**
     * Преобразует объект в экземпляр {@link UUID}.
     *
     * @param value объект для преобразования
     * @return преобразованный {@code UUID} или {@code null}, если был передан {@code null}
     */
    public static UUID toUUID(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID) {
            return (UUID) value;
        }
        if (value instanceof String) {
            return UUID.fromString((String) value);
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to UUID");
    }

    /**
     * Парсит строку таймзоны в ZoneOffset. Непарсимые значения возвращают UTC.
     *
     * @param timeZone строковое представление таймзоны
     * @return объект ZoneOffset
     */
    public ZoneOffset parseTimeZoneToOffset(String timeZone) {
        if (timeZone == null || timeZone.trim().isEmpty()) {
            return ZoneOffset.UTC;
        }
        try {
            final var cleaned = timeZone.trim().replace("+", "");
            final var parts = cleaned.split(":");

            final var hours = Integer.parseInt(parts[0].replaceAll("\\D", ""));
            final var minutes = parts.length > 1
                    ? Integer.parseInt(parts[1].replaceAll("\\D", ""))
                    : 0;

            return ZoneOffset.ofHoursMinutes(hours, minutes);
        } catch (Exception e) {
            return ZoneOffset.UTC;
        }
    }
}
