package ru.sber.transport.authentication.providers.auditor.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.database.authentication.tables.records.AuditRecord;

@Mapper
public interface AuditMapper {
    
    AuditDto toBusiness(AuditRecord audit);
    
}
