package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.VehicleRequirements;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с требованиями к транспорту.
 * Предоставляет методы для доступа к данным таблицы vehicle_requirements.
 */
@Repository
public interface VehicleRequirementsRepository extends JpaRepository<VehicleRequirements, UUID> {

    /**
     * Находит требования к транспорту по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     * @return Optional с требованиями, если найдены
     */
    Optional<VehicleRequirements> findByRequestId(UUID requestId);

    /**
     * Проверяет, существуют ли требования к транспорту для указанной заявки.
     *
     * @param requestId идентификатор заявки
     * @return true, если запись существует
     */
    boolean existsByRequestId(UUID requestId);

    /**
     * Удаляет требования к транспорту по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     */
    void deleteByRequestId(UUID requestId);
}
