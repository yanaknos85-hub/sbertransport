package ru.sberbank.ditsib.geo.providers;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.geo.database.geo.tables.CachedSuggestRequest;
import ru.sber.transport.geo.database.geo.tables.records.CachedSuggestRequestRecord;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер для работы с сохраненными запросами suggest.
 */
public interface CachedSuggestRequestProvider extends JooqRepository<CachedSuggestRequest, CachedSuggestRequestRecord, UUID> {

    /**
     * Поиск сохраненного запроса.
     *
     * @param clusterId идентификатор кластера.
     * @param query     текст запроса.
     * @return сохраненный запрос.
     */
    Optional<CachedSuggestRequestRecord> findByQuery(UUID clusterId, String query);
}
