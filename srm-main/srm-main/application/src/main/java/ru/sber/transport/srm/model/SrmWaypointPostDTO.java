package ru.sber.transport.srm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * DTO Адреса маршрута для публикации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class SrmWaypointPostDTO implements Waypoint {
    
    /**
     * Идентификатор точки.
     */
    private UUID id;
    
    /**
     * Широта
     */
    @NotNull
    private Double latitude;
    
    /**
     * Долгота
     */
    @NotNull
    private Double longitude;
    
    /**
     * Время ожидания
     */
    @Builder.Default
    private Integer waitingTime = 0;
    
    /**
     * Наименование
     */
    private String address;
    
    /**
     * Количество грузчиков
     */
    @Builder.Default
    private Integer loaderNumber = 0;
}
