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
public class CargoVehicleFile implements VehicleFile {

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

    /**
     * Объем, м3
     */
    private Double volume;

    /**
     * Длина, м
     */
    private Double length;

    /**
     * Ширина, м
     */
    private Double width;

    /**
     * Высота, м
     */
    private Double height;

}
