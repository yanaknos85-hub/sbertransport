package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportResponseDTO;

import jakarta.validation.Valid;

@RequestMapping({"analysis","analysis/"})
@Tag(name = "Аналитические отчеты", description = "Набор операций для работы с аналитическими отчетами")
public interface AnalysisController {
    
    /**
     * Получение данных по общему аналитическому отчету
     *
     * @param request параметры запроса в виде JSON year - год, за который нужно получить данные
     */
    @PostMapping(value = {"general","general/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить данные общего аналитического отчета", description = "Получить данные общего аналитического отчета")
    GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(
            @RequestHeader(value = "X-Version", defaultValue = "1") int version,
            @RequestBody @Valid GeneralAnalyticalReportRequestDTO request,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                                     );
}