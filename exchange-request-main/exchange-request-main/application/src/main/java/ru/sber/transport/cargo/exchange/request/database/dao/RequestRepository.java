package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Репозиторий для управления сущностью Request.
 * Предоставляет CRUD-операции и поддержку динамических запросов через Specification.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>, JpaSpecificationExecutor<Request> {
    /**
     * Находит все заявки по идентификатору владельца (пользователя-создателя).
     *
     * @param ownerId идентификатор пользователя — владельца заявок
     * @return список заявок, принадлежащих пользователю
     */
    List<Request> findByOwnerId(UUID ownerId);

    @EntityGraph("requestFull")
    Optional<Request> findFullRequestById(UUID id);

    @EntityGraph("requestFull")
    Optional<Request> getByIdAndOwnerId(UUID id, UUID ownerId);

    @Override
    @NonNull
    @EntityGraph("requestFull")
    Page<Request> findAll(@Nullable Specification<Request> spec, @NonNull Pageable pageable);

    Stream<Request> getByStatusAndExpiresAtBefore(RequestStatus status, LocalDateTime expiresAt);

    long deleteByStatusAndExpiresAtBefore(RequestStatus status, LocalDateTime expiresAt);
}

