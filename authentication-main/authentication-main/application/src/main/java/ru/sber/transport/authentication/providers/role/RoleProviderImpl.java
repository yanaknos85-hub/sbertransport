package ru.sber.transport.authentication.providers.role;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.JSON;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.RoleDto;
import ru.sber.transport.authentication.business.providers.RoleProvider;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.authentication.providers.role.mapper.RolesMapper;
import ru.sber.transport.roles.messages.RoleMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация провайдера ролей.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class RoleProviderImpl implements RoleProvider,
        ru.sber.transport.authentication.messaging.listeners.providers.RoleProvider {

    private final AccountRepository accountRepository;

    private final RoleRepository roleRepository;

    private final RolesMapper rolesMapper;

    @Override
    public void setRoles(AccountDto accountDto, Set<String> roles) {
        accountRepository.findByActiveTrueAndLogin(accountDto.getLogin())
                .map(AccountRecord::getId)
                .ifPresent(accountRecord -> {
                    roleRepository.clearRoles(accountRecord);
                    roleRepository.findAllByCode(roles).forEach(role -> roleRepository.add(accountRecord, role));
                });
    }

    @Override
    public Set<RoleDto> getRoles(AccountDto accountDto) {
        return roleRepository.findAllByAccountId(accountDto.getId())
                .stream()
                .map(rolesMapper::toBusiness)
                .collect(Collectors.toSet());
    }

    @Override
    public ArrayList<String> getDefaultForList(AccountDto accountDto) {
        var db = accountRepository.findByActiveTrueAndLogin(accountDto.getLogin()).orElseThrow();
        var roles = roleRepository.findAllByAccountId(db.getId());
        var defaultForList = new ArrayList<String>();
        var objectMapper = new ObjectMapper();
        if(!roles.isEmpty()){
            roles.forEach(roleRecord -> {
                try {
                    defaultForList.addAll(Arrays.asList(objectMapper.readValue(roleRecord.getDefaultFor().data(), String[].class)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }
        return defaultForList;
    }

    @Override
    public void delete(String code) {
        roleRepository.findById(code).ifPresent(role -> {
            roleRepository.clearRole(role.getCode());
            roleRepository.delete(role);
        });
    }

    @Override
    public void save(RoleMessage message) {
        var role = rolesMapper.toModel(message);
        var objectMapper = new ObjectMapper();
        try {
            role.setDefaultFor(JSON.valueOf(objectMapper.writeValueAsString(message.scopes())));
        } catch (JsonProcessingException e) {
            log.warn("Processing data failed", e);
        }
        roleRepository.save(role);
    }
}
