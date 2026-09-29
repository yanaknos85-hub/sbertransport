package ru.sberbank.ditsib.transport.request.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;

@Slf4j
public final class DateTimeUtil {

    public static final String EUROPE_MOSCOW = "Europe/Moscow";
    public static final String GMT = "GMT";
    private static final String ISO_OFFSET_PATTERN = "[+-][0-9]{2}(:[0-9]{2})?";
    public static final String ZERO_TZ = "Z";
    private static final Clock clock = Clock.systemDefaultZone();

    private DateTimeUtil() {
        throw new UnsupportedOperationException();
    }

    public static LocalDateTime toLocalDateTime(long epochSeconds, ZoneId zoneId) {
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(zoneId)
                .toLocalDateTime();
    }

    /**
     * Парсит строку вида "GMT+3", "GMT-5" в объект ZoneId.
     *
     * @param timeZoneStr строка вида "GMT+3", "GMT+0", "GMT-1"
     * @return ZoneId соответствующий смещению
     */
    public static ZoneId parseTimeZone(String timeZoneStr) {
        if (timeZoneStr == null || timeZoneStr.isBlank()) {
            log.warn("Часовой пояс не указан, используем Europe/Moscow по умолчанию");
            return ZoneId.of(EUROPE_MOSCOW);
        }

        final var trimmed = timeZoneStr.strip();
        log.debug("Парсим часовой пояс: '{}'", timeZoneStr);

        if (trimmed.toUpperCase().startsWith("GMT")) {
            var offsetStr = trimmed.substring(3).strip().replaceAll("\\s+", "");
            log.debug("Обнаружен GMT формат, смещение: '{}'", offsetStr);

            if (offsetStr.isEmpty()) {
                log.debug("GMT без смещения, используем UTC");
                return ZoneOffset.UTC;
            }

            if (offsetStr.matches("[+-]\\d{1}(?::\\d{2})?")) {
                String originalOffset = offsetStr;
                offsetStr = offsetStr.replaceFirst("([+-])(\\d{1})", "$10$2");
                log.debug("Нормализован формат смещения: '{}' -> '{}'", originalOffset, offsetStr);
            }

            try {
                final var zoneOffset = ZoneOffset.of(offsetStr);
                log.debug("Успешно распарсен часовой пояс: {}", zoneOffset);
                return zoneOffset;
            } catch (DateTimeException e) {
                log.warn("Не удалось распарсить смещение '{}' для строки '{}', используем Europe/Moscow. Ошибка: {}",
                        offsetStr, timeZoneStr, e.getMessage());
                return ZoneId.of(EUROPE_MOSCOW);
            }
        }

        try {
            final var zoneId = ZoneId.of(trimmed);
            log.debug("Успешно распарсен часовой пояс: {}", zoneId);
            return zoneId;
        } catch (DateTimeException e) {
            log.warn("Не удалось распарсить часовой пояс '{}', используем Europe/Moscow. Ошибка: {}",
                    timeZoneStr, e.getMessage());
            return ZoneId.of(EUROPE_MOSCOW);
        }
    }

    /**
     * Конвертирует часовой пояс в ISO 8601 формат (например, "+03:00").
     * Если входное значение уже в правильном формате, возвращает его без изменений.
     * Если значение в формате "GMT+03", преобразует в "+03:00".
     *
     * @param timeZone Часовой пояс
     * @return Часовой пояс в ISO 8601 формате (например "+03:00" или "Z")
     */
    public static String formatTimeZone(String timeZone) {
        if (timeZone == null) {
            return ZERO_TZ;
        }

        if (timeZone.matches(ISO_OFFSET_PATTERN) || timeZone.equals(ZERO_TZ) || timeZone.equals("UTC")) {
            return timeZone;
        }

        if (timeZone.startsWith("GMT")) {
            var offset = timeZone.substring(3);
            if (offset.startsWith("+") || offset.startsWith("-")) {
                if (offset.length() == 3) {
                    return offset + ":00";
                } else if (offset.length() == 6) {
                    return offset;
                }
            }
        }
        return timeZone;
    }

    /**
     * Разрешает строковое представление часового пояса в ZoneId.
     * Логика:
     * - null/пустая строка -> fallback на clock.getZone()
     * - "Z" или ISO-offset (+HH или +HH:MM) -> ZoneOffset
     * - иначе пытается ZoneId.of(value); при ошибке -> fallback на clock.getZone()
     *
     * @param tzCandidate строка часового пояса (может быть null)
     * @return ZoneId, безопасный fallback при некорректном вводе
     */
    public static ZoneId resolveZoneId(String tzCandidate) {
        if (tzCandidate == null || tzCandidate.isEmpty()) {
            return clock.getZone();
        }
        if (ZERO_TZ.equals(tzCandidate) || tzCandidate.matches(ISO_OFFSET_PATTERN)) {
            return ZoneOffset.of(tzCandidate.equals(ZERO_TZ) ? ZERO_TZ : tzCandidate);
        }
        try {
            return ZoneId.of(tzCandidate);
        } catch (DateTimeException ex) {
            log.warn("Unknown time-zone ID '{}', falling back to clock zone {}", tzCandidate, clock.getZone());
            return clock.getZone();
        }
    }
}
