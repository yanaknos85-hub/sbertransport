package ru.sber.transport.token_generator.providers.database;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.token_generator.database.token_generator.tables.AccountRoles;
import ru.sber.transport.token_generator.database.token_generator.tables.records.AccountRolesRecord;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JooqRepository<AccountRoles, AccountRolesRecord, UUID> {

    List<String> findRoles(String id);

    void saveAll(String id, List<String> roles);

    void deleteAll(String id, List<String> roles);
}
