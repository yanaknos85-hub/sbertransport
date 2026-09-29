package ru.sber.transport.magenta.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.sberbank.ditsib.converters.MagentaZonedDateTimeUTCDeserializer;
import ru.sberbank.ditsib.converters.ZonedDateTimeToUTCSerializer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO маршрута для публикации совместимый с маджентой, используется для CompatibilityController SRM
 */
@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class ExtendedNewMagentaSharedRequestPostDTO {
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
     * Полный идентификатор тарифа
     */
    private final String tariffUuid;
    
    /**
     * Время начала поездки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaZonedDateTimeUTCDeserializer.class)
    private final ZonedDateTime pickupStartTime;
    
    /**
     * Время прибытия во последнюю точку маршрута
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaZonedDateTimeUTCDeserializer.class)
    private final ZonedDateTime dropStartTime;
    
    /**
     * Временная зона.
     */
    @Schema(description = "Временная зона")
    private String timeZone;
    
    /**
     * Остановки.
     */
    @Size(min = 2, message = "Minimum of 2 waypoints is required")
    @Builder.Default
    private final List<MagentaWaypointPostDTO> stops = new ArrayList<>();
}
