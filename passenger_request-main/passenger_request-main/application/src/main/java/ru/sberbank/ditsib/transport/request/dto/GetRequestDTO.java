package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.request.dto.fraud.FraudCommentDTO;

import java.time.LocalDateTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки")
@SuperBuilder
public class GetRequestDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Human readable id
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
     * Текстовое описание кода статуса заявки
     */
    @Schema(description = "Текстовое описание кода статуса заявки")
    private String statusCodeDescription;
    
    /**
     * Author id
     */
    @NotNull
    @Valid
    @Schema(description = "Автор", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO author;
    
    /**
     * Passenger id
     */
    @NotNull
    @Schema(description = "Пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO passenger;
    
    /**
     * List of all passengers
     */
    @NotNull
    @Schema(description = "Все Пассажиры", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private List<EmployeeDTO> passengers = new ArrayList<>();
    
    /**
     * Type of transport used for request
     */
    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;
    
    /**
     * Class of taxi used for request
     */
    @Schema(description = "Класс такси")
    private TaxiClass taxiClass;
    
    /**
     * Flag of coop trip
     */
    @Schema(description = "Флаг совместной поездки")
    private boolean coopTrip;
    
    /**
     * Passenger count
     */
    @Schema(description = "Количество пассажиров")
    @Builder.Default
    private int passengerCount = 1;
    
    /**
     * Id of tariff used
     */
    @Schema(description = "Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    /**
     * Id of outcome tariff used
     */
    @Schema(description = "Идентификатор используемого расходного тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID outcomeTariffId;
    
    /**
     * Desired date and time of trip
     */
    @Schema(description = "Желаемая дата поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime desiredDate;
    
    /**
     * Data and calculations about waypoints
     */
    @NotNull
    @Valid
    private ExpectedDataDTO expected;
    
    /**
     * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки
     */
    @Schema(description = "Доля заказа в общей стоимости (Такси/ЛТ)", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "1.0")
    @Builder.Default
    private Double costSharePart = 1.0;
    
    /**
     * Trip purpose
     */
    @NotNull
    @Schema(description = "Цель поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripPurposeDTO purpose;
    
    /**
     * Comment
     */
    @Schema(description = "Комментарий для водителя")
    private String commentForDriver;
    
    /**
     * Идентификатор совместной поездки
     */
    @Schema(description = "Идентификатор совместной поездки (Только для совместных поездок)")
    private UUID sharedRideId;
    
    /**
     * Согласующий
     */
    @Schema(description = "Согласующий")
    private EmployeeDTO approvedBy;
    
    /**
     * Статус согласования
     */
    @Schema(description = "Статус согласования")
    private ApprovalState approvalState;
    
    /**
     * Дата согласования
     */
    @Schema(description = "Дата согласования")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime approvalDate;
    
    
    @Schema(description = "Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    /**
     * Дата и время подтверждения поездки
     */
    @Schema(description = "Дата и время подтверждения поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tripConfirmationDate;
    
    @Schema(description = "Оценка заявки")
    private RequestRatingDTO requestRating;
    
    @Schema(description = "Опции поездки")
    @Builder.Default
    private final Set<RequestOptions> requestOptions = new HashSet<>();
    
    /**
     * Описание работ в формате JSON.
     */
    @Schema(description = "Описание работ в формате JSON. Заполняется после назначения водителя на поездку")
    private String resolution;
    
    /**
     * Временная зона создаваемой заявки
     */
    @NotNull
    @Schema(description = "Временная зона")
    private String timeZone;
    
    /**
     * Дата и время дедлайна
     */
    @Schema(description = "Дата и время дедлайна", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime deadline;
    
    /**
     * Количество занятых мест в личном транспорте. Задается для transportType = PERSONAL
     */
    @Schema(description = "Количество занятых мест в личном транспорте")
    private Integer occupiedPlacesCount;

    @Schema(description = "Время аренды (автобусы) в мс")
    private Integer busRentDuration;

    @Schema(description = "Кол-во автобусов")
    private Integer busCount;

    @Schema(description = "Водитель")
    private DriverDTO driver;

    @Schema(description = "Автомобиль")
    private VehicleDTO vehicle;

    @Schema(description = "Информация о водителе и транспорте")
    private DriverInfoDTO driverInfo;

    @Schema(description = "Главная заявка (владелец) совместной поездки")
    private boolean sharedRideOwner;

    @Builder.Default
    @Schema(description = "Присоединенные пассажиры")
    private List<EmployeeDTO> joinedPassengers = new ArrayList<>();

    @Schema(description = "Комментарий к цели", maxLength = 250)
    @Size(max = 250, message = "Комментарий к цели не может содержать больше 250 символов")
    private String commentForPurpose;

    @Schema(description = "Информация о фроде. Заполняется только если есть подозрение на фрод")
    private List<FraudCommentDTO> fraudComment;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetRequestDTO that = (GetRequestDTO) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
