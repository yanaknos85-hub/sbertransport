package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.telemechanic.dto.RegionDto;

import java.util.List;

/**
 * Controller for working with regions
 */
@Validated
@RequestMapping("region-codes")
@Tag(name = "Коды регионов", description = "Получение списка кодов и наименований регионов")
public interface RegionController {
    
    /**
     * Get list of all regions
     *
     * @return List of {@link RegionDto}
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение регионов", description = "Получение списка кодов и наименований регионов")
    List<RegionDto> getRegions();

}
