package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;

import jakarta.validation.Valid;

@RequestMapping({"analysis/xlsx","analysis/xlsx/"})
@Tag(name = "Экспорт аналитических отчетов", description = "Набор операций для работы с аналитическими отчетами")
public interface AnalysisXlsxController {
    
    /**
     * Получение аналитических данных по такси
     */
    @PostMapping(value = {"taxi","taxi/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение аналитических данных по такси", description = "Получение аналитических данных по такси")
    byte[] taxiAnalysis(
            @RequestBody @Valid RequestForTaxiReportDTO requestSearchDTO
                       );
    
    
    /**
     * Получение аналитических данных по личному транспорту
     */
    @PostMapping(value = {"personal","personal/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение аналитических данных по личному транспорту", description = "Получение аналитических данных по личному транспорту")
    byte[] personalAnalysis(
            @RequestBody @Valid RequestForPersonalReportDTO requestSearchDTO
                           );
    
    /**
     * Получение аналитических данных по публичному транспорту
     */
    @PostMapping(value = {"public","public/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение аналитических данных по публичному транспорту",
               description = "Получение аналитических данных по публичному транспорту")
    byte[] publicAnalysis(
            @RequestBody @Valid RequestForPublicReportDTO requestSearchDTO
                         );
    
    
    /**
     * Получение аналитических данных по Каршерингу
     */
    @PostMapping(value = {"carsharing","carsharing/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение аналитических данных по каршерингу", description = "Получение аналитических данных по каршерингу")
    byte[] carsharingAnalysis(
            @RequestBody @Valid RequestForCarsharingReportDTO сarsharingRequestDTO
                             );
    
    
}