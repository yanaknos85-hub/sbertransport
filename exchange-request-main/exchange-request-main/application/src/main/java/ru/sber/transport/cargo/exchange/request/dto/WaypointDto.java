package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sber.transport.cargo.exchange.request.enums.WaypointType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Waypoint", description = "Точка маршрута (погрузка/выгрузка)")
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaypointDto {

    @Schema(description = "Уникальный идентификатор точки маршрута")
    private UUID id;

    @Schema(description = "Порядковый номер точки в маршруте (начиная с 0)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderingIndex;

    @Schema(description = "Радиус поиска в метрах вокруг точки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer radius;

    @Schema(description = "Тип точки: LOAD — погрузка, UNLOAD — выгрузка", requiredMode = Schema.RequiredMode.REQUIRED)
    private WaypointType type;

    @Schema(description = "JSON с информацией об адресе: город, улица, координаты и т.д.")
    private AddressInfoDto addressInfo;

    @Schema(description = "Дата операции (из dateFrom)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Дата обязательна")
    private LocalDate date;

    @Schema(description = "Время начала операции (в формате HH:mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Время начала обязательно")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime from;

    @Schema(description = "Время окончания операции (в формате HH:mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Время окончания обязательно")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime to;

    @Schema(description = "Контактные данные для взаимодействия на данной точке маршрута")
    private WaypointContactDto contact;
}
