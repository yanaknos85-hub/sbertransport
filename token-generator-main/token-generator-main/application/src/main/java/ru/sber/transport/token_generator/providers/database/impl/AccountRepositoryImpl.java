package ru.sber.transport.token_generator.providers.database.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.token_generator.database.token_generator.tables.AccountRoles;
import ru.sber.transport.token_generator.database.token_generator.tables.records.AccountRolesRecord;
import ru.sber.transport.token_generator.providers.database.AccountRepository;

import java.util.List;
import java.util.UUID;

@Repository
class AccountRepositoryImpl implements AccountRepository {

    @Override
    public AccountRoles table() {
        return AccountRoles.ACCOUNT_ROLES;
    }

    @Override
    public List<String> findRoles(String id) {
        return context().select(table().ROLE).from(table()).where(table().ID.eq(UUID.fromString(id)))
                .fetchInto(String.class);
    }

    @Override
    public void saveAll(String id, List<String> roles) {
        for (var role : roles) {
            var accountRecord = new AccountRolesRecord();
            accountRecord.setId(UUID.fromString(id));
            accountRecord.setRole(role);

            save(accountRecord);
        }
    }

    @Override
    public void deleteAll(String id, List<String> roles) {
        context().deleteFrom(table()).where(table().ID.eq(UUID.fromString(id))).and(table().ROLE.in(roles)).execute();
    }
}
