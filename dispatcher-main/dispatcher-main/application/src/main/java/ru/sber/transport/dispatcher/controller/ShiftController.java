package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ShiftSearchDto;
import ru.sber.transport.dispatcher.dto.search.VehicleShiftSearchDto;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.validation.ShiftEmptyValidator;

import jakarta.validation.Valid;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequestMapping("/")
@Tag(name = "Смены", description = "Набор операций для работы со сменами")
public interface ShiftController {

    @PostMapping(value = "/{contractorId}/shift/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Создание смены")
    @Validated({ShiftEmptyValidator.class})
    List<ShiftDTO> addShift(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody List<ShiftDTO> shiftDTOList
    );

    @GetMapping(value = "/{contractorId}/shift/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение смен")
    Page<ShiftResponseDTO> getShifts(
            @PathVariable("contractorId") UUID contractorId,
            ShiftSearchDto shiftSearchDto,
            @Parameter(hidden = true) Authentication authentication

    );

    @GetMapping(value = "/shifts/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение списка смен")
    List<ShiftListResponseDto> getShiftList(
            @RequestParam("startDate") OffsetDateTime startDate,
            @Parameter(hidden = true) Authentication authentication
    );

    @GetMapping(value = "/{contractorId}/vehicle-shift/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение смен по автомобилям")
    Page<VehicleShiftResponse> getVehicleShifts(
            @PathVariable("contractorId") UUID contractorId,
            VehicleShiftSearchDto searchDto,
            @Parameter(hidden = true) Authentication authentication

    );

    @PostMapping(value = "/{contractorId}/vehicle-shift/status/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение статусов автомобилей в сменах")
    List<VehicleShiftStatusResponse> getVehicleShiftsStatus(
            @PathVariable("contractorId") UUID contractorId,
            @RequestBody VehicleShiftsStatusDto vehicleShiftsStatusDto,
            @Parameter(hidden = true) Authentication authentication

    );

    @PutMapping(value = "/{contractorId}/shift/{shiftId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Смены", description = "Изменение смены")
    void editShift(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("shiftId") UUID shiftId,
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid ShiftDTO shiftDTO
    );

    @GetMapping(value = "/{contractorId}/shift/{shiftId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение смены")
    ShiftResponseDTO getShift(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("shiftId") UUID shiftId,
            @Parameter(hidden = true) Authentication authentication

    );

    @DeleteMapping(value = "/{contractorId}/shift/{shiftId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение смены")
    void deleteShift(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("shiftId") UUID shiftId,
            @Parameter(hidden = true) Authentication authentication

    );

    @DeleteMapping(value = "/{contractorId}/shift/row/{rowId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смены", description = "Получение смены")
    void deleteRowOfShifts(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("rowId") UUID rowId,
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid DeleteShiftRowDTO dto
            );

    @PostMapping(value = "/shifts/ewb/first-title/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение первых титулов эпл созданных по сменам")
    List<FirstTitleResponseDto> getEwbShifts(
            @RequestBody @Valid FirstTitleRequestDto dto,
            @Parameter(hidden = true) Authentication authentication
    );

    @PostMapping(value = "/shifts/ewb/sign/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Подписание EWB", description = "Подписание EWB документов")
    void signEwbDocument(
            @RequestBody @Valid List<SignEwbRequestDto> signRequests,
            @Parameter(hidden = true) Authentication authentication
    );

}
