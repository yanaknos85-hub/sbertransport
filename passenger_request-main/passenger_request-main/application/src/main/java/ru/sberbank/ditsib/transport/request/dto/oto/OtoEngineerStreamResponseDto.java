package ru.sberbank.ditsib.transport.request.dto.oto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(title = "Данные потока заявок", description = "Данные потока заявок")
public class OtoEngineerStreamResponseDto {

    /**
     * Тип транспорта
     */
    @NotNull
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;

    /**
     * Идентификатор заявки
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * Человекочитаемы идентификаторь заявки
     */
    @NotNull
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;

    /**
     * Статус заявки
     */
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;

    /**
     * Данные пассажира
     */
    @NotNull
    @Schema(description = "Данные пассажира")
    private EmployeeDTO passenger;

    /**
     * Комментарий для водителя
     */
    @Schema(description = "Комментарий для водителя")
    private String commentForDriver;

    /**
     * Адрес подачи
     */
    @Schema(description = "Адрес подачи")
    private String departureAddress;

    /**
     * Адрес назначения
     */
    @Schema(description = "Адрес назначения")
    private String destinationAddress;

    /**
     * Дата и время создания поездки
     */
    @Schema(description = "Дата и время создания заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;

    /**
     * Желаемая дата и время отправления
     */
    @Schema(description = "Желаемая дата и время отправления")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime desiredDate;

    /**
     * Состояние контрольного срока
     */
    @Schema(description = "Состояние контрольного срока")
    private DeadlineState deadlineState;

    /**
     * Контрольный срок, мин
     */
    @Schema(description = "Контрольный срок")
    private LocalDateTime deadline;

    /**
     * Время ожидания
     */
    @Min(0)
    @Schema(description = "Время ожидания, мин", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long waitTime;

    /**
     * Идентификатор Тарифа
     */
    @Schema(description = "Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;

    /**
     * Класс автомобиля
     */
    @Schema(description = "Класс автомобиля")
    private TaxiClass taxiClass;

    /**
     * Информация о перевозчике
     */
    @Schema(description = "Информация о перевозчике(контрагент)")
    private ContractorShortDTO contractor;

    @Schema(description = "Дата и время получения заявки перевозчиком")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime dateTimeRegistered;

    @Schema(description = "Фактическое время поиска автомобиля, мин")
    private Long factSearchTime;

    @Schema(description = "Фактическое время выезда")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime tripStartTime;

    @Schema(description = "Фактическая дата и время закрытия заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime tripFinishTime;


}
