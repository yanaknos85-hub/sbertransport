package ru.sber.transport.authentication.providers.refresh.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.authentication.tables.Session;
import ru.sber.transport.database.authentication.tables.records.SessionRecord;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for persisting sessions.
 */
public interface SessionRepository extends JooqRepository<Session, SessionRecord, UUID> {
    
    /**
     * Поиск сессии по токену.
     *
     * @param accessToken токен доступа.
     *
     * @return сессия.
     */
    Optional<SessionRecord> findByToken(String accessToken);
    
    /**
     * Поиск сессии по токену не позднее времени.
     *
     * @param accessToken токен доступа.
     * @param expiration текущее время для проверки истечения.
     *
     * @return сессия.
     */
    Optional<SessionRecord> findByTokenAndExpiredAtBefore(String accessToken, LocalDateTime expiration);
    
    /**
     * Поиск всех сессий УЗ.
     *
     * @param login логин для поиска сессий.
     * @param expiration текущее время для проверки истечения.
     * @return сессии.
     */
    List<SessionRecord> findAllByAccountAndExpiredAtAfter(String login, LocalDateTime expiration);
    
    /**
     * Поиск неистекшей сессии.
     *
     * @param id идентификатор.
     * @param expiration время для проверки.
     * @return неистёкшая сессия.
     */
    Optional<SessionRecord> findByIdAndExpiredAtAfter(UUID id, LocalDateTime expiration);
    
    List<SessionRecord> findAllByExpiredAtBefore(LocalDateTime now);

    Collection<SessionRecord> findAllByAccountInAndExpiredAtAfter(List<String> logins, LocalDateTime expiration);
}
