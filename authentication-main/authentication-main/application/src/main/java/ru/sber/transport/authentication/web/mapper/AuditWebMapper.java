package ru.sber.transport.authentication.web.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.authentication.business.dto.AuditDto;

@Mapper
public interface AuditWebMapper {
    
    AuditDto toDto(AuditDto source);
    
}
