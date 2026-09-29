package ru.sber.transport.dispatcher.dto.search;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Schema(title = "Поиск транспортных средств", description = "Фильтры для транспортного средства")
public class VehicleSearchDTO extends PageSortFilterParameters<VehicleSearchParameters> {

    /**
     * Создать новый объект.
     */
    public VehicleSearchDTO() {
        super(VehicleSearchParameters.STATE_NUMBER);
    }

    @Schema(description = "Автопарк")
    private UUID autopark;

    @Schema(description = "Гос номер")
    private String stateNumber;

    @Schema(description = "Год производства")
    private Integer manufactureYear;

    @Schema(description = " Начало диапазона (год производства)")
    private Integer startDateManufactureYear;

    @Schema(description = " Конец диапазона (год производства)")
    private Integer endDateManufactureYear;

    @Schema(description = "Тип трансмиссии")
    private String transmissionType;

    @Schema(description = "Марка ТС")
    @JsonAlias(value = "brands")
    private String brand;

    @Schema(description = "Модель ТС")
    private String model;

    @Schema(description = "Тип ТС")
    private VehicleType vehicleType;

    @Schema(description = "Находится в эксплуатации")
    private Boolean inExploitation;
}
