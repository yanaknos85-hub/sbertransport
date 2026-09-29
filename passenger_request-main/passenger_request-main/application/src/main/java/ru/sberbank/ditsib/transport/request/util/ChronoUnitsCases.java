package ru.sberbank.ditsib.transport.request.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import javax.validation.constraints.Positive;

public enum ChronoUnitsCases {
    DAYS(ChronoUnit.DAYS, "день", "дня", "дней"),
    HOURS(ChronoUnit.HOURS, "час", "часа", "часов"),
    MINUTES(ChronoUnit.MINUTES, "минута", "минуты", "минут");

    private final ChronoUnit unit;
    private final String nominative;
    private final String genitive;
    private final String plural;

    public static String convertValueAndChronoUnitToString(ChronoUnit unit, @Positive Integer value) {
        ChronoUnitsCases constant = getEnumByChronoUnit(unit);
        StringBuilder builder = new StringBuilder();
        builder.append(value).append(" ");
        if (value % 10 == 1 && value % 100 != 11) {
            builder.append(constant.nominative);
        } else if (value % 10 < 2 || value % 10 > 4 || value % 100 >= 10 && value % 100 < 20) {
            builder.append(constant.plural);
        } else {
            builder.append(constant.genitive);
        }

        return builder.toString();
    }

    public static ChronoUnitsCases getEnumByChronoUnit(ChronoUnit unit) {
        return Arrays.stream(values()).filter((c) -> c.unit.equals(unit)).findFirst().orElseThrow(() -> new IllegalArgumentException("Некорректное значение единицы времени: %s!".formatted(unit.name())));
    }

    public static String durationToString(Duration duration) {
        StringBuilder timeInString = new StringBuilder();
        String hours = duration.toHoursPart() > 0 ? convertValueAndChronoUnitToString(ChronoUnit.HOURS, duration.toHoursPart()) : "";
        String minutes = duration.toMinutesPart() > 0 ? convertValueAndChronoUnitToString(ChronoUnit.MINUTES, duration.toMinutesPart()) : "";
        if (hours.isBlank() && minutes.isBlank()) {
            timeInString.append("менее 1 минуты");
        } else {
            timeInString.append(hours).append(" ").append(minutes);
        }

        return timeInString.toString().trim();
    }

    private ChronoUnitsCases(ChronoUnit unit, String nominative, String genitive, String plural) {
        this.unit = unit;
        this.nominative = nominative;
        this.genitive = genitive;
        this.plural = plural;
    }

    public ChronoUnit getUnit() {
        return this.unit;
    }

    public String getNominative() {
        return this.nominative;
    }

    public String getGenitive() {
        return this.genitive;
    }

    public String getPlural() {
        return this.plural;
    }
}
