package ru.sber.transport.token_generator.providers.database;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.token_generator.database.token_generator.tables.Roles;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;

/**
 * Repository of roles.
 */
public interface RolesRepository extends JooqRepository<Roles, RolesRecord, String> {
}
