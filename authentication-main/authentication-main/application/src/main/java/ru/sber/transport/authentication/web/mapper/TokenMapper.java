package ru.sber.transport.authentication.web.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.authentication.web.model.Token;

/**
 * Маппинг токенов из бизнес в веб.
 */
@Mapper
public interface TokenMapper {
    
    Token toDto(ru.sber.transport.authentication.business.dto.Token source);
    
}
