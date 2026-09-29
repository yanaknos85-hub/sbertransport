package ru.sberbank.ditsib.transport.request.database.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(title = "Запрос маршрута", description = "Данные для запроса расстояния маршрута")
public class RouteRequest {
    /**
     * Coordinates of route.
     */
    @Schema(description = "Координаты ключевых точек маршрутов")
    @Builder.Default
    private List<Address> coordinates = new ArrayList<>();
    
    /**
     * Distance unit.
     */
    @Schema(description = "Единицы измерения расстояния")
    private DistanceUnit distanceUnit;
}
