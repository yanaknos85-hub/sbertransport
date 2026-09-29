package ru.sber.transport.authentication.providers.reset.dao;

import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.authentication.tables.ResetCode;
import ru.sber.transport.database.authentication.tables.records.ResetCodeRecord;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetCodeRepository extends JooqRepository<ResetCode, ResetCodeRecord, UUID> {

    Optional<ResetCodeRecord> findByEmail(String email);

}
