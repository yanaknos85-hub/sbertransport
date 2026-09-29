package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Object with data of Vehicle.
 */
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о транспорте", description = "Данные транспорта")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDTO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "Бренд")
    private String brand;

    @Schema(description = "Модель")
    private String model;

    @Schema(description = "Регистрационный номер")
    private String stateNumber;

    @Schema(description = "Цвет")
    private String color;

    @Schema(description = "ID контрагента")
    private UUID contractorId;

    @Schema(description = "Признак удаленности автомобиля")
    private boolean deleted;

    @Schema(description = "Тип автомобиля")
    private String vehicleType = "CARGO";
}
