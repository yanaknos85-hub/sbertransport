package ru.sber.transport.request_checks.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ErrorMessages {

    public static final String MULTIPOINT_LIMIT = "Превышен лимит поездок с количеством точек > 2";
    public static final String DURATION_LIMIT = "Превышена продолжительность поездок в 12 часов в сутки";
    public static final String OVERRUN_LIMIT = "Превышена суммарная протяженность поездок за месяц";

}
