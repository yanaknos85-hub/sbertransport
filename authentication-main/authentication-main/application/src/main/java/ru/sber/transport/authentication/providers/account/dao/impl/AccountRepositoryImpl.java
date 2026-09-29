package ru.sber.transport.authentication.providers.account.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.account.mapper.AccountMapper;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountMapper accountMapper;

    @Override
    public Optional<AccountRecord> findByActiveTrueAndLogin(String login) {
        return context().selectFrom(table()).where(table().LOGIN.eq(login)).and(table().ACTIVE.isTrue()).fetchOptional();
    }

    @Override
    public Optional<AccountRecord> findByActiveTrueAndId(UUID userId) {
        return context().selectFrom(table()).where(table().ID.eq(userId)).and(table().ACTIVE.isTrue())
                .fetchOptional();
    }

    @Override
    public AccountRecord getByLogin(String login) {
        return context().selectFrom(table()).where(table().LOGIN.eq(login)).fetchOne();
    }

    @Override
    public int countByLoginLike(String login) {
        return context().fetchCount(context().selectFrom(table()).where(table().LOGIN.likeIgnoreCase(login)));
    }

    @Override
    public Collection<AccountRecord> findAllByActiveTrueAndIdIn(Collection<UUID> id) {
        return context().selectFrom(table()).where(table().ID.in(id)).and(table().ACTIVE.isTrue()).fetchInto(AccountRecord.class);
    }

    @Override
    public Collection<AccountRecord> findAllByActiveTrueAndLoginIn(List<String> logins) {
        return context().selectFrom(table()).where(table().ACTIVE.isTrue()).and(table().LOGIN.in(logins)).fetchInto(AccountRecord.class);
    }

    @Override
    public Optional<AccountRecord> findAllByActiveIsTrueAndEmail(String email) {
        return context().selectFrom(table()).where(table().ACTIVE.isTrue()).and(table().EMAIL.eq(email)).fetchOptional();
    }

    @Override
    public @NonNull AccountRecord save(@NonNull AccountRecord accountRecord) {
        var idField = table().ID;
        var toSave = findById(accountRecord.getId())
                .orElse(accountRecord);

        accountMapper.update(toSave, accountRecord);

        if (idField.changed(toSave)) {
            this.context().insertInto(this.table()).set(toSave)
                    .onConflict(table().LOGIN).doUpdate()
                    .set(incrementLogin(toSave)).execute();
        } else {
            this.context().update(this.table()).set(toSave).where(idField.eq(toSave.get(idField.getName(), idField.getType()))).execute();
        }
        return toSave;
    }

    @Override
    public Account table() {
        return Account.ACCOUNT;
    }

    private AccountRecord incrementLogin(AccountRecord accountRecord) {
        var login = accountRecord.getLogin();
        var count = countByLoginLike(login + "%");
        accountRecord.setLogin(count == 0 ? login : login + count);
        return accountRecord;
    }
}
