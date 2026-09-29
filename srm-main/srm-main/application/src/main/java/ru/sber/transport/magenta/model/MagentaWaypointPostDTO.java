package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO Адреса маршрута для публикации
 */
@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class MagentaWaypointPostDTO {
    /**
     * Улица номер дома
     */
    @NotBlank
    private final String address;
    
    /**
     * Широта
     */
    @NotNull
    private final Double latitude;
    
    /**
     * Долгота
     */
    @NotNull
    private final Double longitude;
    
    @Min(0)
    private final Integer waitingTimeMin;
}
