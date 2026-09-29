package ru.sberbank.ditsib.transport.reports.service.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dto.DriverReportFiltersDTO;
import ru.sberbank.ditsib.transport.reports.dto.DriverUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.driversData.Driver;
import ru.sberbank.ditsib.transport.reports.service.DriverXlsxExporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.sberbank.ditsib.transport.reports.service.impl.DriverXlsxExporterImpl.ALL_COLUMN_NAMES.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class DriverXlsxExporterImpl implements DriverXlsxExporter {
    
    interface ALL_COLUMN_NAMES {
        String CONTRACTOR_NAME = "Наименование контрагента";
        String DRIVER_ID = "ID Водителя";
        String FIRST_NAME = "Имя";
        String LAST_NAME = "Фамилия";
        String PATRONYMIC = "Отчество";
        String AUTOPARK = "Автопарк";
        String CONTACT_NUMBER = "Контактный номер";
        String LICENSE_AVAILABILITY = "Наличие лицензии на оказание услуг по пассажирским перевозкам";
        String LICENSE_NUMBER = "Номер лицензии";
        String PASSPORT = "Серия, номер паспорта";
        String DRIVER_LICENSE_CATEGORY = "Категория прав";
        String DRIVER_LICENSE_NUMBER = "Серийный номер водительского удостоверения";
        String DRIVER_TAG = "Признак водителя";
        String DRIVER_EXPIRIENCE = "Опыт вождения";
        String DRIVER_STATUS = "Статус водителя";
        String DRIVER_RAITING = "Рейтинг водителя";
    }
    
    static String NA = "Н/Д";
    
    @FunctionalInterface
    interface Function4<ONE, TWO, THREE, FOUR> {
        FOUR apply(ONE request, TWO driver, THREE dto);
    }
    
    static LinkedHashMap<String, Function4<Request, Driver, DriverReportFiltersDTO, String>>
            DEFAULT_MAPPING = new LinkedHashMap<>();
    
    private static LinkedHashMap<String, Function4<Request, Driver, DriverReportFiltersDTO, String>>
            DRIVER_MAPPING = new LinkedHashMap<>();
    
    static {
        DEFAULT_MAPPING.put(CONTRACTOR_NAME, (r, driver, dto) -> {
            if (r.getContractor() != null && r.getContractor().getName() != null) {
                return r.getContractor().getName();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(DRIVER_ID, (r, driver, dto) -> {
            if (r.getDriver() != null) {
                return r.getDriver().getId().toString();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(AUTOPARK, (r, driver, dto) -> {
            if (r.getAutopark() != null) {
                return r.getAutopark().getAutoparkName();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(FIRST_NAME, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getFirstName() != null) {
                return r.getDriver().getFirstName();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(LAST_NAME, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getLastName() != null) {
                return r.getDriver().getLastName();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(PATRONYMIC, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getPatronymic() != null) {
                return r.getDriver().getPatronymic();
            } else {
                return NA;
            }
        });
        
        DEFAULT_MAPPING.put(CONTACT_NUMBER, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getContactPhone() != null) {
                return r.getDriver().getContactPhone();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(LICENSE_AVAILABILITY, (r, driver, dto) -> {
            if (r.getDriver() != null) {
                if (!"".equals(driver.getDriverLicenseNumber())) {
                    return "Да";
                } else {
                    return "Нет";
                }
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(LICENSE_NUMBER, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getServiceProviderLicenseNumber() != null) {
                return r.getDriver().getServiceProviderLicenseNumber();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(PASSPORT, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getPassport() != null) {
                return r.getDriver().getPassport();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(DRIVER_LICENSE_CATEGORY, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getLicenseClasses() != null) {
                return String.join(", ", r.getDriver().getLicenseClasses());
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(DRIVER_LICENSE_NUMBER, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getDriverLicenseNumber() != null) {
                return r.getDriver().getDriverLicenseNumber();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(DRIVER_TAG, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getTags() != null) {
                return String.join(", ", r.getDriver().getTags());
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(DRIVER_EXPIRIENCE, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getExperience() != null) {
                return r.getDriver().getExperience();
            } else {
                return NA;
            }
        });
        DEFAULT_MAPPING.put(DRIVER_STATUS, (r, driver, dto) -> {
            if (r.getDriver().isActive()) {
                return "Активен";
            } else {
                return "Не активен";
            }
        });
        DEFAULT_MAPPING.put(DRIVER_RAITING, (r, driver, dto) -> {
            if (r.getDriver() != null && r.getDriver().getRating() != null) {
                return r.getDriver().getRating().toString();
            } else {
                return NA;
            }
        });
    }
    
    static {
        useDefaultMapping(DRIVER_MAPPING,
                          CONTRACTOR_NAME,
                          DRIVER_ID,
                          FIRST_NAME,
                          LAST_NAME,
                          PATRONYMIC,
                          CONTACT_NUMBER,
                          LICENSE_AVAILABILITY,
                          LICENSE_NUMBER,
                          PASSPORT,
                          DRIVER_LICENSE_CATEGORY,
                          DRIVER_LICENSE_NUMBER,
                          DRIVER_TAG,
                          DRIVER_EXPIRIENCE,
                          DRIVER_STATUS,
                          DRIVER_RAITING
                         );
    }
    
    private static void useDefaultMapping(
            Map<String, Function4<Request, Driver, DriverReportFiltersDTO, String>> map,
            String name
                                         ) {
        var function = DEFAULT_MAPPING.get(name);
        Objects.requireNonNull(function, "Not found mapping implementation for '" + name + "'");
        map.put(name, function);
    }
    
    private static void useDefaultMapping(Map<String, Function4<Request, Driver, DriverReportFiltersDTO, String>> map, String... names) {
        for (String name : names) {
            useDefaultMapping(map, name);
        }
    }
    
    private static LinkedHashMap<String, Function4<Request, Driver, DriverReportFiltersDTO, String>> customPublicMapping(
            DriverUIVisibilityDTO driverFiltersDTO
                                                                                                                        ) {
        LinkedHashMap<String, Function4<Request, Driver, DriverReportFiltersDTO, String>> customMap =
                new LinkedHashMap<>();
        useDefaultMapping(customMap, CONTRACTOR_NAME);
        useDefaultMapping(customMap, DRIVER_ID);
        if (driverFiltersDTO.getAutoparkNames()) {
            useDefaultMapping(customMap, AUTOPARK);
        }
        if (driverFiltersDTO.getFirstName()) {
            useDefaultMapping(customMap, FIRST_NAME);
        }
        if (driverFiltersDTO.getLastName()) {
            useDefaultMapping(customMap, LAST_NAME);
        }
        if (driverFiltersDTO.getPatronymic()) {
            useDefaultMapping(customMap, PATRONYMIC);
        }
        useDefaultMapping(customMap, CONTACT_NUMBER);
        useDefaultMapping(customMap, LICENSE_AVAILABILITY);
        useDefaultMapping(customMap, LICENSE_NUMBER);
        useDefaultMapping(customMap, PASSPORT);
        useDefaultMapping(customMap, DRIVER_LICENSE_CATEGORY);
        useDefaultMapping(customMap, DRIVER_LICENSE_NUMBER);
        if (driverFiltersDTO.getDriverTags()) {
            useDefaultMapping(customMap, DRIVER_TAG);
        }
        if (driverFiltersDTO.getDriverExp()) {
            useDefaultMapping(customMap, DRIVER_EXPIRIENCE);
        }
        if (driverFiltersDTO.getDriverStatuses()) {
            useDefaultMapping(customMap, DRIVER_STATUS);
        }
        if (driverFiltersDTO.getDriverRating()) {
            useDefaultMapping(customMap, DRIVER_RAITING);
        }
        return customMap;
    }
    
    @Override
    public ByteArrayOutputStream exportToXlsx(
            Collection<Request> records, Map<UUID, Driver> driversMap,
            Map<UUID, DriverReportFiltersDTO> checkMap,
            DriverReportFiltersDTO requestReportDTO
                                             ) throws IOException {
        return exportToXlsx(records, driversMap, checkMap, customPublicMapping(requestReportDTO.getDriverUIVisibilityDTO()));
    }
    
    /**
     * Создание xlsx из набора request, с полями заданными в mapping
     *
     * @param records коллекция заявок
     * @param mapping коллекция полей и их маппинг для отображения
     *
     * @return стрим xlsx файла
     *
     * @throws IOException
     */
    private ByteArrayOutputStream exportToXlsx(
            Collection<Request> records,
            Map<UUID, Driver> driversMap,
            Map<UUID, DriverReportFiltersDTO> checkMap,
            LinkedHashMap<String, Function4<Request, Driver, DriverReportFiltersDTO, String>> mapping
                                              )
            throws IOException {
        Table<Integer, String, String> table = HashBasedTable.create();
        var rowIndex = new AtomicInteger(0);
        if (records.size() == 0) {
            mapping.forEach((column, provider) -> table.put(rowIndex.get(), column, ""));
        }
        for (Request record : records) {
            mapping.forEach((column, provider) -> // ordered iteration
                            {
                                var driver = record.getDriver();
                                var check = checkMap.get(record.getDriver().getId());
                                String applyValue = provider.apply(record, driver, check);
                                table.put(rowIndex.get(), column, applyValue);
                            });
            rowIndex.incrementAndGet();
        }
        return exportToXlsx(table, "Отчет по водителям");
    }
    
}
