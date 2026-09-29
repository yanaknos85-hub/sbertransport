package ru.sberbank.ditsib.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;

import java.util.ArrayList;
import java.util.List;

/**
 * Object with data for requesting route.
 */
@Getter
@Schema(title = "Запрос маршрута", description = "Данные для запроса маршрута")
public class RouteRequestDto {

    /**
     * Coordinates of route.
     */
    @Schema(description = "Координаты ключевых точек маршрутов")
    private final List<WaypointDto> coordinates = new ArrayList<>();

    /**
     * Distance unit.
     */
    @Schema(description = "Единицы измерения расстояния")
    private DistanceUnit distanceUnit;

    /**
     * Route type for requesting.
     */
    @Schema(description = "Тип маршрута (пешеходный, автомобильный и пр.). Игнорируется, если transportServiceType = EMPLOYEE_TRANSPORTATION")
    private RouteType routeType;

    /**
     * Type of transport service
     */
    @Schema(description = "Тип транспортного сервиса")
    private String transportServiceType;

}
