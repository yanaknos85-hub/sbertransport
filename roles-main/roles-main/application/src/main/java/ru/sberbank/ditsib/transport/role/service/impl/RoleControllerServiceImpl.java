package ru.sberbank.ditsib.transport.role.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.roles.database.roles.tables.Role;
import ru.sberbank.ditsib.transport.role.dto.RoleDto;
import ru.sberbank.ditsib.transport.role.dto.RoleSortParameters;
import ru.sberbank.ditsib.transport.role.mappers.RoleMapper;
import ru.sberbank.ditsib.transport.role.messaging.senders.RoleSender;
import ru.sberbank.ditsib.transport.role.service.RoleControllerService;
import ru.sberbank.ditsib.transport.role.service.RoleService;

import java.util.Locale;

/**
 * Implementation of role controller service.
 */
@Service
@RequiredArgsConstructor
class RoleControllerServiceImpl implements RoleControllerService {
    
    private final RoleService roleService;
    
    private final RoleSender roleSender;
    
    private final RoleMapper roleMapper;
    
    @Override
    public RoleDto add(RoleDto role) {
        if (roleService.isExists(role.getCode(), role.getName())) {
            throw new DuplicateDataException(Role.class, "code", role.getCode());
        }
        
        role.setCode(correctCode(role.getCode()));
        var entity = roleMapper.toModel(role);
        
        entity = roleService.add(entity);
        roleSender.send(entity, false);
        
        return roleMapper.toDto(entity);
    }
    
    @Override
    public void edit(String code, RoleDto roleDto) {
        var fCode = correctCode(code);
        var oldEntity = roleService.get(fCode).orElseThrow(() -> new EntityNotFoundException(Role.class, fCode));
        var entity = roleService.edit(oldEntity, roleMapper.toModel(roleDto));
        roleSender.send(entity, false);
    }
    
    @Override
    public void delete(String code) {
        var entity = roleService.get(correctCode(code)).orElseThrow(() -> new EntityNotFoundException(Role.class, code));
        roleService.delete(entity);
        roleSender.send(entity, true);
    }
    
    @Override
    public RoleDto get(String code) {
        return roleService.get(correctCode(code)).map(roleMapper::toDto)
            .orElseThrow(() -> new EntityNotFoundException(Role.class, code));
    }

    @Override
    public Iterable<RoleDto> get(boolean paged, RoleSortParameters parameters) {
        if (paged) {
            var page = parameters.getPage();
            var size = parameters.getSize();
            var direction = parameters.getDirection();
            var name = parameters.getField().getName();
            return roleService.get(page, size, direction, name).map(roleMapper::toDto);
        }
        return roleService.get().parallelStream().map(roleMapper::toDto).toList();
    }

    private String correctCode(String source) {
        source = source.toUpperCase(Locale.ROOT);
        if (!source.startsWith("ROLE_")) {
            source = "ROLE_" + source;
        }
        return source;
    }
}
