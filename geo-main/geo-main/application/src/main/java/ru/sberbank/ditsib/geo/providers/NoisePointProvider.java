package ru.sberbank.ditsib.geo.providers;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.geo.database.geo.tables.NoisePoint;
import ru.sber.transport.geo.database.geo.tables.records.NoisePointRecord;

import java.util.UUID;

/**
 * Провайдер для работы с "шумными" точками.
 */
public interface NoisePointProvider extends JooqRepository<NoisePoint, NoisePointRecord, UUID> {
}
