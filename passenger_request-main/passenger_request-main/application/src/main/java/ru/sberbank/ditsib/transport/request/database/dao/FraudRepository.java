package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с данными фрода.
 */
public interface FraudRepository extends JpaRepository<FraudData, UUID> {

    /**
     * Получить данные фрода по списку идентификаторов запросов.
     *
     * @param requests список идентификаторов запросов
     * @return список данных фрода
     */
    List<FraudData> findAllByRequestIdIn(List<UUID> requests);

    /**
     * Проверить, существует ли данные фрода по идентификатору запроса и типу.
     *
     * @param requestId идентификатор заявки
     * @param type тип фрода
     * @return true, если данные фрода существуют, иначе false
     */
    boolean existsByRequestIdAndType(UUID requestId, FraudType type);
}
