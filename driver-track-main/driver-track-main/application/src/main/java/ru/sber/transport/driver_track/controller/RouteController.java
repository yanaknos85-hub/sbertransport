package ru.sber.transport.driver_track.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sber.transport.driver_track.dto.BatchCoordinateRequest;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.dto.RouteType;

import java.util.UUID;

/**
 * Контроллер для работы с координатами.
 */
public interface RouteController {

    /**
     * Сохранение очередной точки водителя.
     *
     * @param geoWaypointDTO geo info.
     */
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Информация о последней точке", description = "Информация о последней точке")
    void savePointInfo(@Valid @RequestBody GeoWaypointDTO geoWaypointDTO,
                       @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Получение фактического маршрута.
     *
     * @param tripId     идентификатор поездки.
     * @param sourceType источник данных.
     * @param routeType  тип маршрута: плановый/фактический.
     * @return маршрут.
     */
    @GetMapping(value = "/")
    @Operation(summary = "Плановый/Фактический маршрут", description = "Получение планового/фактического маршрута")
    ResponseEntity<RouteDTO> getRoute(@RequestParam(value = "tripId") UUID tripId,
                                      @RequestParam(value = "sourceType", defaultValue = "FORMULA", required = false) RouteSource sourceType,
                                      @RequestParam(value = "routeType", defaultValue = "FACT", required = false) RouteType routeType);

    /**
     * Пакетная отправка координат водителя.
     *
     * @param request пакет координат.
     */
    @PostMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Пакетная отправка координат", description = "Пакетная отправка координат для сохранения точек, потерянных при отсутствии интернета")
    void saveBatchPoints(@Valid @RequestBody BatchCoordinateRequest request,
                         @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
