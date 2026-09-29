package ru.sber.transport.authentication.providers.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;

/**
 * Маппер УЗ из БД в бизнес.
 */
@Mapper
public interface AccountMapper {

    AccountDto toBusiness(AccountRecord source);

    void toModel(
            @MappingTarget AccountRecord target,
            AccountDto source
    );

    void update(@MappingTarget AccountRecord toSave, AccountRecord accountRecord);
}
