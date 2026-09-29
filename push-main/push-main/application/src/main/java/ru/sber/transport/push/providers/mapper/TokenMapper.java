package ru.sber.transport.push.providers.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.database.push.tables.records.TokenRecord;

import java.util.UUID;

@Mapper
public interface TokenMapper {

    TokenRecord toRecord(UUID recipientId, TokenData tokenData);

    TokenData toModel(TokenRecord tokenRecord);
}
