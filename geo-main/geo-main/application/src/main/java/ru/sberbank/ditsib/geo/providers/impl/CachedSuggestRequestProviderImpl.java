package ru.sberbank.ditsib.geo.providers.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.geo.database.geo.tables.CachedSuggestRequest;
import ru.sber.transport.geo.database.geo.tables.records.CachedSuggestRequestRecord;
import ru.sberbank.ditsib.geo.providers.CachedSuggestRequestProvider;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class CachedSuggestRequestProviderImpl implements CachedSuggestRequestProvider {

    @Override
    public Optional<CachedSuggestRequestRecord> findByQuery(UUID clusterId, String query) {
        return context()
                .selectFrom(table())
                .where(table().CLUSTER_ID.eq(clusterId)
                        .and(table().QUERY.eq(query.trim().toLowerCase(Locale.ROOT))))
                .fetchOptional();
    }

    @Override
    public CachedSuggestRequest table() {
        return CachedSuggestRequest.CACHED_SUGGEST_REQUEST;
    }
}
