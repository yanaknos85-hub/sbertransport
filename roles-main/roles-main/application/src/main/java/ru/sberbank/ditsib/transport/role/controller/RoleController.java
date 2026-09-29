package ru.sberbank.ditsib.transport.role.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.role.dto.RoleDto;
import ru.sberbank.ditsib.transport.role.dto.RoleSortParameters;

import jakarta.validation.Valid;

/**
 * Controller for working with roles.
 */
@RequestMapping("/")
@Tag(name = "Роли", description = "Набор операций для работы с ролями")
public interface RoleController {
    
    /**
     * Add a new role.
     *
     * @param newRoleDto data of new role.
     * @return added role.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление новой роли")
    RoleDto addRole(@Valid @RequestBody RoleDto newRoleDto);
    
    /**
     * Edit role.
     *
     * @param code code of role to edit.
     * @param newDataRoleDto new data of role.
     */
    @PutMapping(value = "{code}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных роли")
    void editRole(@PathVariable("code") String code, @Valid @RequestBody RoleDto newDataRoleDto);
    
    /**
     * Delete role.
     *
     * @param code code of role to delete.
     */
    @DeleteMapping(value = "{code}")
    @Operation(summary = "Удаление", description = "Удаление данных роли")
    void delete(@PathVariable("code") String code);
    
    /**
     * Get role.
     *
     * @param code code of role to get.
     * @return role.
     */
    @GetMapping(value = "{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных роли")
    RoleDto getRole(@PathVariable("code") String code);
    
    /**
     * Get all roles.
     *
     * @return roles collection.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение всех ролей")
    Iterable<RoleDto> getRoles(
            @RequestHeader(value = "X-Paged", required = false) String paged,
            RoleSortParameters parameters
    );
    
}
