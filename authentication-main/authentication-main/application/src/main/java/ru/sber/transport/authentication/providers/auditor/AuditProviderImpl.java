package ru.sber.transport.authentication.providers.auditor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.sber.transport.database.authentication.tables.Audit;
import ru.sber.transport.database.authentication.tables.records.AuditRecord;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.providers.AuditProvider;
import ru.sber.transport.authentication.messaging.senders.UserAgentSender;
import ru.sber.transport.authentication.providers.auditor.dao.AuditRepository;
import ru.sber.transport.authentication.providers.auditor.mapper.AuditMapper;
import ru.sber.transport.authentication.providers.auditor.model.Action;
import ru.sber.transport.authentication.providers.auditor.model.Result;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AuditProviderImpl implements AuditProvider {

    private final AuditRepository auditRepository;

    private final AuditMapper mapper;

    private final UserAgentSender userAgentSender;

    @Override
    public void wrongLogin(String login) {
        audit(login, Action.LOGIN, Result.WRONG_LOGIN, null);
    }

    @Override
    public void wrongPassword(String login) {
        audit(login, Action.LOGIN, Result.WRONG_PASSWORD, null);
    }

    @Override
    public void tooManyLoginTries(String login) {
        audit(login, Action.LOGIN, Result.TOO_MANY_LOGIN_TRIES, null);
    }

    @Override
    public void login(String login, String userAgent, String clientType) throws AccountNotFoundException {
        audit(login, Action.LOGIN, Result.SUCCESS, null);
        if (userAgent != null && clientType != null) {
            userAgentSender.send(login, userAgent, clientType);
        }
    }

    @Override
    public void logout(String login) {
        audit(login, Action.LOGOUT, Result.SUCCESS, null);
    }

    @Override
    public void refreshExpired(String login, String refresh) {
        audit(login, Action.RELOGIN, Result.WRONG_LOGIN, refresh);
    }

    @Override
    public void refresh(String login, String userAgent, String clientType, String refresh) throws AccountNotFoundException {
        audit(login, Action.RELOGIN, Result.SUCCESS, refresh);
        if (userAgent != null && clientType != null) {
            userAgentSender.send(login, userAgent, clientType);
        }
    }

    @Override
    public Page<AuditDto> getRecords(int page, int size) {
        var pageRequest = PageRequest.of(page, size, Sort.by(Audit.AUDIT.TIMESTAMP.getName()).descending());
        return auditRepository.findAll(pageRequest).map(mapper::toBusiness);
    }

    private void audit(String login, Action action, Result result, String refresh) {
        var auditRecord = new AuditRecord();
        auditRecord.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        auditRecord.setResult(result.name());
        auditRecord.setAction(action.name());
        auditRecord.setLogin(login);
        auditRecord.setSessionId(refresh == null ? null : UUID.fromString(refresh));
        auditRepository.save(auditRecord);
    }
}
