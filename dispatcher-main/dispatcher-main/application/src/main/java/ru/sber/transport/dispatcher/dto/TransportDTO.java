package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Данные о свободном транспорте")
public class TransportDTO {

    @Schema(description = "Идентификатор транспорта")
    private UUID id;

    @Schema(description = "Марка транспорта")
    private String brand;

    @Schema(description = "Модель транспорта")
    private String model;

    @Schema(description = "Регистрационный номер транспорта")
    private String stateNumber;

    @Schema(description = "Список поездок машины")
    private List<Trip> trips = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Список временных слотов")
    public static class Trip {

        @Schema(description = "Дата начала временного слота")
        private OffsetDateTime start;

        @Schema(description = "Дата окончания временного слота")
        private OffsetDateTime end;

    }

}
