package ru.sberbank.ditsib.transport.exceptions;

/**
 * Значения из message.properties
 */
public enum ExceptionMessageResource {
    /**
     * Дублирование данных
     */
    DUPLICATE_DATA,

    /**
     * Данных не найдено
     */
    DATA_NOT_FOUND,

    /**
     * Автопарк с именем не найден
     */
    AUTOPARK_BY_NAME_NOT_FOUND,

    /**
     * Автопарк с именем уже существует
     */
    AUTOPARK_BY_NAME_ALREADY_EXISTS,

    /**
     * Контрагент с именем и ИНН уже существует
     */
    CONTRACTOR_BY_NAME_TIN_ALREADY_EXISTS,

    /**
     * High-level department already exists
     */
    HIGH_LEVEL_DEPARTMENT_EXISTS,

    /**
     * Класс поездки уже существует
     */
    TRIP_CLASS_ALREADY_EXISTS,

    /**
     * Ошибка дублирования записи: Департамент в организации ''{0}'' с кодом ''{1}'' уже существует
     */
    DEPARTMENT_BY_ORGANIZATION_AND_CODE_ALREADY_EXISTS,

    /**
     * Ошибка дублирования записи: ТС с гос номером ''{0}'' или идентификационным номером транспортного средства (VIN) ''{1}'' уже существует
     */
    VEHICLE_BY_STATE_NUMBER_OR_VIN_ALREADY_EXISTS,

    /**
     * Ошибка дублирования записи: ТС с гос номером ''{0}'', или паспортом ТС ''{1}'', или идентификационным номером транспортного средства (VIN) ''{2}'' уже существует
     */
    VEHICLE_BY_STATE_NUMBER_OR_PASSPORT_OR_VIN_ALREADY_EXISTS
}
