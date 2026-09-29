package ru.sber.transport.authentication.providers.two_factor.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.authentication.business.dto.TwoFactor;
import ru.sber.transport.database.authentication.tables.records.TwoFactorRecord;

/**
 * Бизнес-маппер второго фактора.
 */
@Mapper
public interface TwoFactorBusinessMapper {

    /**
     * Конвертация объекта БД в бизнес-объект.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "expiration", source = "expire")
    TwoFactor map(TwoFactorRecord source);

}
