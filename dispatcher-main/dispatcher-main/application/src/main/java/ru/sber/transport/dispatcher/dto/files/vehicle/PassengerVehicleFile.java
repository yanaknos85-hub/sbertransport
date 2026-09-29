package ru.sber.transport.dispatcher.dto.files.vehicle;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PassengerVehicleFile implements VehicleFile {

    /**
     * Тип
     */
    @NotEmpty
    private String vehicleType;

    /**
     * Принадлежность к автопарку
     */
    @NotEmpty
    private String autoParkName;

    /**
     * Регистрационный знак
     */
    @NotEmpty
    private String stateNumber;

    /**
     * Марка ТС
     */
    @NotEmpty
    private String modelBrand;

    /**
     * Модель ТС
     */
    private String modelName;

    /**
     * Идентификационный номер (VIN)
     */
    @Size(min = 17, max = 17)
    private String vin;

    /** Цвет */
    private String color;

    /**
     * Разрешенная максимальная масса, кг
     */
    private Integer maxAllowedWeight;

    /**
     * В эксплуатации
     */
    @NotEmpty
    private String inExploitation;

    /** Паспорт ТС */
    private String passport;

    /** Тип кузова */
    private String bodyType;

    /** Тип трансмиссии */
    private String transmissionType;

    /**
     * Тип двигателя
     */
    private String engineType;

    /**
     * Тип привода
     */
    private String chassisType;

    /** Год выпуска */
    private Integer modelYear;

    /**
     * Пробег автомобиля, км
     */
    private Integer mileage;

    /** Комплектация */
    private String packageClass;

    /** Потребление топлива, л */
    private Double fuelConsumption;

    /**
     * Эко класс
     */
    private String ecoClass;

    /**
     * Серия номер страховки
     */
    private String insuranceNumber;

}
