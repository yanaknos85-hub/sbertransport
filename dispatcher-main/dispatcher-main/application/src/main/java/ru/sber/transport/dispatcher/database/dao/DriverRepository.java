package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.database.model.Driver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with drivers.
 */
public interface DriverRepository extends JpaRepository<Driver, UUID>, JpaSpecificationExecutor<Driver> {

    /**
     * Получение водителей по контрагенту и идентификатору
     *
     * @param contractorId идентификатор контрагента.
     * @param driverId идентификатор водителя.
     * @return водители.
     */
    @Query("SELECT driver FROM Driver driver INNER JOIN driver.contractor contractor " +
            "WHERE contractor.id = :contractorId AND driver.id = :driverId ")
    Optional<Driver> findByContractorAndId(UUID contractorId, UUID driverId);

    /**
     * Посчитать количество водителей контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @return число.
     */
    int countAllByContractorId(UUID contractorId);


    /**
     * Проверка существования водителя по паспорту и признаку активности.
     * @param passport паспорт водителя.
     * @return результат проверки.
     */
    boolean existsByPassportAndActiveTrue(String passport);

    /**
     * Проверка существования водителя по паспорту и признаку активности исключая идентификатор.
     * @param passport паспорт водителя.
     * @return результат проверки.
     */
    boolean existsByPassportAndActiveTrueAndIdNot(String passport, UUID id);

    /**
     * Проверка существования водителя по ВУ и признаку активности.
     * @param driverLicenseNumber ВУ.
     * @return результат проверки.
     */
    boolean existsByDriverLicenseNumberAndActiveTrue(String driverLicenseNumber);

    /**
     * Проверка существования водителя по ВУ и признаку активности исключая идентификтор.
     * @param driverLicenseNumber ВУ.
     * @return результат проверки.
     */
    boolean existsByDriverLicenseNumberAndActiveTrueAndIdNot(String driverLicenseNumber, UUID id);

    /**
     * Проверка существования водителя по СНИЛС и признаку активности.
     * @param snils СНИЛС.
     * @return результат проверки.
     */
    boolean existsBySnilsAndActiveTrue(String snils);

    /**
     * Проверка существования водителя по СНИЛС и признаку активности исключая идентификтор.
     * @param snils СНИЛС.
     * @return результат проверки.
     */
    boolean existsBySnilsAndActiveTrueAndIdNot(String snils, UUID id);

    /**
     * Проверка существования водителя по ИНН и признаку активности.
     * @param tin ИНН.
     * @return результат проверки.
     */
    boolean existsByTinAndActiveTrue(String tin);

    /**
     * Проверка существования водителя по ИНН и признаку активности исключая идентификтор.
     * @param tin ИНН.
     * @return результат проверки.
     */
    boolean existsByTinAndActiveTrueAndIdNot(String tin, UUID id);

    /**
     * Поиск водителя по идентификатору OAuth.
     * @param oauthId идентификатор OAuth.
     * @return водитель.
     */
    Optional<Driver> findByOauthId(UUID oauthId);

    /**
     * Получение списка водителей по контрагенту.
     */
    List<Driver> findAllByContractorId(UUID contractorId);

    /**
     * Поиск водителся по табельному номеру
     * @param personnelNumber табельный номер
     * @return водиль
     */
    Optional<Driver> findByPersonnelNumberIgnoreCaseAndActiveTrue(String personnelNumber);
}
