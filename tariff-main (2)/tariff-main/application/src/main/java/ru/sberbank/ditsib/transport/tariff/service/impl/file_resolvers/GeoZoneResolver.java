package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Получатель геоданных.
 */
interface GeoZoneResolver {

    /**
     * Получить имена геозон.
     *
     * @param ids исходный набор идентификаторов.
     * @return сопоставления.
     */
    default Map<UUID, String> geoResolve(@NonNull Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return geoZoneRepository().findAllById(ids).stream()
                .collect(Collectors.toMap(GeoZone::getId, GeoZone::getName));
    }

    /**
     * Получить репозиторий геозон.
     *
     * @return репозиторий геозон.
     */
    @Lookup
    default GeoZoneRepository geoZoneRepository() {
        return null;
    }

}
