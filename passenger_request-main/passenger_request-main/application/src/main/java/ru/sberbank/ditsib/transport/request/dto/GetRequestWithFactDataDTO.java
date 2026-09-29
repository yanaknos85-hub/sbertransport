package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.RequestOptions;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.util.*;

@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Фактические данные по поездке")
@SuperBuilder
public class GetRequestWithFactDataDTO {
    
    /**
     * Идентификатор
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Идентификатор (человекочитаемый)
     */
    @NotNull
    @Schema(description = "Идентификатор (человекочитаемый)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableId;
    
    /**
     * Статус заявки
     */
    @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripRequestStatus status;
    
    /**
     * Пассажир
     */
    @NotNull
    @Schema(description = "Пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO passenger;
    
    /**
     * Согласующий
     */
    @Schema(description = "Согласующий")
    private EmployeeDTO approvedBy;
    
    /**
     * Все Пассажиры
     */
    @NotNull
    @Schema(description = "Все Пассажиры", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private List<EmployeeDTO> passengers = new ArrayList<>();
    
    /**
     * Дата и время создания заявки
     */
    @Schema(description = "Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    /**
     * Фактические данные по маршруту
     */
    @Schema(title = "Фактические данные по маршруту")
    private FactDataDTO factDataDTO;
    
    /**
     * Список точек маршрута
     */
    @Schema(description = "Список точек маршрута")
    @Builder.Default
    private List<WaypointDTO> waypoints = new ArrayList<>();
    
    /**
     * Класс такси
     */
    @Schema(description = "Класс такси")
    private TaxiClass taxiClass;
    
    /**
     * PКоличество пассажиров
     */
    @Schema(description = "Количество пассажиров")
    private int passengerCount;
    
    /**
     * Цель поездки
     */
    @NotNull
    @Schema(description = "Цель поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripPurposeDTO purpose;
    
    /**
     * Флаг совместной поездки
     */
    @Schema(description = "Флаг совместной поездки")
    private boolean coopTrip;
    
    /**
     * Идентификатор используемого тарифа
     */
    @Schema(description = "Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    /**
     * Опции поездки
     */
    @Schema(description = "Опции поездки")
    @Builder.Default
    private final Set<RequestOptions> requestOptions= new HashSet<>();
    
    /**
     * Комментарий для водителя
     */
    @Schema(description = "Комментарий для водителя")
    private String commentForDriver;
    
    /**
     * Оценка заявки
     */
    @Schema(description = "Оценка заявки")
    private RequestRatingDTO requestRating;
    
    /**
     * Идентификатор совместной поездки
     */
    @Schema(description = "Идентификатор совместной поездки (Только для совместных поездок)")
    private UUID sharedRideId;
    
    /**
     * Дата внесения фактических параметров поездки
     */
    @Schema(description = "Дата внесения фактических параметров поездки")
    private LocalDateTime factParametersSettingTime;
    
    /**
     * Описание работ в формате JSON.
     */
    @Schema(description = "Описание работ в формате JSON. Заполняется после назначения водителя на поездку")
    private String resolution;
}

