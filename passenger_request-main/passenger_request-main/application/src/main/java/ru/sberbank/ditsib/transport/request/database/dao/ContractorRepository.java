package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContractorRepository extends JpaRepository<Contractor, UUID> {

    /**
     * Получение списка контрагентов по заявкам
     **/
    @Query("SELECT contractor FROM Contractor contractor INNER JOIN RequestForCarsharing rfc on contractor.id = rfc.contractorId WHERE rfc.id in (:requestIds)")
    List<Contractor> findAllByRequestForCarsh(Collection<UUID> requestIds);

    @Query("SELECT contractor FROM Contractor contractor INNER JOIN RequestForTaxi rft on contractor.id = rft.contractorId WHERE rft.id in (:requestIds)")
    List<Contractor> findAllByRequestForTaxi(Collection<UUID> requestIds);

}
