package ru.sber.transport.driver_track.service.gpsspoofing;

import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;

/**
 * Контекст состояния алгоритма фильтрации GPS-спуфинга.
 */
public record GpsFilterContext(
    /**
     * Текущее состояние фильтра.
     */
    State state,
    /**
     * Последняя валидная точка (не спуфинг).
     */
    CoordinateRecord lastValidPoint,
    /**
     * Последняя отклонённая (ложная) точка.
     */
    CoordinateRecord previousFalsePoint,
    /**
     * Счётчик интервалов в фазе SPOOFING.
     */
    int intervalCount,
    /**
     * Средняя скорость в км/ч на момент перехода в SPOOFING.
     */
    double averageSpeedKmh
) {

    /**
     * Состояние алгоритма фильтрации.
     */
    public enum State {
        /**
         * Точки принимаются/отклоняются по обычной логике.
         */
        NORMAL,
        /**
         * Ожидаем подтверждение возврата — точки отклоняются.
         */
        SPOOFING
    }

    /**
     * Создаёт начальный NORMAL контекст.
     */
    public static GpsFilterContext normal(CoordinateRecord firstPoint) {
        return new GpsFilterContext(State.NORMAL, firstPoint, null, 0, 0);
    }

    /**
     * Создаёт контекст перехода в SPOOFING.
     */
    public static GpsFilterContext spoofing(CoordinateRecord lastValidPoint,
                                            CoordinateRecord firstFalsePoint,
                                            double averageSpeedKmh) {
        return new GpsFilterContext(State.SPOOFING, lastValidPoint, firstFalsePoint, 1, averageSpeedKmh);
    }

    /**
     * Создаёт обновлённый контекст SPOOFING с новой ложной точкой и увеличенным счётчиком.
     */
    public GpsFilterContext withNewFalsePoint(CoordinateRecord newFalsePoint) {
        return new GpsFilterContext(State.SPOOFING, lastValidPoint, newFalsePoint, intervalCount + 1, averageSpeedKmh);
    }

    /**
     * Создаёт контекст подтверждения возврата — NORMAL с новой валидной точкой.
     */
    public GpsFilterContext withReturn(CoordinateRecord returnPoint) {
        return new GpsFilterContext(State.NORMAL, returnPoint, null, 0, 0);
    }

    /**
     * Создаёт контекст SPOOFING с отклонённой точкой возврата (сохраняем intervalCount, обновляем Sprev).
     */
    public GpsFilterContext withRejectedReturn(CoordinateRecord rejectedPoint) {
        return new GpsFilterContext(State.SPOOFING, lastValidPoint, rejectedPoint, intervalCount, averageSpeedKmh);
    }
}
