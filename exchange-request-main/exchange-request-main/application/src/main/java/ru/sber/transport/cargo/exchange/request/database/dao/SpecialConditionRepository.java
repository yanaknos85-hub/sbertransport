package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.SpecialConditions;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с особыми условиями перевозки.
 * Предоставляет доступ к данным таблицы special_condition.
 */
@Repository
public interface SpecialConditionRepository extends JpaRepository<SpecialConditions, UUID> {

    /**
     * Находит особые условия по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     * @return Optional с особыми условиями, если найдены
     */
    Optional<SpecialConditions> findByRequestId(UUID requestId);

    /**
     * Проверяет, существуют ли особые условия для указанной заявки.
     *
     * @param requestId идентификатор заявки
     * @return true, если запись существует
     */
    boolean existsByRequestId(UUID requestId);

    /**
     * Удаляет особые условия по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     */
    void deleteByRequestId(UUID requestId);
}
