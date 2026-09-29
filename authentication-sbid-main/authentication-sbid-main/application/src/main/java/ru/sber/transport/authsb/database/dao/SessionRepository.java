package ru.sber.transport.authsb.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.authsb.database.model.Session;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сессиями авторизации.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByStateAndActiveTrue(String state);

    Optional<Session> findByState(String state);

    Optional<Session> findAllByRefreshTokenAndActiveTrue(String refreshToken);
}
