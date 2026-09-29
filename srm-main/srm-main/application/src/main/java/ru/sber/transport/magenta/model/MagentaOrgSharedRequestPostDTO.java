package ru.sber.transport.magenta.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.converters.MagentaZonedDateTimeUTCDeserializer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO точки маршрута для публикации
 */
@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class MagentaOrgSharedRequestPostDTO {

    /**
     * Количество пассажиров
     */
    @NotNull(message = "Passenger count cannot be null")
    @Min(value = 1, message = "Passenger count must be greater than 0")
    private final Integer passengers;
    
    /**
     * Идентификатор тарифа в мадженте, UUID->positive Long
     */
    @NotNull(message = "Tariff id cannot be null")
    private final String tariffId;
    
    /**
     * Временная зона
     */
    private final String timezone;
    
    /**
     * Время начала поездки
     */
    @JsonDeserialize(using = MagentaZonedDateTimeUTCDeserializer.class)
    private final ZonedDateTime pickupStartTime;
    
    /**
     * Время прибытия во последнюю точку маршрута
     */
    @JsonDeserialize(using = MagentaZonedDateTimeUTCDeserializer.class)
    private final ZonedDateTime dropStartTime;
    
    /**
     * остановки
     */
    @Builder.Default
    private final List<MagentaWaypointPostDTO> stops = new ArrayList<>();
}
