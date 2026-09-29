package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Tin;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TinRepository extends CrudRepository<Tin, UUID> {
    Optional<Tin> findByEmployeeId(UUID employeeId);
}
