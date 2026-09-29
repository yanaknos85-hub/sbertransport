package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.RequestSourceEnum;
import ru.sberbank.ditsib.transport.request.dto.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO с <b>общими</b> данными по новой заявке на компенсацию за общественный транспорт
 **/
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class NewRequestForPublicDTO {
    
    /**
     * Автор
     */
    @NotNull
    @Schema(description = "Автор", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO author;
    
    /**
     * Пассажир
     */
    @NotNull
    @Valid
    @Schema(description = "Пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO passenger;
    
    /**
     * Временная зона создаваемой заявки
     */
    @Schema(description = "Временная зона")
    @Builder.Default
    private String timeZone = "UTC";
    
    /**
     * Тип транспорта
     */
    @Schema(description = "Тип транспорта")
    @Builder.Default
    private TransportTypeEnum transportType = TransportTypeEnum.PUBLIC;
    
    /**
     * Желаемая дата поездки
     */
    @Schema(description = "Желаемая дата поездки (в случае отсутствия - поездка на ближайшее время, для совместных " +
                          "поездок возможно создание минимум через 45 минут)")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime desiredDate;
    
    /**
     * Расчетные данные по маршруту
     */
    @NotNull
    @Valid
    @Schema(description = "Расчетные данные по маршруту", requiredMode = Schema.RequiredMode.REQUIRED)
    private PublicExpectedDataDTO expected;
    
    /**
     * Части маршрута между ключевых точек
     */
    @Size(min = 1, message = "Маршрут должен состоять минимум из 1 отрезка")
    @Schema(description = "Части маршрута между ключевых точек", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private final List<RouteSegmentDTO> segments = new ArrayList<>();
    
    /**
     * Ключевые точки
     */
    @Size(min = 2, max = 50, message = "Маршрут должен состоять минимум из 2, максимум из 50 точек")
    @Schema(description = "Ключевые точки", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "2", maximum = "50")
    @Builder.Default
    private final List<WaypointDTO> waypoints = new ArrayList<>();
    
    /**
     * Цель поездки
     */
    @NotNull
    @Valid
    @Schema(description = "Цель поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripPurposeDTO purpose;
    
    /**
     * Id of tariff used
     */
    @NotNull
    @Schema(description = "Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    @Schema(description = "Источник создания заявки")
    @Builder.Default
    private RequestSourceEnum source = RequestSourceEnum.UNDEFINED;
    
    @Size(max = 250, message = "Комментарий к цели не может содержать больше 250 символов")
    @Schema(description = "Комментарий к цели")
    private String commentForPurpose;
    
    @Schema(description = "Идентификатор связной заявки")
    private UUID payRequestId;
    
    @Schema(description = "Стоимость минимальной поездки на такси")
    private CalculateDTO minTariffTaxi;
    
}
