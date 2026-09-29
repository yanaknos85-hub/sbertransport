package ru.sber.transport.request_checks.util;

import com.google.protobuf.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class TimeUtils {

    public static ZoneId parseZoneId(String timeZone) {
        if (timeZone == null) {
            throw new IllegalArgumentException("timeZone must not be null");
        }
        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid time zone ID: " + timeZone, e);
        }
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp, String zoneOffset) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
            ZoneId.of(zoneOffset)
        );
    }

    public static OffsetDateTime getStartOfDay(OffsetDateTime date, int dayStartHour, String dayStartTz, String dataTimeZone) {
        ZoneId dayStartZoneId = ZoneId.of(dayStartTz);
        ZoneId dataZoneId = ZoneId.of(dataTimeZone);

        // Преобразуем дату в таймзону начала дня для определения даты
        ZonedDateTime dayStartZoned = date.atZoneSameInstant(dayStartZoneId);
        LocalDate localDate = dayStartZoned.toLocalDate();

        // Получаем начало дня по таймзоне начала дня (с учетом dayStartHour)
        ZonedDateTime startOfDayInDayStartTz = localDate.atTime(LocalTime.of(dayStartHour, 0)).atZone(dayStartZoneId);

        // Преобразуем в таймзону для данных
        OffsetDateTime startOfDayInDataTz = startOfDayInDayStartTz.withZoneSameInstant(dataZoneId).toOffsetDateTime();

        return startOfDayInDataTz;
    }

}
