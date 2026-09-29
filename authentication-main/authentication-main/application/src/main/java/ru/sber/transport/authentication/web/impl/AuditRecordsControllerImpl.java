package ru.sber.transport.authentication.web.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.authentication.business.use_cases.AuditCases;
import ru.sber.transport.authentication.web.AuditRecordsController;
import ru.sber.transport.authentication.web.mapper.AuditWebMapper;

@RestController
@RequiredArgsConstructor
class AuditRecordsControllerImpl implements AuditRecordsController {
    
    private final AuditCases auditCases;
    
    private final AuditWebMapper mapper;
    
    @Override
    public Iterable<AuditDto> getRecords(Integer size, Integer page) {
        return auditCases.get(size, page).map(mapper::toDto);
    }
}
