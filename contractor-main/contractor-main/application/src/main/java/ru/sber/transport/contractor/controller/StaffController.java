package ru.sber.transport.contractor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.enums.StaffSpeciality;
import ru.sber.transport.contractor.dto.internal.CreateStaffDto;
import ru.sber.transport.contractor.dto.internal.GetStaffDto;
import ru.sber.transport.contractor.dto.internal.StaffDto;

import java.util.List;
import java.util.UUID;

/**
 * Controller for working with internal auto-park staff.
 */
@RequestMapping("/internal-auto-park/staff")
@Tag(name = "Внутренний автопарк", description = "Набор операций для работы с сотрудниками внутренего автопарка")
public interface StaffController {

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание", description = "Создание сотрудника внутреннего автопарка")
    void createStaff(
            @Parameter(hidden = true) JwtAuthenticationToken token,
            @RequestBody CreateStaffDto createStaffDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение сотрудников внутреннего автопарка")
    Page<StaffDto> getStaff(
            @Parameter(hidden = true) JwtAuthenticationToken token,
            GetStaffDto getStaffDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

    @DeleteMapping(value = "/{externalId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Удаление", description = "Удаление сотрудника внутреннего автопарка")
    void deleteStaff(
            @Parameter(hidden = true) JwtAuthenticationToken token,
            @RequestParam("speciality") GetStaffDto.Speciality speciality,
            @RequestParam(name = "organizationId") UUID organizationId,
            @PathVariable(name = "externalId") UUID externalId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

    @PatchMapping(value = "/{externalId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение сотрудника")
    void patchStaff(@Parameter(hidden = true) JwtAuthenticationToken token,
                    @PathVariable(name = "externalId") UUID externalId,
                    @RequestBody List<PatchData> data,
                    @RequestParam("speciality") StaffSpeciality speciality,
                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

}
