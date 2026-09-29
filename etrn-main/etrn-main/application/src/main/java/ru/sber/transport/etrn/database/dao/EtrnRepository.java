package ru.sber.transport.etrn.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.etrn.database.model.Etrn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EtrnRepository extends JpaRepository<Etrn, UUID>, JpaSpecificationExecutor<Etrn> {

    Optional<Etrn> findByHumanReadableId(String humanReadableId);

    boolean existsByHumanReadableId(String humanReadableId);

    @Query(value = "SELECT * FROM etrn_cargo.etrn " +
            "WHERE lock_info is not null " +
            "and (lock_info ->> 'lockUntil')::timestamp < now()",
            nativeQuery = true)
    List<Etrn> findAllWithExpiredLockingTime();
}