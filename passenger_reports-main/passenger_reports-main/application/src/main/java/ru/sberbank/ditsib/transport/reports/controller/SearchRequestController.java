package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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

import jakarta.validation.Valid;

@Deprecated
@RequestMapping
@Validated
@Tag(name = "Заявки", description = "Поиск заявок")
public interface SearchRequestController {
    /**
     * Get all requests by taxi search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"taxi_report","taxi_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок такси c пагинацией", deprecated = true)
    Page<TaxiResponseDTO> getTaxi(
            @RequestBody @Valid RequestForTaxiReportDTO requestSearchDTO
                                 );
    
    /**
     * Get all requests by personal search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"personal_report","personal_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок c личным транспортом c пагинацией", deprecated = true)
    Page<PersonalResponseDTO> getPersonal(
            @RequestBody @Valid RequestForPersonalReportDTO requestSearchDTO
                                         );
    
    /**
     * Get all requests by public search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"public_report","public_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок c общественным транспортом c пагинацией", deprecated = true)
    Page<PublicResponseDTO> getPublic(
            @RequestBody @Valid RequestForPublicReportDTO requestSearchDTO
                                     );
    
    /**
     * Get all requests by carsharing search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"carsharing_report","carsharing_report/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск каршеринг заявок c пагинацией", deprecated = true)
    Page<CarsharingResponseDTO> getСarsharing(
            @RequestBody @Valid RequestForCarsharingReportDTO сarsharingRequestDTO
                                             );

    /**
     * Get all requests by group transfer search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"group_transfer_report","group_transfer_report/"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок группового трансфера c пагинацией")
    Page<GroupTransferResponseDTO> getGroupTransferReport(
            @RequestBody @Valid RequestForGroupTransferReportDTO requestSearchDTO
    );
    
}