package ru.sberbank.ditsib.geo_zones.web.http.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneWithChildrenDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.NewGeoZoneDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.WaypointDto;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Tag(name = "Контроллер геозон", description = "Добавление/изменение/удаление геозон")
@RequestMapping
@Validated
public interface GeoZoneController {
    
    @Operation(summary = "Добавление", description = "Добавление геозоны")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GeoZoneDto create(@RequestBody @Valid NewGeoZoneDto data);
    
    @Operation(summary = "Изменение", description = "Изменение геозоны")
    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    void edit(
            @PathVariable("id") @Parameter(description = "Идентификатор для изменения", required = true) UUID id,
            @RequestBody @Valid NewGeoZoneDto data
             );
    
    @Operation(summary = "Удаление", description = "Удаление геозоны")
    @DeleteMapping(value = "{id}")
    void delete(@PathVariable("id") @Parameter(description = "Идентификатор для удаления", required = true) UUID id);
    
    @Operation(summary = "Получение", description = "Получение геозоны")
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GeoZoneDto get(
            @PathVariable("id") @Parameter(description = "Идентификатор для получения", required = true) UUID id
                  );
    
    @Operation(summary = "Получение всех", description = "Получение всех геозон")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<GeoZoneDto> getAll();
    
    @Operation(summary = "Получение всех", description = "Получение полного списка геозон в виде дерева")
    @GetMapping(value = "roots", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<GeoZoneWithChildrenDto> getRoots();
    
    
    @Operation(summary = "Получение списка дочерних зон(на 1 уровень глубже)",
               description = "Получение списка " + "дочерних зон")
    @GetMapping(value = "children", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<GeoZoneDto> getChildren(UUID parentId);
    
    @PostMapping(path = "search", produces = MediaType.APPLICATION_JSON_VALUE, consumes =
            MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение геозоны", description = "Получение геозоны по данным адреса")
    @ResponseBody
    GeoZoneDto search(@RequestBody WaypointDto waypoint);
    
    @PostMapping(path = "searchBranch", produces = MediaType.APPLICATION_JSON_VALUE, consumes =
            MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка геозон", description = "Получение геозоны и родителей по данным адреса")
    @ResponseBody
    List<GeoZoneDto> searchBranch(@RequestBody WaypointDto waypoint);
    
    
}
