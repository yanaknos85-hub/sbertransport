package ru.sber.transport.dispatcher.database.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.dispatcher.dto.VehicleAdditional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CargoVehicleData implements VehicleAdditional {

    /**
     * Регистрационный номер полуприцепа
     */
    private String semitrailerNumber;

    /**
     * Объем, м3
     */
    private double volume;

    /**
     * Длина, м
     */
    private double length;

    /**
     * Ширина, м
     */
    private double width;

    /**
     * Высота, м
     */
    private double height;

}
