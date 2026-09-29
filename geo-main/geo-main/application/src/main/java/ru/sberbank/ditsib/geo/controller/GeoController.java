package ru.sberbank.ditsib.geo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.geo.dto.*;

import java.util.Collection;
import java.util.List;

/**
 * Controller for working with geo service.
 */
@RequestMapping("/")
@Tag(name = "ГЕО", description = "Набор операций для работы с гео-сервисом")
public interface GeoController {

    /**
     * Get address by coordinates.
     *
     * @return address.
     */
    @GetMapping(path = "address", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение адресов", description = "Получение информации об адресах по данных")
    Collection<AddressDto> getAddress(AddressRequestDto requestDto);

    /**
     * Request for route.
     *
     * @param routeRequest route request.
     * @return route.
     */
    @PostMapping(path = "route", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение маршрута", description = "Получение информации о маршруте")
    RouteDto getRoute(@RequestBody RouteRequestDto routeRequest);

    /**
     * Request for routes.
     *
     * @param routeRequest route request.
     * @return route.
     */
    @PostMapping(path = "routes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение маршрутов", description = "Получение информации о возможных маршрутах")
    @ResponseBody
    List<RouteDto> getRoutes(@RequestBody RouteRequestDto routeRequest);

    @PostMapping(path = "region", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение региона", description = "Получение региона по координатам")
    @ResponseBody
    RegionDto getRegion(@RequestBody WaypointDto waypoint);

}
