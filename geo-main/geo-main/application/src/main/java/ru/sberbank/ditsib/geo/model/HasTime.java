package ru.sberbank.ditsib.geo.model;

import java.time.Duration;
import java.util.Optional;

/**
 * Объект содержит компонент времени.
 */
public interface HasTime {
    
    /**
     * Получить время.
     *
     * @return сохраненное время.
     */
    Duration getTime();
    
    /**
     * Установить время.
     *
     * @param time новое значение.
     */
    void setTime(Duration time);
    
    /**
     * Добавить время к текущему значению.
     *
     * @param time время для добавления.
     */
    default void addTime(Duration time) {
        setTime(Optional.ofNullable(getTime()).orElse(Duration.ZERO).plus(time));
    }
    
}
