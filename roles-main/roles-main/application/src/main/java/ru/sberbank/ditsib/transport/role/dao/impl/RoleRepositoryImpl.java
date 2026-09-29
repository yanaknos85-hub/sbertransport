package ru.sberbank.ditsib.transport.role.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.roles.database.roles.Tables;
import ru.sber.transport.roles.database.roles.tables.Role;
import ru.sberbank.ditsib.transport.role.dao.RoleRepository;

@Transactional
@Repository
@RequiredArgsConstructor
class RoleRepositoryImpl implements RoleRepository {

    private static final Role TABLE = Tables.ROLE;

    @Override
    public boolean existsByCodeOrName(String code, String name) {
        var select = context().selectFrom(TABLE)
                .where(TABLE.CODE.equalIgnoreCase(code).or(TABLE.NAME.equalIgnoreCase(name)));
        return context().fetchExists(select);
    }

    @Override
    public Role table() {
        return Role.ROLE;
    }
}
