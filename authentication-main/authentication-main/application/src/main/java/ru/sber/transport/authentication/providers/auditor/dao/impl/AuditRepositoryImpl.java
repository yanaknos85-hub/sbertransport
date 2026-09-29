package ru.sber.transport.authentication.providers.auditor.dao.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.database.authentication.tables.Audit;
import ru.sber.transport.authentication.providers.auditor.dao.AuditRepository;

@Repository
public class AuditRepositoryImpl implements AuditRepository {
    
    @Override
    public Audit table() {
        return Audit.AUDIT;
    }

}
