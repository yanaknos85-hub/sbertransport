package ru.sber.transport.authentication.providers.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.AccountRolesRecord;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.account.mapper.AccountMapper;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.authentication.providers.role.model.Scope;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация провайдера УЗ.
 */
@SuppressWarnings("java:S3958")
@RequiredArgsConstructor
@Transactional
@Component
public class AccountProviderImpl implements AccountProvider {
    
    private final AccountRepository accountRepository;
    
    private final RoleRepository roleRepository;
    
    private final AccountMapper mapper;
    
    @Override
    public Optional<AccountDto> get(String login) {
        return accountRepository.findByActiveTrueAndLogin(login).map(mapper::toBusiness);
    }
    
    @Override
    public Optional<AccountDto> get(UUID id) {
        return accountRepository.findByActiveTrueAndId(id).map(mapper::toBusiness);
    }

    @Override
    public Collection<AccountDto> get(Collection<UUID> id) {
        return accountRepository.findAllByActiveTrueAndIdIn(id).stream().map(mapper::toBusiness).toList();
    }

    @Override
    public Optional<AccountDto> getAllActiveness(UUID id) {
        return accountRepository.findById(id).map(mapper::toBusiness);
    }

    @Override
    public void setPassword(AccountDto accountDto, String hash, boolean isTransfer) {
        var dbAccount = accountRepository.findByActiveTrueAndLogin(accountDto.getLogin()).orElseThrow(
                () -> new EntityNotFoundException(Account.class, accountDto.getLogin()));
        dbAccount.setHash(hash);
        dbAccount.setTransferPassword(isTransfer);
        accountRepository.save(dbAccount);
    }
    
    @Override
    public void deactivate(AccountDto accountDto) {
        var dbAccount = accountRepository.findByActiveTrueAndLogin(accountDto.getLogin()).orElseThrow();
        dbAccount.setActive(false);
        accountRepository.save(dbAccount);
    }

    @Override
    public void deactivate(Collection<AccountDto> accountDto) {
        var dbAccount = accountRepository.findAllByActiveTrueAndLoginIn(accountDto.stream().map(AccountDto::getLogin).toList());
        dbAccount.forEach(acc -> acc.setActive(false));
        accountRepository.saveAll(dbAccount);
    }
    
    @Override
    public void save(AccountDto accountDto) {
        save(List.of(accountDto));
    }

    @Override
    public void save(Collection<AccountDto> accountDtos) {
        var accounts = accountRepository.findAllById(accountDtos.stream().map(AccountDto::getId).toList())
                .parallelStream().collect(Collectors.toMap(AccountRecord::getId, Function.identity()));
        for (var accountDto : accountDtos) {
            var savedAccount = accounts.computeIfAbsent(accountDto.getId(), id -> new AccountRecord());
            mapper.toModel(savedAccount, accountDto);
            accountRepository.save(savedAccount);

            var savedRoles = roleRepository.findAllByAccountId(savedAccount.getId());
            if (savedRoles.isEmpty() && accountDto.getRoles() == null) {
                var scope = Optional.ofNullable(accountDto.getScope()).map(Enum::name).map(Scope::valueOf).orElse(Scope.EMPLOYEE);
                var roleSet = roleRepository.findAll().stream()
                        .filter(r -> roleRepository.getDefaultFor(r.getDefaultFor()).contains(scope))
                        .collect(Collectors.toUnmodifiableSet());
                savedAccount.setActive(accountDto.isActive());
                for (var roleCode : roleSet) {
                    var accountRole = new AccountRolesRecord();
                    accountRole.setAccountId(savedAccount.getId());
                    accountRole.setRoleCode(roleCode.getCode());
                    roleRepository.add(savedAccount.getId(), roleCode);
                }
            }
            if (accountDto.getRoles() != null) {
                roleRepository.clearRoles(accountDto.getId());
                for (var roleCode : accountDto.getRoles().keySet()) {
                    roleRepository.findById(roleCode).ifPresent(r -> roleRepository.add(accountDto.getId(), r));
                }
            }
        }
    }
    
    @Override
    public boolean empty() {
        return accountRepository.count() == 0;
    }

    @Override
    public Optional<AccountDto> getByEmail(String email) {
        return accountRepository.findAllByActiveIsTrueAndEmail(email).map(mapper::toBusiness);
    }

    @Override
    public int getLoginsCount(String login) {
        return accountRepository.countByLoginLike(login + "%");
    }
}
