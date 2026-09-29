package ru.sberbank.ditsib.transport.tariff.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.dto.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Сервис по работе с тарифами.
 */
public interface TariffService {
    
    /**
     * Сохранить тариф.
     *
     * @param tariff тариф для сохранения.
     *
     * @return сохраненный тариф.
     */
    BaseTariff save(BaseTariff tariff);
    
    /**
     * Удалить тариф.
     *
     * @param tariff тариф для удаления.
     */
    void delete(BaseTariff tariff);
    
    Optional<TaxiTariff> findByDepartmentIdAndTransportTypeAndTaxiClassAndRegionIdAndActive(
            UUID departmentID, TransportTypeEnum transportTypeEnum, TaxiClass taxiClass, UUID regionId, boolean active
                                                                                           );
    
    /**
     * Получение тарифа по идентификатору.
     *
     * @param transportType вид транспорта.
     * @param tariffId идентификатор тарифа.
     *
     * @return тариф.
     */
    Optional<? extends BaseTariff> get(UUID transportType, UUID tariffId);
    
    /**
     * Получение тарифа по идентификатору.
     *
     * @param transportType вид транспорта.
     * @param tariffId идентификатор тарифа.
     *
     * @return тариф.
     */
    Optional<? extends BaseTariff> get(TransportTypeEnum transportType, UUID tariffId);
    
    Optional<Department> getDepartmentByHumanReadableId(String humanReadableId);
    
    Optional<Department> getDepartmentById(UUID id);
    
    BaseTariff getById(UUID tariffId);
    
    Optional<BaseTariff> findById(UUID tariffId);
    
    /**
     * Получение тарифов.
     *
     * @param regionId идентификатор региона.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(UUID regionId, ContractType contractType);
    
    /**
     * Получение тарифов.
     *
     * @param regionId идентификатор региона
     * @param isActive Флаг активности. Отображение только активных тарифов
     * @param contractType тип договора
     *
     * @return тарифы
     */
    List<? extends BaseTariff> getAll(UUID regionId, Boolean isActive, ContractType contractType);
    
    /**
     * Получение тарифов.
     *
     * @param regionId идентификатор региона.
     * @param organizationId идентификатор организации.
     * @param isActive Флаг активности. Отображение только активных тарифов.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(UUID regionId, UUID organizationId, Boolean isActive, ContractType contractType);
    
    /**
     * Получение тарифов по виду транспортной услуги.
     *
     * @param regionId идентификатор региона.
     * @param organizationId идентификатор организации.
     * @param isActive Флаг активности. Отображение только активных тарифов.
     * @param type - `тип - грузы/пассажирка
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(
            UUID regionId, UUID organizationId, Boolean isActive, TransportServiceType type, List<TransportTypeEnum> transportTypeList,
            ContractType contractType
                                     );
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(UUID transportType, UUID regionId, ContractType contractType);
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(TransportTypeEnum transportType, UUID regionId, ContractType contractType);
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param organizationId идентификатор организации для получения тарифов.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(TransportTypeEnum transportType, UUID regionId, UUID organizationId, ContractType contractType);
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param organizationId идентификатор организации для получения тарифов.
     * @param isActive флаг активности
     * @param contractorId идентификатор контрагента
     * @param humanReadableId человеко читаемый идентификатор
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, UUID regionId, UUID organizationId, Boolean isActive,
            UUID contractorId, String humanReadableId, ContractType contractType
                                     );
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param organizationId идентификатор организации для получения тарифов.
     * @param isActive флаг активности
     * @param contractorId идентификатор контрагента
     * @param humanReadableId человеко читаемый идентификатор
     * @param isNightTariff признак необходимости поиска только ночных тарифов
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, List<UUID> regionId, UUID organizationId, Boolean isActive,
            UUID contractorId, String humanReadableId, Boolean isNightTariff, ContractType contractType
                                     );
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param organizationId идентификатор организации для получения тарифов.
     * @param isActive флаг активности
     * @param contractorId идентификатор контрагента
     * @param humanReadableId человеко читаемый идентификатор
     * @param isNightTariff признак необходимости поиска только ночных тарифов
     * @param contractNumber номер контракта
     * @param transportClass класс транспорта
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, List<UUID> regionId, UUID organizationId, Boolean isActive,
            UUID contractorId, String humanReadableId, Boolean isNightTariff, String contractNumber, TransportClass transportClass,
            ContractType contractType
                                     );
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionId идентификатор региона.
     * @param transportType идентификатор типа транспорта.
     * @param organizationId идентификатор организации для получения тарифов.
     * @param isActive флаг активности
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, UUID regionId, UUID organizationId, Boolean isActive, ContractType contractType
                                     );
    
    
    List<TaxiTariff> getDepartmentUniqueTariff(UUID departmentId, TransportTypeEnum transportType);
    
    /**
     * Получение тарифов по типу транспорта.
     *
     * @param regionIds идентификаторы регионов.
     * @param transportType идентификатор типа транспорта.
     * @param organizationId идентификатор организации для получения тарифов.
     * @param isActive флаг активности
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, List<UUID> regionIds, UUID organizationId, Boolean isActive, ContractType contractType
                                     );
    
    /**
     * Получить список тарифов по фильтру
     *
     * @param searchDTO поисковый  dto
     * @param organizationId id организации сотрудника
     * @param dataMaster true - доступны все КК, false - только КК пользователя (organizationId)
     * @param pageable параметры пейджинга
     *
     * @return список найденных тарифов
     */
    Page<? extends BaseTariff> search(TariffSearchDTO searchDTO, UUID organizationId, boolean dataMaster, Pageable pageable);
    
    /**
     * Получить список тарифов по фильтру
     *
     * @param searchDTO поисковый  dto
     * @param pageable параметры пейджинга
     *
     * @return список найденных тарифов
     */
    Page<? extends BaseTariff> search(TariffSearchDTO searchDTO, Pageable pageable);
    
    /**
     * Получить тариф по типу и человекочитаемому идентификатору.
     *
     * @param transportType тип транспорта.
     * @param humanReadableId идентификатор.
     *
     * @return тариф.
     */
    Optional<? extends BaseTariff> get(TransportTypeEnum transportType, String humanReadableId);
    
    WorkGroup getWorkgroup(BaseTariffWithContract tariff);
    
    TaxiTariff newDtoToTaxiTariff(NewTaxiTariffDTO source);
    
    TaxiTariffDTO taxiTariffToDTO(TaxiTariff source);
    
    /**
     * Проверка на возможность формирования отчёта для данных фильтров и типа транспорта (по которому будет сгенерирован отчёт)
     *
     * @param dto фильтры
     * @param transportType тип транспорта
     *
     * @return true - если отчёт должен быть сгенерирован
     */
    boolean canReportBeGenerated(@NonNull TariffSearchDTO dto, @NonNull TransportTypeEnum transportType);
    
    Map<UUID, List<GroupTransferTariff>> findConflictTariff(
            LocalDate tariffStartDate, LocalDate tariffEndDate, UUID contractorId, UUID organizationId,
            Set<UUID> regionIds, Set<UUID> transportIds
                                                           );
    
    /**
     * Повторная отправка всех тарифов
     */
    void resend();
}
