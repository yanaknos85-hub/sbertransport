package ru.sber.transport.authentication.providers.refresh.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;

import java.util.Optional;

/**
 * Маппер УЗ из бд в бизнес.
 */
@Mapper
public interface SessionMapper {
    
    AccountDto toBusiness(AccountRecord source);
    
    default String toBusiness(RoleRecord role) {
        return Optional.ofNullable(role).map(RoleRecord::getCode).orElse(null);
    }
    
}
