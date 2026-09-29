package ru.sber.transport.contractor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.dto.internal.BranchResponseDto;
import ru.sber.transport.contractor.dto.internal.CreateBranchDto;
import ru.sber.transport.contractor.dto.internal.GetBranchDto;

import java.util.UUID;

/**
 * Controller for working with branches of internal auto-park.
 */
@RequestMapping("/internal-auto-park/branches")
@Tag(name = "Внутренний автопарк", description = "Набор операций для работы с филиалами внутреннего автопарка")
public interface BranchController {

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание", description = "Создание филиала внутреннего автопарка")
    void createBranch(
            @Parameter(hidden = true) JwtAuthenticationToken token,
            @RequestBody CreateBranchDto createBranchDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

    @PutMapping(value = "/{externalId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактирование", description = "Реадктирование филиала внутреннего автопарка")
    void updateBranch(
            @PathVariable(name = "externalId") UUID externalId,
            @Parameter(hidden = true) JwtAuthenticationToken token,
            @RequestBody CreateBranchDto createBranchDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

    @DeleteMapping(value = "/{externalId}/")
    @Operation(summary = "Удаление", description = "Удаление филиала внутреннего автопарка")
    void deleteBranch(
            @PathVariable(name = "externalId") UUID externalId,
            @Parameter(hidden = true) JwtAuthenticationToken token,
            @RequestParam(name = "organizationId") UUID organizationId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение филиалов внутреннего автопарка")
    Page<BranchResponseDto> getAllBranches(
            @Parameter(hidden = true) JwtAuthenticationToken token,
            GetBranchDto getBranchDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );

}
