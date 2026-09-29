package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.telemechanic.database.model.Attorney;

import java.util.UUID;

public interface AttorneyRepository extends JpaRepository<Attorney, UUID> {}
