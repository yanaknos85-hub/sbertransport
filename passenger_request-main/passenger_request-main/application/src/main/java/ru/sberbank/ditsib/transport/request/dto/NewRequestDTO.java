package ru.sberbank.ditsib.transport.request.dto;

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
import ru.sberbank.ditsib.transport.request.database.model.RequestSourceEnum;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Новая заявка", description = "Данные новой заявки")
public class NewRequestDTO {
    
    /**
     * Passenger id
     */
    @NotNull
    @Schema(description = "Пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
    private EmployeeDTO passenger;
    
    /**
     * Временная зона создаваемой заявки
     */
    @Schema(description = "Временная зона")
    private String timeZone;
    
    /**
     * Type of transport used for request
     */
    @NotNull
    @Schema(description = "Тип транспорта",
            example = "TAXI | PERSONAL | PUBLIC | CARSHARING | BICYCLE | SCOOTER | WALK",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;
    
    /**
     * ID of transport type.
     */
    @Schema(description = "Идентификатор типа транспорта")
    private UUID transportTypeId;
    
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
    @NotNull
    @Schema(description = "Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    /**
     * Id of outcome tariff used
     */
    @NotNull
    @Schema(description = "Идентификатор используемого расходного тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID outcomeTariffId;
    
    /**
     * Триггерное время для заявки на такси
     */
    @Schema(description = "Триггерное время для заявки на такси. Получается из расчётов по тарифу такси priceDetails.autoCancelDeadlineMin")
    private Integer autoCancelDeadlineMin;
    
    /**
     * Desired date and time of trip
     */
    @Schema(description = "Желаемая дата поездки (в случае отсутствия - поездка на ближайшее время, для совместных " +
                          "поездок возможно создание минимум через 45 минут)")
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
     * Trip purpose
     */
    @NotNull
    @Schema(description = "Цель поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripPurposeDTO purpose;

    @Schema(description = "Опции поездки")
    @Builder.Default
    private final Set<RequestOptions> requestOptions = new HashSet<>();
    
    /**
     * Comment
     */
    @Schema(description = "Комментарий для водителя")
    private String commentForDriver;
    
    /**
     * Количество занятых мест в личном транспорте. Задается для transportType = PERSONAL
     */
    @Schema(description = "Количество занятых мест в личном транспорте")
    private Integer occupiedPlacesCount;
    
    /**
     * ID of personal car
     */
    @Schema(description = "Идентификатор личного транспорта")
    private UUID personalCarId;
    
    /**
     * Class of carsharing used for request
     */
    @Schema(description = "Класс каршеринга")
    private CarsharingClass carsharingClass;

    @Schema(description = "Идентификатор контрагента")
    private UUID contractorId;

    @Schema(description = "Время аренды (автобусы) в мс")
    private Integer busRentDuration;

    @Schema(description = "Кол-во автобусов")
    private Integer busCount;
    
    /**
     * Предрасчитанная цена заявки.
     */
    @Schema(description = "Предрасчитанная цена заявки")
    private Long requestPrice;

    @Schema(description = "Признак VIP")
    private boolean vip;

    @Schema(description = "Класс группового трансфера")
    private GroupTransferClass groupTransferClass;

    @Schema(description = "Дополнительная информация о заявке")
    private GroupTransferRequestInformationDTO information;

    @Schema(description = "Пассажиры", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private Set<UUID> joinedPassengerIds = new HashSet<>();

    @Schema(description = "Источник создания заявки")
    private RequestSourceEnum source;

    @Schema(description = "Стоимость минимально поездки на такси")
    private CalculateDTO minTariffTaxi;
    
    /**
     * Тайм зона устройства сотрудника
     */
    private String employeeDeviceTimeZone;

    @Schema(description = "Комментарий к цели", maxLength = 250)
    @Size(max = 250, message = "Комментарий к цели не может содержать больше 250 символов")
    private String commentForPurpose;
}
