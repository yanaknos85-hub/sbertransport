package ru.sber.transport.driver_track.repository;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.driver_track.database.driver_track.tables.DriverMessage;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;

import java.util.UUID;

/**
 * Репозиторий для работы с водителями.
 */
public interface DriverRepository extends JooqRepository<DriverMessage, DriverMessageRecord, UUID> {

    /**
     * Поиск водителя по идентификатору с проверкой нахождения.
     *
     * @param id идентификатор пользователя.
     * @return водитель.
     */
    DriverMessageRecord getByIdNotNull(UUID id);
}
