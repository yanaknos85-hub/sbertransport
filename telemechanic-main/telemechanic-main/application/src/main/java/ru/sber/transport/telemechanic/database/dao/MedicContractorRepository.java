package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.MedicContractor;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicContractorRepository extends JpaRepository<MedicContractor, UUID> {

    Optional<MedicContractor> findByPersonnelNumber(String personnelNumber);
}
