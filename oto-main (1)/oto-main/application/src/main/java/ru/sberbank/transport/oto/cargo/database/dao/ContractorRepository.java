package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.Contractor;

import java.util.UUID;

/**
 * Repository for working with contractors.
 */
@Repository
public interface ContractorRepository extends JpaRepository<Contractor, UUID> {
}
