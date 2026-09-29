package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.transport.oto.cargo.database.model.Routelist;

import java.util.UUID;

public interface RouteRepository extends JpaRepository<Routelist, UUID> {
}
