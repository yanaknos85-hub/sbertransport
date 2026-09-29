package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.CargoDetails;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью CargoDetails.
 * Предоставляет доступ к данным о грузе в рамках заявки.
 */
@Repository
public interface CargoDetailsRepository extends JpaRepository<CargoDetails, UUID> {

    /**
     * Находит данные о грузе по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     * @return Optional с данными о грузе, если найдены
     */
    Optional<CargoDetails> findByRequestId(UUID requestId);

    /**
     * Проверяет существование данных о грузе по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     * @return true, если запись существует
     */
    boolean existsByRequestId(UUID requestId);

    /**
     * Удаляет данные о грузе по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     */
    void deleteByRequestId(UUID requestId);
}