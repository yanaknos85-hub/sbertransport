package ru.sberbank.ditsib.geo.providers;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.geo.database.geo.tables.Cluster;
import ru.sber.transport.geo.database.geo.tables.records.ClusterRecord;

import java.util.UUID;

/**
 * Провайдер для работы с кластерами.
 */
public interface ClusterProvider extends JooqRepository<Cluster, ClusterRecord, UUID> {
}
