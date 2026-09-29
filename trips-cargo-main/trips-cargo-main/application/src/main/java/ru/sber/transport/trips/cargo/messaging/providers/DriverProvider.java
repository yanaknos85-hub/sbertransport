package ru.sber.transport.trips.cargo.messaging.providers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.trips.cargo.business.model.Driver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер водителей.
 */
public interface DriverProvider {

    /**
     * Сохранение
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
     * @param autoparkId ID автопарка
     * @param online признак выхода на смену
     * @param active признак активности
     */
    List<Driver> findAllByActiveAndOnlineAndContractorIdAndAutoparkId(boolean online, boolean active, UUID contractorId, UUID autoparkId);

    /**
     * Получить водителей по ID контрагента и ID
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param online признак выхода на смену
     * @param page страница запроса
     */
    Page<Driver> findAllByContractorIdAndOnline(UUID contractorId, UUID autoparkId, boolean online, Pageable page);

    /**
     * Получение списка водителей, у которых смена попадает в заданное значение времени
     * @param time дата
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     */
    List<Driver> getAllDriversByShiftDateIn(LocalDateTime time, UUID contractorId, UUID autoparkId);

}
