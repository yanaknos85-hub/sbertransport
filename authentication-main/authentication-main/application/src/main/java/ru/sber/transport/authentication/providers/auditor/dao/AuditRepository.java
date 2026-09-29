package ru.sber.transport.authentication.providers.auditor.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.authentication.tables.Audit;
import ru.sber.transport.database.authentication.tables.records.AuditRecord;

import java.time.LocalDateTime;

public interface AuditRepository extends JooqRepository<Audit, AuditRecord, LocalDateTime> {
}
