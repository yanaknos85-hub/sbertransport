package ru.sber.transport.token_generator.providers.database.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.token_generator.database.token_generator.tables.Roles;
import ru.sber.transport.token_generator.providers.database.RolesRepository;

@Repository
class RolesRepositoryImpl implements RolesRepository {

    @Override
    public Roles table() {
        return Roles.ROLES;
    }

}
