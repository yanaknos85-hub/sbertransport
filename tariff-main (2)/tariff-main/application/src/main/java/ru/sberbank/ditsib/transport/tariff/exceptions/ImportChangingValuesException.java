package ru.sberbank.ditsib.transport.tariff.exceptions;

/**
 * Ошибка, выбрасываемая в случае попытки изменения неизменяемых полей.
 */
public class ImportChangingValuesException extends RuntimeException{
    public static final String CONTRACT_NUMBER_CHANGING = "Поле \"Договор\" недоступно для изменения";
    public static final String ORGANIZATION_NAME_CHANGING = "Поле \"Организация\" недоступно для изменения";
    public static final String REGION_NAME_CHANGING = "Поле \"Регион\" недоступно для изменения";
    public static final String TAXI_CLASS_CHANGING = "Поле \"Класс\" недоступно для изменения";
    public static final String TAXI_SERVICE_TYPE_CHANGING = "Поле \"Тип услуги\" недоступно для изменения";
    public static final String DEPARTMENT_CHANGING = "Поле \"Подразделение\" недоступно для изменения";
    public static final String INCORRECT_WORK_GROUP_FORMAT = "Не верный формат поля \"Рабочая группа\" для типа интеграции с контрагентом";
    public static final String INCORRECT_WORK_GROUP_CHANGING = "Поле \"Рабочая группа\" должно соответствовать шаблону " +
                                                               "organization/транспорт/region/contract";
    
    /**
     * Создать исключение.
     *
     * @param pattern тип ошибки.
     */
    public ImportChangingValuesException(String pattern){
        super(pattern);
    }
    
}