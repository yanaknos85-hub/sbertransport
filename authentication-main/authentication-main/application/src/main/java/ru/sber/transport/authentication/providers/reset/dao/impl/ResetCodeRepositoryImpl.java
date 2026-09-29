package ru.sber.transport.authentication.providers.reset.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.ResetCode;
import ru.sber.transport.database.authentication.tables.records.ResetCodeRecord;
import ru.sber.transport.authentication.providers.reset.dao.ResetCodeRepository;

import java.util.Optional;

@Transactional
@Repository
public class ResetCodeRepositoryImpl implements ResetCodeRepository {

    @Override
    public Optional<ResetCodeRecord> findByEmail(String email) {
        var account = Account.ACCOUNT;
        return context().select().from(table())
                .innerJoin(account).on(table().ACCOUNT_ID.eq(account.ID))
                .where(account.EMAIL.eq(email)).fetchOptionalInto(ResetCodeRecord.class);
    }

    @Override
    public ResetCode table() {
        return ResetCode.RESET_CODE;
    }

}
