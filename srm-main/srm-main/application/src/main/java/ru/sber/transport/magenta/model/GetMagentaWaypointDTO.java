package ru.sber.transport.magenta.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.sberbank.ditsib.converters.ZonedDateTimeToUTCSerializer;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * DTO точки маршрута для публикации
 */
@Data
@AllArgsConstructor
@ToString
@Builder
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class GetMagentaWaypointDTO {
    
    /**
     * Id заказа из  маджента
     */
    @NotNull
    private Integer orderId;
    
    /**
     * id заявки, в которой задана данная точка
     */
    private UUID requestId;
    
    /**
     * Состояние точки маршрута. true - активна false - удалена/отменена
     */
    private boolean active;
    
    /**
     * Тип события (посадка, высадка, ожидание)
     */
    private String eventType;
    
    /**
     * Время начала остановки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    private ZonedDateTime startTime;
    
    /**
     * Время завершения остановки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    private ZonedDateTime endTime;
    
    /**
     * Точка маршрута
     */
    private WaypointDTO waypoint;
}
