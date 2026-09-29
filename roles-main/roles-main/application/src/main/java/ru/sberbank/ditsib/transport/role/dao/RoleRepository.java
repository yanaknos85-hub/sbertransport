package ru.sberbank.ditsib.transport.role.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.roles.database.roles.tables.Role;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;

/**
 * Repository of roles.
 */
public interface RoleRepository extends JooqRepository<Role, RoleRecord, String> {
    
    /**
     * Check role existence by code or name.
     *
     * @param code code of role to check.
     * @param name name of role to check.
     * @return <code>true</code> if role is exists.
     */
    boolean existsByCodeOrName(String code, String name);

}
