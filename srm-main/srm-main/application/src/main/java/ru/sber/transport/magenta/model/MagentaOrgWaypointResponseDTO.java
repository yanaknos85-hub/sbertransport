package ru.sber.transport.magenta.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.sberbank.ditsib.converters.MagentaZonedDateTimeToUTCSerializer;

import java.time.ZonedDateTime;

/**
 * DTO Адреса маршрута, результат публикации
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(title = "Остановка", description = "Один из адресов маршрута")
public class MagentaOrgWaypointResponseDTO {
    
    public static final String EVENT_BOARD = "Посадка";
    
    public static final String EVENT_UNBOARD = "Высадка";
    
    public static final String EVENT_WAIT = "Ожидание";
    
    public static final String ACTIVE_STATE = "Active";
    
    /**
     * ID заказа, в котором были заданы остановки поездки
     */
    private String orderId;
    
    /**
     * Состояние заказа в поездке, активен/неактивен
     */
    private String state = ACTIVE_STATE;
    
    /**
     * Улица номер дома
     */
    private String address;
    
    /**
     * Широта
     */
    private Double latitude;
    
    /**
     * Долгота
     */
    private Double longitude;
    
    /**
     * рассчитанное время остановки для начала поездки
     */
    @JsonSerialize(using = MagentaZonedDateTimeToUTCSerializer.class)
    private ZonedDateTime startTime;
    
    /**
     * рассчитанное время остановки для окончания поездки
     */
    @JsonSerialize(using = MagentaZonedDateTimeToUTCSerializer.class)
    private ZonedDateTime endTime;
    
    /**
     * тип остановки
     */
    private String eventType;
}
