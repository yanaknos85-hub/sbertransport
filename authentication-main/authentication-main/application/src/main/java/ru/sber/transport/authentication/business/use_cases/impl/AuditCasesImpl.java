package ru.sber.transport.authentication.business.use_cases.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.business.providers.AuditProvider;
import ru.sber.transport.authentication.business.providers.RefreshProvider;
import ru.sber.transport.authentication.business.use_cases.AuditCases;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация кейсов.
 */
@RequiredArgsConstructor
@Component
class AuditCasesImpl implements AuditCases {
    
    private static final String UNKNOWN = "unknown";
    
    private final AuditProvider auditProvider;
    
    private final AccountProvider accountProvider;
    
    private final RefreshProvider refreshProvider;
    
    @Override
    public void login(String login, String userAgent, String clientType) throws AccountNotFoundException {
        auditProvider.login(login, userAgent, clientType);
    }
    
    @Override
    public void logout(UUID userId) {
        var login = accountProvider.get(userId).map(AccountDto::getLogin).orElse(UNKNOWN);
        auditProvider.logout(login);
    }
    
    @Override
    public void refresh(String refresh, String userAgent, String clientType) throws AccountNotFoundException {
        var login = refreshProvider.getAccount(refresh).map(AccountDto::getLogin).orElse(UNKNOWN);
        auditProvider.refresh(login, userAgent, clientType, refresh);
    }
    
    @Override
    public void wrongLogin(String login) {
        auditProvider.wrongLogin(login);
    }
    
    @Override
    public void wrongPassword(String login) {
        auditProvider.wrongPassword(login);
    }

    @Override
    public void tooManyLoginTries(String login) {
        auditProvider.tooManyLoginTries(login);
    }

    @Override
    public Page<AuditDto> get(Integer size, Integer page) {
        return auditProvider.getRecords(Optional.ofNullable(page).orElse(0),
                                        Optional.ofNullable(size).orElse(Integer.MAX_VALUE));
    }
    
    @Override
    public void refreshExpired(String refresh) {
        var login = refreshProvider.getAccount(refresh).map(AccountDto::getLogin).orElse(UNKNOWN);
        auditProvider.refreshExpired(login, refresh);
    }
}
