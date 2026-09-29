package ru.sberbank.ditsib.transport.request.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.HashMap;

/**
 * Настройки для диспетчерской.
 */
@ConfigurationProperties(prefix = "dispatcher")
@Getter
@Setter
public class DispatcherProperties {
    
    /**
     * За указанное время в минутах до истечение контрольного срока необходимо поменять индикацию на красную.
     */
    private final HashMap<TripRequestStatus, Long> deadlineNotifyTimeoutMap = new HashMap<>();
    
    /**
     * Максимальное время контрольного срока в минутах.
     */
    private long maxDeadlineTime = 45L;
    
    /**
     * Граница, после которой увеличение расстояния поездки в км необходимо индицировать желтым.
     */
    private double maxTripDistance = 100.0;
    
    public DispatcherProperties() {
        deadlineNotifyTimeoutMap.put(TripRequestStatus.TAXI_APPROVED, 60L);
        deadlineNotifyTimeoutMap.put(TripRequestStatus.TAXI_AWAITING_SEARCH, 30L);
        deadlineNotifyTimeoutMap.put(TripRequestStatus.TAXI_DRIVER_SEARCH, 10L);
        deadlineNotifyTimeoutMap.put(TripRequestStatus.TAXI_DRIVER_FOUND, 0L);
    }
    
}
