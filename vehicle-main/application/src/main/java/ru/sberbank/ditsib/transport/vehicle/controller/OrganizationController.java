package ru.sberbank.ditsib.transport.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.vehicle.dto.GetDepartmentsInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for working with organization.
 */
@RequestMapping("organization")
@Tag(name = "Информация по организациям", description = "Набор операций для работы с получением информации по организациям")
public interface OrganizationController {
    
    /**
     * Get all organizations.
     *
     * @param authentication auth info.
     *
     * @return list {@link OrganizationDto}.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение организаций", description = "Получение данных организаций")
    List<OrganizationDto> getAll(@Parameter(hidden = true) Authentication authentication);
    
    /**
     * Get organization with department.
     *
     * @param request  list of organization ids.
     *
     * @return list {@link GetDepartmentsInfo}.
     */
    @PostMapping(value = "/department", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение организаций с подразделениями", description = "Получение данных организаций с подразделениями")
    List<GetDepartmentsInfo> getAllWithDepartment(@RequestBody @NotEmpty Set<UUID> request);
    
    
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
}
