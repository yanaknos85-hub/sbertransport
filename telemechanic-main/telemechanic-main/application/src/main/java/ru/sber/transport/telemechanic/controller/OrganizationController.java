package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.telemechanic.dto.OrganizationDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithDepartmentDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for working with organization.
 */
@RequestMapping("organization")
@Validated
@Tag(name = "Информация по организациям", description = "Набор операций для работы с получением информации по организациям")
public interface OrganizationController {
    
    /**
     * Get the user's organization.
     *
     * @param authentication auth info.
     *
     * @return {@link OrganizationDto}.
     */
    @GetMapping(value = "/employee", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение организации пользователя", description = "Получение данных организации пользователя")
    OrganizationDto get(@Parameter(hidden = true) Authentication authentication);
    
    /**
     * Get all organizations.
     *
     * @return list {@link OrganizationDto}.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение организаций",
               description = "Получение данных организаций")
    List<OrganizationDto> getAll();
    
    /**
     * Get all organizations with internal contractors.
     *
     * @return list {@link OrganizationDto}.
     */
    @GetMapping(value = "/internal-autopark", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение организаций с внутренними автопарками",
               description = "Получение данных организаций")
    List<OrganizationDto> getAllWithInternalContractor();
    /**
     * Get organization with department.
     *
     * @param request  list of organization ids.
     *
     * @return list {@link OrganizationWithDepartmentDto}.
     */
    @PostMapping(value = "/department", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение организаций с подразделениями", description = "Получение данных организаций с подразделениями")
    List<OrganizationWithDepartmentDto> getAllWithDepartment(@RequestBody @NotEmpty Set<UUID> request);
    
    /**
     * Получение списка подразделений по идентификатору организации, у которых есть активные тарифы
     * @param id идентификатор организации
     * @return {@link TariffDepartmentResponse} список подразделений
     */
    @GetMapping(value = "/{id}/departments-with-tariffs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка департаментов", description = "Получение списка департаментов, для которых есть тариф")
    List<TariffDepartmentResponse> getAllDepartmentWithTariff(@PathVariable UUID id);
}

