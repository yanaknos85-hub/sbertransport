package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.search.OrganizationSearchDto;

import java.util.UUID;

@RequestMapping
@Validated
@Tag(name = "Заявки", description = "Поиск заявок организации")
public interface SearchRequestOrganizationController {
    /**
     * Get all requests by taxi search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"{organizationId}/taxi_report","{organizationId}/taxi_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок такси c пагинацией")
    Page<TaxiResponseDTO> getTaxi(
            @RequestBody @Valid RequestForTaxiReportDTO requestSearchDTO,
            @PathVariable(value = "organizationId", required = false) UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                 );
    
    /**
     * Get all requests by personal search dto
     *
     * @return list of requests.
     */
    
    @PostMapping(value = {"{organizationId}/personal_report","{organizationId}/personal_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок c личным транспортом c пагинацией")
    Page<PersonalResponseDTO> getPersonal(
            @RequestBody @Valid RequestForPersonalReportDTO requestSearchDTO,
            @PathVariable(value = "organizationId", required = false) UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                         );
    
    /**
     * Get all requests by public search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"{organizationId}/public_report","{organizationId}/public_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок c общественным транспортом c пагинацией")
    Page<PublicResponseDTO> getPublic(
            @RequestBody @Valid RequestForPublicReportDTO requestSearchDTO,
            @PathVariable(value = "organizationId", required = false) UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                     );
    
    /**
     * Get all requests by carsharing search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"{organizationId}/carsharing_report","{organizationId}/carsharing_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск каршеринг заявок c пагинацией")
    Page<CarsharingResponseDTO> getCarsharing(
            @RequestBody @Valid RequestForCarsharingReportDTO сarsharingRequestDTO,
            @PathVariable(value = "organizationId", required = false) UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                             );
    
    /**
     * Get all requests by group transfer search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"{organizationId}/group_transfer_report","{organizationId}/group_transfer_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок группового трансфера c пагинацией")
    Page<GroupTransferResponseDTO> getGroupTransferReport(
            @RequestBody @Valid RequestForGroupTransferReportDTO requestSearchDTO,
            @PathVariable(value = "organizationId", required = false) UUID organizationId
                                                         );
    
    @GetMapping(value ="/search-organisation", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить организации частично совпадающие по наименованию входного параметра поиска")
    OrganizationSearchDto getOrganizationBySearchParameter(@RequestParam String organizationName);
}
