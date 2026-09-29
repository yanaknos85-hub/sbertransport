package ru.sber.transport.notifications.database.dao.messages.limits;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.limits.Limit;

import java.util.UUID;

/**
 * Репозиторий для работы с лимитами.
 */
public interface LimitRepository extends JpaRepository<Limit, UUID> {
    
    /**
     * Поиск лимитов по подразделению и типу транспорта.
     *
     * @param departmentId идентификатор подразделения.
     * @param type тип транспорта.
     * @param year год лимита.
     * @param pageRequest страница.
     * @return лимит.
     */
    Page<Limit> findByDepartmentIdAndTransportTypeAndYear(UUID departmentId, String type, Integer year, Pageable pageRequest);
}
