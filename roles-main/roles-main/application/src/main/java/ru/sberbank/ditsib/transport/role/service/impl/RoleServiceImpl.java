package ru.sberbank.ditsib.transport.role.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.roles.database.roles.tables.Role;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.ditsib.transport.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.role.service.RoleService;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса ролей.
 */
@Transactional
@Component
@RequiredArgsConstructor
class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    
    @Override
    public boolean isExists(String code, String name) {
        return roleRepository.existsByCodeOrName(code, name);
    }
    
    @Override
    public RoleRecord add(RoleRecord role) {
        return roleRepository.save(role);
    }
    
    @Override
    public RoleRecord edit(RoleRecord old, RoleRecord newEntity) {
        if (!old.getCode().equals(newEntity.getCode())) {
            roleRepository.delete(old);
        }
        old.setName(newEntity.getName());
        old.setDescription(newEntity.getDescription());
        old.setDefaultFor(newEntity.getDefaultFor());
        old.setDataMaster(newEntity.getDataMaster());
        old.setExclusive(newEntity.getExclusive());
        return roleRepository.save(old);
    }
    
    @Override
    public void delete(RoleRecord role) {
        roleRepository.delete(role);
    }
    
    @Override
    public Optional<RoleRecord> get(String code) {
        return roleRepository.findById(code);
    }
    
    @Override
    public Page<RoleRecord> get(int page, int size, Direction direction, String name) {
        return roleRepository.findAll(PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), name));
    }

    @Override
    public List<RoleRecord> get() {
        return roleRepository.findAll(Sort.by(Role.ROLE.NAME.getName()));
    }
}
