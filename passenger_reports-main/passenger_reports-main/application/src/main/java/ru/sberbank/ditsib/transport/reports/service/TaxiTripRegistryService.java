package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.RegistryPerContractorDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryShortDTO;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис импорта реестров поездок на такси от контрагента
 */
public interface TaxiTripRegistryService {
    
    /**
     * Найти загруженный Реестр за конкретную дату
     * @param contractorId ID контрагента
     * @param date дата
     * @return данные реестра TaxiTripRegistryDTO или null
     */
    TaxiTripRegistryDTO findRegistry(UUID contractorId, LocalDate date);
    
    /**
     * Найти все загруженные Реестры для контрагента
     * @param contractorId ID контрагента
     * @return список реестров TaxiTripRegistryDTOWithoutContractor
     */
    List<TaxiTripRegistryShortDTO> findAllRegistries(UUID contractorId);
    
    /**
     * Найти все загруженные Реестры для всех контрагентов
     * @return набор реестров RegistryPerContractorDTO
     */
    Set<RegistryPerContractorDTO> findAllRegistries();
    
    /**
     * Импорт реестра их файла Excel в БД
     * @param registryDTO данные о новом реестре
     * @param file MultipartFile - файл excel с реестром поездок от контрагента
     * @return данные записанного реестра TaxiTripRegistryDTO
     */
    TaxiTripRegistryDTO importRegistry(NewTaxiTripRegistryDTO registryDTO, MultipartFile file);
    
    /**
     * Замена реестра в БД путем импорта файла Excel
     * @param registryId ID заменяемого реестра
     * @param file MultipartFile - файл excel с реестром поездок от контрагента
     * @return данные записанного реестра TaxiTripRegistryDTO
     */
    TaxiTripRegistryDTO replaceRegistry(UUID registryId, MultipartFile file);
    
    /**
     * Выгрузка раннее импортированного реестра из файла Excel
     * @param contractorId ID контрагента
     * @param year - год
     * @param month - месяц
     * @return раннее загруженый файл
     */
    Resource downloadFileAsResource(UUID contractorId, Integer year, Integer month);

    List<TaxiTripRegistry> findAllBySpec(Specification<TaxiTripRegistry> spec);
}
