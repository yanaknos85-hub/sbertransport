package ru.sberbank.ditsib.transport.role.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.role.controller.RoleController;
import ru.sberbank.ditsib.transport.role.dto.RoleDto;
import ru.sberbank.ditsib.transport.role.dto.RoleSortParameters;
import ru.sberbank.ditsib.transport.role.service.RoleControllerService;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * Implementation of controller.
 */
@RestController
@RequiredArgsConstructor
class RoleControllerImpl implements RoleController {
    
    private final RoleControllerService roleService;
    
    @Override
    public RoleDto addRole(@Valid RoleDto newRoleDto) {
        return roleService.add(newRoleDto);
    }
    
    @Override
    public void editRole(String code, @Valid RoleDto newDataRoleDto) {
        roleService.edit(code, newDataRoleDto);
    }
    
    @Override
    public void delete(String code) {
        roleService.delete(code);
    }
    
    @Override
    public RoleDto getRole(String code) {
        return roleService.get(code);
    }
    
    @Override
    public Iterable<RoleDto> getRoles(String paged, RoleSortParameters parameters) {
        var toPage = Optional.ofNullable(paged).map(Boolean::parseBoolean).orElse(false);
        return roleService.get(toPage, parameters);
    }
}
