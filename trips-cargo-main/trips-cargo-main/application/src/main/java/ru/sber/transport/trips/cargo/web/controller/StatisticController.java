package ru.sber.transport.trips.cargo.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sber.transport.trips.cargo.business.dto.TripAssignStatisticDto;

import java.util.UUID;

/**
 * Контроллер статистики.
 */
@RequestMapping("/contractor/{contractorId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/")
public interface StatisticController {

    @GetMapping(value = "/statistic/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поездки", description = "Получение статистики по назначению поездок")
    TripAssignStatisticDto getAssignStatistic(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @RequestParam(value = "autoparkId",required = false) UUID autoparkId
    );
}
