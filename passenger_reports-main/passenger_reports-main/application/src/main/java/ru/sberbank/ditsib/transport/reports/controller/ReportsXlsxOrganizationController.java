package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaskResultDto;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;

import java.util.UUID;

/**
 * Контроллер для выгрузки реестров в xlsx и выгрузок по поездок к выплате.
 */
@RequestMapping({"{organizationId}/xlsx","{organizationId}/xlsx/"})
@Tag(name = "Отчеты", description = "Набор операций для работы с отчетами")
public interface ReportsXlsxOrganizationController {
    
    /**
     * Выгрузка реестра в XLSX
     *
     * @return список подходящих поездок
     */
    @PostMapping(value = {"trip-requests/public","trip-requests/public/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Выгрузка реестра поездок на общественном транспорте в XLSX",
               description = "Выгрузка реестра поездок на общественном транспорте в XLSX")
    TaskResultDto downloadRequestsForPublicInXls(
            @RequestBody RequestForPublicReportDTO requestForPublicReportDTO,
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
            );
    
    /**
     * Выгрузка реестра в XLSX
     *
     * @return список подходящих поездок
     */
    @PostMapping(value = {"trip-requests/taxi","trip-requests/taxi/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Выгрузка реестра поездок на такси в XLSX",
               description = "Выгрузка реестра поездок на такси в XLSX")
    TaskResultDto downloadRequestsForTaxiInXls(
            @RequestBody RequestForTaxiReportDTO requestForTaxiReportDTO,
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                              );
    
    /**
     * Выгрузка реестра в XLSX
     *
     * @return список подходящих поездок
     */
    @PostMapping(value = {"trip-requests/personal","trip-requests/personal/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Выгрузка реестра поездок на личном транспорте в XLSX",
               description = "Выгрузка реестра поездок на личном транспорте в XLSX")
    TaskResultDto downloadRequestsForPersonalInXls(
            @RequestBody RequestForPersonalReportDTO requestForPersonalReportDTO,
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                  );
    
    /**
     * Выгрузка реестра в XLSX
     *
     * @return список подходящих поездок
     */
    @PostMapping(value = {"trip-requests/carsharing","trip-requests/carsharing/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Выгрузка реестра поездок на каршеринге",
               description = "Выгрузка реестра поездок на каршеринге в XLSX")
    TaskResultDto downloadRequestsForCarsharingInXls(
            @RequestBody RequestForCarsharingReportDTO requestForCarsharingReportDTO,
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                    );
    
    @PostMapping(value = {"payment-report/personal","payment-report/personal/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Формирование реестра поездок к выплате",
               description = "Формирование XLSX отчета по запрошенным поездкам на личном транспорте. Все запрошенные поездки со статусом 'Формирование приказа на выплату' будут переведены в статус 'Ожидает выплаты'.")
    TaskResultDto downloadRequestsForPersonalPaymentInXls(
            @RequestBody RequestForPersonalReportDTO requestReportDto,
            @PathVariable("organizationId") UUID organizationId
                                                         );
    
    @PostMapping(value = {"payment-report/public","payment-report/public/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Формирование реестра поездок к выплате",
               description = "Формирование XLSX отчета по запрошенным поездкам на общественном транспорте. Все запрошенные поездки со статусом 'Формирование приказа на выплату' будут переведены в статус 'Ожидает выплаты'.")
    TaskResultDto downloadRequestsForPublicPaymentInXls(
            @RequestBody RequestForPublicReportDTO requestReportDto,
            @PathVariable("organizationId") UUID organizationId
                                                       );
    
}