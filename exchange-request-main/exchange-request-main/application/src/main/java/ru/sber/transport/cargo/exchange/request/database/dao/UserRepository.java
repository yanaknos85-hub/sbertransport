package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.User;

import java.util.UUID;

/**
 * Репозиторий для доступа к данным пользователей в схеме exchange_request.
 * <p>
 * Предоставляет стандартные CRUD-операции через JpaRepository,
 * а также кастомный метод для поиска пользователя по уникальному идентификатору токена (token_id).
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> { }
