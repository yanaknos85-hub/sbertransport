package ru.sber.transport.authentication.providers.refresh.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.Session;
import ru.sber.transport.database.authentication.tables.records.SessionRecord;
import ru.sber.transport.authentication.providers.refresh.dao.SessionRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class SessionRepositoryImpl implements SessionRepository {

    @Override
    public Optional<SessionRecord> findByToken(String accessToken) {
        return context().selectFrom(table()).where(table().TOKEN.eq(accessToken)).fetchOptional();
    }

    @Override
    public Optional<SessionRecord> findByTokenAndExpiredAtBefore(String accessToken, LocalDateTime expiration) {
        return context().selectFrom(table()).where(table().TOKEN.eq(accessToken)).and(table().EXPIRE_AT.greaterThan(expiration))
                .fetchOptional();
    }

    @Override
    public List<SessionRecord> findAllByAccountAndExpiredAtAfter(String login, LocalDateTime expiration) {
        var accountTable = Account.ACCOUNT;
        return context().select().from(table())
                .innerJoin(accountTable).on(table().ACCOUNT_ID.eq(accountTable.ID))
                .where(accountTable.LOGIN.eq(login)).and(table().EXPIRE_AT.greaterOrEqual(expiration))
                .fetchInto(SessionRecord.class);
    }

    @Override
    public Optional<SessionRecord> findByIdAndExpiredAtAfter(UUID id, LocalDateTime expiration) {
        return context().selectFrom(table()).where(table().ID.eq(id)).and(table().EXPIRE_AT.greaterOrEqual(expiration))
                .fetchOptional();
    }

    @Override
    public List<SessionRecord> findAllByExpiredAtBefore(LocalDateTime now) {
        return context().selectFrom(table()).where(table().EXPIRE_AT.lessOrEqual(now))
                .fetchInto(SessionRecord.class);
    }

    @Override
    public Collection<SessionRecord> findAllByAccountInAndExpiredAtAfter(List<String> logins, LocalDateTime expiration) {
        var accountTable = Account.ACCOUNT;
        return context().select().from(table())
                .innerJoin(accountTable).on(table().ACCOUNT_ID.eq(accountTable.ID))
                .where(accountTable.LOGIN.in(logins)).and(table().EXPIRE_AT.greaterOrEqual(expiration))
                .fetchInto(SessionRecord.class);
    }

    @Override
    public Session table() {
        return Session.SESSION;
    }
}
