package ru.sber.transport.driver_track.repository;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.driver_track.database.driver_track.tables.Coordinate;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с координатами.
 */
public interface CoordinateRepository extends JooqRepository<Coordinate, CoordinateRecord, UUID> {

    /**
     * Поиск всех координат по трипу, отсоритрованных по возрастанию времени.
     *
     * @param tripId идентификатор трипа.
     * @return список координат.
     */
    List<CoordinateRecord> findCoordinatesByTripId(UUID tripId);

    /**
     * Удаление всех координат по трипу.
     *
     * @param tripId идентификатор трипа.
     */
    void deleteAllByTripId(UUID tripId);

    /**
     * Удаление всех координат по списку поездок.
     *
     * @param tripIds список идентификаторов воездок.
     */
    void deleteAllByTripIdList(List<UUID> tripIds);
}
