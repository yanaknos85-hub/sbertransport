package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.Waypoint;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Waypoint.
 * Предоставляет CRUD-операции и стандартные методы поиска.
 */
@Repository
public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {

    /**
     * Находит все точки маршрута по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     * @return список точек маршрута, отсортированный по orderingIndex
     */
    List<Waypoint> findByRequestIdOrderByOrderingIndexAsc(UUID requestId);

    /**
     * Проверяет существование точек маршрута по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     * @return true, если есть хотя бы одна точка маршрута
     */
    boolean existsByRequestId(UUID requestId);

    /**
     * Удаляет все точки маршрута по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     */
    void deleteByRequestId(UUID requestId);
}
