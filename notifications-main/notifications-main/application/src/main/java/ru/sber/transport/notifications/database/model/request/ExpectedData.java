package ru.sber.transport.notifications.database.model.request;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class ExpectedData {
    
    /**
     * Стоимость
     */
    private final double cost;
    
    /**
     * Расстояние
     */
    private final double distance;
    
    /**
     * Время.
     */
    private final Duration time;
    
}
