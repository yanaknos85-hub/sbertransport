package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.dispatcher.database.model.EcoClass;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;

/**
 * Object with data about Vehicle.
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о транспорте", description = "Новые данные транспорта")
public class NewVehicleDTO {
    
    /**
     * Идентификационный номер транспортного средства
     */
    @Size(min =17, max = 17)
    @Schema(description = "Идентификационный номер транспортного средства VIN", minLength = 17, maxLength = 17)
    private String vin;

    /** Паспорт ТС */
    @Schema(description = "Серия номер ПТС", pattern = "\\d{2}\\w{2}\\d{6}")
    private String passport;

    /** Гос номер */
    @Schema(description = "Гос номер", pattern = "([А-Яа-яA-Za-z]\\d{3}[А-Яа-яA-Za-z]{2}\\d{3}|[А-Яа-яA-Za-z]{2}\\d{3}\\d{2,3})(RUS|rus)")
    private String stateNumber;

    /** Серия номер страховки */
    @Schema(description = "Номер страховки", minLength = 1, pattern = "[А-Яа-яA-Za-z]{3}[0-9]{10}")
    private String insuranceNumber;

    /** Модель автомобиля */
    @NotNull
    @Schema(description = "Данные о модели")
    private CarModelDto model;

    /** Эко класс */
    @Schema(description = "Экологический стандарт ТС, EURO_0 - EURO_6")
    private EcoClass ecoClass;

    /** Потребление топлива, л */
    @Schema(description = "Потребление топлива, л", minimum = "0")
    private Double fuelConsumption;

    /** Комплектация */
    @Schema(description = "Комплектация автомобиля")
    private String packageClass;

    /** Пробег автомобиля, км */
    @Schema(description = "Пробег, км", minimum = "0")
    private Integer mileage;

    /** Цвет */
    @Schema(description = "Цвет")
    private String color;

    /** Год производства */
    @Schema(description = "Год производства", minimum = "0")
    private Integer manufactureYear;

    /** Разрешенная максимальная масса, кг */
    @Schema(description = "Разрешенная максимальная масса, кг", minimum = "0")
    private Integer maxAllowedWeight;

    /** Тип привода */
    @Schema(description = "Тип привода")
    private String chassisType;

    /** Тип трансмиссии */
    @Schema(description = "Тип трансмиссии")
    private String transmissionType;

    /** Тип кузова */
    @Schema(description = "Тип кузова")
    private String bodyType;
    
    /**
     * Тип двигателя
     */
    @Schema(description = "Тип двигателя")
    private String engineType;
    
    /**
     * В эксплуатации
     */
    @Schema(description = "В эксплуатации")
    private Boolean inExploitation;
    
    /** Автопарк */
    @Schema(description = "Автопарк")
    private AutoparkDTO autopark;

    /** Тип автомобиля */
    @NotNull
    @Schema(description = "Тип автомобиля")
    private VehicleType vehicleType;

    /** Дополнительные данные автомобиля */
    @Schema(description = "Дополнительные данные автомобиля")
    @JsonDeserialize(using = VehicleAdditionalDeserialize.class)
    private VehicleAdditional vehicleAdditional;

}

