package ru.sber.transport.authentication.providers.role.dao;

import lombok.NonNull;
import org.jooq.JSON;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.authentication.tables.Role;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.authentication.providers.role.model.Scope;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * repository for working with roles.
 */
public interface RoleRepository extends JooqRepository<Role, RoleRecord, String> {
    
    /**
     * Get roles by code.
     *
     * @return list of roles.
     */
    List<RoleRecord> findAllByCode(@NonNull Set<String> codeSet);


    List<RoleRecord> findAllByAccountId(UUID id);

    Collection<Scope> getDefaultFor(JSON defaultFor);

    void add(UUID accountId, RoleRecord roleCode);

    void clearRoles(UUID id);

    void clearRole(String roleCode);
}
