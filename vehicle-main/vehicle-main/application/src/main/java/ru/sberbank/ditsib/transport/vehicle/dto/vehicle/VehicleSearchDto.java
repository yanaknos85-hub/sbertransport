package ru.sberbank.ditsib.transport.vehicle.dto.vehicle;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(title = "Поиск автомобиля", description = "Запрос на поиск автомобиля")
public record VehicleSearchDto(
        VehicleInfoDto vehicle,
        EngineInfoDto engine,
        PageSettingDto pageSetting
) {
    @Schema(title = "Параметры бренда и модели", description = "Параметры для поиска автомобиля по бренду и/или модели")
    public record VehicleInfoDto(
            @Schema(title = "Ид бренда")
            UUID brand,
            @Schema(title = "Ид модели")
            UUID model,
            @Schema(title = "Тип кузова")
            UUID bodyType,
            @Schema(title = "Год выпуска", description = "Год выпуска")
            @Positive
            Integer manufactureYear,
            @Schema(title = "Период выпуска", description = "Период выпуска в формате yyyy - yyyy, " +
                    "в случае отсуствия даты снятия с производства: yyyy - н.в.")
            String manufacturePeriod
            ) {
    }

    @Schema(title = "Параметры типа двигателя и привода", description = "Параметры для поиска автомобиля по типу двигателя и/или приводу")
    public record EngineInfoDto(
            @Schema(title = "Ид типа двигателя")
            UUID engineType,
            @Schema(title = "Ид привода")
            UUID drive,
            @Schema(title = "Тип трансмиссии")
            UUID transmissionType,
            @Schema(title = "Объем двигателя")
            Integer engineCapacity,
            @Schema(title = "Мощность ЛС")
            BigDecimal enginePower
            ) {
    }

    public VehicleInfoDto getVehicle() {
        return Objects.isNull(vehicle) ?
                new VehicleInfoDto(null, null, null, null, null) :
                vehicle;
    }

    public EngineInfoDto getEngine() {
        return Objects.isNull(engine) ?
                new EngineInfoDto(null, null, null, null, null) :
                engine;
    }

}
