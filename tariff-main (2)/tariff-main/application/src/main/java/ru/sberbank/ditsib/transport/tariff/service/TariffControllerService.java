package ru.sberbank.ditsib.transport.tariff.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.tariff.model.BaseTariffDataDto;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;
import ru.sberbank.ditsib.transport.tariff.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Service of controller for working with personal tariffs.
 */
public interface TariffControllerService {
    
    /**
     * Создать новый тариф такси
     *
     * @param newData данные нового тарифа
     *
     * @return созданный тариф
     */
    TaxiTariffDTO addTaxi(NewTaxiTariffDTO newData);
    
    /**
     * Создать новый тариф такси
     *
     * @param newData данные нового тарифа
     *
     * @return созданный тариф
     */
    PersonalTariffDTO addPersonal(NewPersonalTariffDTO newData);
    
    /**
     * Создать новый тариф каршеринга
     *
     * @param newData данные нового тарифа
     * @param organizationId id организации
     *
     * @return созданный тариф
     */
    CarSharingTariffDTO addCarSharing(NewCarSharingTariffDTO newData, UUID organizationId, boolean dataMaster);
    
    /**
     * Создать новый тариф велосипеда
     *
     * @param newData данные нового тарифа
     *
     * @return созданный тариф
     */
    BicycleTariffDTO addBicycle(NewBicycleTariffDTO newData);
    
    /**
     * Создать новый тариф самоката
     *
     * @param newData данные нового тарифа
     *
     * @return созданный тариф
     */
    ScooterTariffDTO addScooter(NewScooterTariffDTO newData);
    
    /**
     * Создать новый тариф общественного транспорта
     *
     * @param newData данные нового тарифа
     *
     * @return созданный тариф
     */
    PublicTariffDTO addPublic(NewPublicTariffDTO newData);
    
    /**
     * Создать новый тариф группового трансфера
     *
     * @param newData данные нового тарифа
     *
     * @return созданный тариф
     */
    GroupTransferTariffDTO addGroupTransfer(NewGroupTransferTariffDTO newData);
    
    /**
     * Edit tariff.
     *
     * @param tariffId ID of tariff to edit.
     * @param newData new data of tariff.
     */
    void edit(UUID transportType, UUID tariffId, NewBaseTariffDto newData);
    
    
    /**
     * Delete tariff.
     *
     * @param tariffId ID of tariff to delete.
     */
    void delete(UUID transportType, UUID tariffId);
    
    /**
     * Get tariff by ID.
     *
     * @param tariffId ID of tariff to get.
     *
     * @return tariff with ID.
     */
    BaseTariffDataDto get(UUID transportType, UUID tariffId);
    
    /**
     * Получить тариф такси
     *
     * @param tariffId ID тарифа
     *
     * @return тариф такси
     */
    TaxiTariffDTO getTaxi(UUID tariffId);
    
    /**
     * Получить тариф личного транспорта
     *
     * @param tariffId ID тарифа
     *
     * @return тариф личного транспорта
     */
    PersonalTariffDTO getPersonal(UUID tariffId);
    
    /**
     * Получить тариф каршеринга
     *
     * @param tariffId ID тарифа
     *
     * @return тариф личного транспорта
     */
    CarSharingTariffDTO getCarSharing(UUID tariffId);
    
    /**
     * Получить тариф велосипеда
     *
     * @param tariffId ID тарифа
     *
     * @return тариф личного транспорта
     */
    BicycleTariffDTO getBicycle(UUID tariffId);
    
    /**
     * Получить тариф самоката
     *
     * @param tariffId ID тарифа
     *
     * @return тариф личного транспорта
     */
    ScooterTariffDTO getScooter(UUID tariffId);
    
    /**
     * Получить тариф общественного транспорта
     *
     * @param tariffId ID тарифа
     *
     * @return тариф общественного транспорта
     */
    PublicTariffDTO getPublic(UUID tariffId);
    
    /**
     * Получить тариф группового трансфера
     *
     * @param tariffId ID тарифа
     *
     * @return тариф группового трансфера
     */
    GroupTransferTariffDTO getGroupTransfer(UUID tariffId);
    
    /**
     * Получение всех тарифов.
     *
     * @param regionId идентификатор региона.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends ShortTariffDto> getAll(UUID regionId, ContractType contractType);
    
    /**
     * Получение всех тарифов типа транспорта.
     *
     * @param transportType тип транспорта.
     * @param regionId идентификатор региона.
     * @param contractType тип договора
     *
     * @return тарифы.
     */
    List<? extends ShortTariffDto> getAll(UUID transportType, UUID regionId, ContractType contractType);
    
    /**
     * Получить список всех тарифов такси
     *
     * @param regionId идентификатор региона.
     *
     * @return коллекция тарифов такси.
     */
    List<TaxiTariffDTO> getAllTaxi(UUID regionId);
    
    /**
     * Получить список всех тарифов личного авто
     *
     * @param regionId идентификатор региона.
     *
     * @return коллекция тарифов такси.
     */
    List<PersonalTariffDTO> getAllPersonal(UUID regionId);
    
    /**
     * Получить список всех тарифов каршеринга
     *
     * @param regionId идентификатор региона.
     *
     * @return коллекция тарифов каршеринга.
     */
    List<CarSharingTariffDTO> getAllCarSharing(UUID regionId);
    
    /**
     * Получить список всех тарифов велосипеда
     *
     * @param regionId идентификатор региона для фильтрации.
     *
     * @return коллекция тарифов велосипеда.
     */
    List<BicycleTariffDTO> getAllBicycle(UUID regionId);
    
    /**
     * Получить список всех тарифов самоката
     *
     * @param regionId идентификатор региона.
     *
     * @return коллекция тарифов самоката.
     */
    List<ScooterTariffDTO> getAllScooter(UUID regionId);
    
    /**
     * Получить список всех тарифов группового трансфера
     *
     * @param regionId идентификатор региона.
     *
     * @return коллекция тарифов группового трансфера.
     */
    List<GroupTransferTariffDTO> getAllGroupTransfer(UUID regionId);
    
    /**
     * Получить список тарифов по фильтру
     *
     * @param searchDTO поисковый dto
     * @param pageable параметры пейджинга
     * @param organizationId id организации сотрудника
     * @param dataMaster true - доступны все КК
     *
     * @return список найденных тарифов
     */
    Page<ShortTariffDto> search(TariffSearchDTO searchDTO, UUID organizationId, boolean dataMaster, Pageable pageable);
    
    /**
     * Получить список всех тарифов общественного транспорта
     *
     * @param regionId идентификатор региона.
     *
     * @return коллекция тарифов общественного транспорта
     */
    List<PublicTariffDTO> getAllPublic(UUID regionId);
}
