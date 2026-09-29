package ru.sber.transport.trip.messaging.providers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.trip.business.model.Driver;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер водителей.
 */
public interface DriverProvider {

    /**
     * Сохранение
     *
     * @param driver водитель
     */
    int save(Driver driver);

    /**
     * Получить водителя по ID поездки
     * @param tripId ID поездки
     */
    Optional<Driver> getDriverByTripId(UUID tripId);

    /**
     * Получить водителя по ID
     * @param id ID водителя
     */
    Optional<Driver> get(UUID id);

    /**
     * Получить водителя по OauthId
     * @param oauthId ID водителя во внешней системе
     */
    Optional<Driver> getByOauthId(UUID oauthId);

    /**
     * Получить водителя по ID контрагента и ID
     * @param contractorId ID контрагента
     * @param id ID водителя
     */
    Optional<Driver> findByContractorIdAndId(UUID contractorId, UUID id);

    /**
     * Получить водителя по ID контрагента, признаку выхода на смену и признаку активности
     * @param contractorId ID контрагента
     * @param online признак выхода на смену
     * @param active признак активности
     * @param name фио водителя
     * @param autoparkId ID филиала
     */
    List<Driver> findAllByActiveAndOnlineAndContractorId(boolean online, boolean active, UUID contractorId, String name, UUID autoparkId);

    /**
     * Получить водителей по ID контрагента и ID
     * @param contractorId ID контрагента
     * @param online признак выхода на смену
     * @param page страница запроса
     * @param name фио водителя
     * @param autoparkId ID филиала
     */
    Page<Driver> findAllByContractorIdAndOnline(UUID contractorId, boolean online, Pageable page, String name, UUID autoparkId);

    /**
     * Получение списка водителей, у которых смена попадает в заданное значение времени
     * @param startTime дата начала поездки
     * @param endTime дата окончания поездки
     * @param contractorId ID контрагента
     * @param name фио водителя
     * @param autoparkId ID филиала
     */
    List<Driver> getAllDriversByShiftDateIn(LocalDateTime startTime, LocalDateTime endTime, UUID contractorId, String name, UUID autoparkId);

    /**
     * Сохранение данных о геолокации водителя
     * @param driverId ID водителя
     * @param timeZone временная зона
     * @param pointTime время
     * @param latitude широта
     * @param longitude долгота
     * @param azimuth азимут
     */
    void saveLocationData(UUID driverId, String timeZone, OffsetDateTime pointTime, Double latitude, Double longitude, Double azimuth);

    /**
     * @deprecated
     * Поиск водителей, которых надо освободить от зависших поездок (Предназначено только на время локализации дефекта)
     * @return список водителей
     */
    List<Driver> findDriversToLiberate();

    /**
     * Получить список водителей по ID
     * @param ids
     * @return список водителей
     */
    List<Driver> getAllByIds(List<UUID> ids);

}
