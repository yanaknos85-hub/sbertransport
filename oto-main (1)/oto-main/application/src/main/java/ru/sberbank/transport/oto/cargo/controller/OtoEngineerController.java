package ru.sberbank.transport.oto.cargo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;
import ru.sberbank.transport.oto.cargo.dto.oto.GetTemplateForCargoForOtoDto;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Получение информации по потоку заявок
 */
@RequestMapping
@Tag(name = "Получение детальной информации о поездке для инженера ОТО",
        description = "Получение детальной информации о поездке для инженера ОТО")
public interface OtoEngineerController {

    /**
     * Получить детальную информацию по заявкам по грузоперевозкам
     *
     * @return OtoEngineerRequestDetailDTO с данными по заявке
     */
    @PostMapping(path = {"cargo", "cargo/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить детальную информацию по заявкам по грузоперевозкам для инженера ОТО",
            description = "Получить детальную информацию по заявкам по грузоперевозкам ОТО")
    Page<OtoEngineerCargoRequestDetailDTO> getCargoRequests(@RequestBody CargoRequestDto cargoRequestDto,
                                                            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Получить детальную информацию по шаблонам на регулярную грузоперевозку
     *
     * @return GetTemplateForCargoForOtoDto с данными по заявке
     */
    @GetMapping(path = {"cargo/{organizationId}/template", "cargo/{organizationId}/template/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить детальную информацию по шаблонам на регулярную грузоперевозку для инженера ОТО",
            description = "Получить детальную информацию по шаблонам на регулярную грузоперевозку ОТО")
    Page<GetTemplateForCargoForOtoDto> getCargoTemplatesForOto(
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Размер страницы")
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer size,
            @Parameter(description = "Номер страницы")
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "Направление сортировки")
            @RequestParam(value = "sortDirection", required = false) SortDirection direction,
            @Parameter(description = "Поле сортировки")
            @RequestParam(value = "sortField", required = false) String field,
            @Parameter(description = "Идентификатор заявки")
            @RequestParam(value = "id", required = false) String id,
            @Parameter(description = "Статусы заявки")
            @RequestParam(value = "status", required = false) TripRequestStatus[] statuses,
            @Parameter(description = "Дата создания заявки, с")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(value = "creationTimeFrom", required = false) ZonedDateTime creationTimeFrom,
            @Parameter(description = "Дата создания заявки, по")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(value = "creationTimeTo", required = false) ZonedDateTime creationTimeTo,
            @Parameter(description = "ФИО отправителя")
            @RequestParam(value = "senderName", required = false) String senderName,
            @Parameter(description = "Адрес отправления")
            @RequestParam(value = "senderAddress", required = false) String senderAddress,
            @Parameter(description = "ФИО получателя")
            @RequestParam(value = "recipientName", required = false) String recipientName,
            @Parameter(description = "Адрес получения")
            @RequestParam(value = "recipientAddress", required = false) String recipientAddress,
            @Parameter(description = "Подразделение инициатора(автора) заявки")
            @RequestParam(value = "authorDepartment", required = false) UUID authorDepartment);
}