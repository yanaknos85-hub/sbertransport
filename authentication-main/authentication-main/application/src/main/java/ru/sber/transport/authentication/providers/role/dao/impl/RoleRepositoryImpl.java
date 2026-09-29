package ru.sber.transport.authentication.providers.role.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.authentication.tables.AccountRoles;
import ru.sber.transport.database.authentication.tables.Role;
import ru.sber.transport.database.authentication.tables.records.AccountRolesRecord;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.authentication.providers.role.model.Scope;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Transactional
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final ObjectMapper objectMapper;

    @Override
    public List<RoleRecord> findAllByCode(@NonNull Set<String> codeSet) {
        return context().selectFrom(table()).where(table().CODE.in(codeSet)).fetchInto(RoleRecord.class);
    }

    @Override
    public List<RoleRecord> findAllByAccountId(UUID id) {
        var roleAccount = AccountRoles.ACCOUNT_ROLES;
        return context().select().from(table())
                .innerJoin(roleAccount).on(table().CODE.eq(roleAccount.ROLE_CODE))
                .where(roleAccount.ACCOUNT_ID.eq(id))
                .fetchInto(RoleRecord.class);
    }

    @SneakyThrows(JsonProcessingException.class)
    @Override
    public Collection<Scope> getDefaultFor(JSON defaultFor) {
        return objectMapper.readValue(defaultFor.data(), new TypeReference<>() {});
    }

    @Override
    public void add(UUID accountId, RoleRecord roleCode) {
        var accountRole = AccountRoles.ACCOUNT_ROLES;

        var accountRoleRecord = new AccountRolesRecord();
        accountRoleRecord.setAccountId(accountId);
        accountRoleRecord.setRoleCode(roleCode.getCode());

        context().insertInto(accountRole)
                .set(accountRoleRecord)
                .onConflict().doNothing().execute();
    }

    @Override
    public void clearRoles(UUID accountId) {
        var accountRole = AccountRoles.ACCOUNT_ROLES;
        context().deleteFrom(accountRole)
                .where(accountRole.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    @Override
    public Role table() {
        return Role.ROLE;
    }

    @Override
    public void clearRole(String roleCode) {
        var accountRole = AccountRoles.ACCOUNT_ROLES;
        context().deleteFrom(accountRole).where(accountRole.ROLE_CODE.eq(roleCode)).execute();
    }
}
