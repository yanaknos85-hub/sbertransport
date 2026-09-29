package ru.sber.transport.dispatcher.dto.files.vehicle;

public interface VehicleFile {
    /**
     * Тип
     */
    String getVehicleType();

    /**
     * Принадлежность к автопарку
     */
    String getAutoParkName();

    /**
     * Регистрационный знак
     */
    String getStateNumber();

    /**
     * Марка ТС
     */
    String getModelBrand();

    /**
     * Модель ТС
     */
    String getModelName();

    /**
     * Идентификационный номер (VIN)
     */
    String getVin();

    /** Цвет */
    String getColor();

    /**
     * Разрешенная максимальная масса, кг
     */
    Integer getMaxAllowedWeight();
}
