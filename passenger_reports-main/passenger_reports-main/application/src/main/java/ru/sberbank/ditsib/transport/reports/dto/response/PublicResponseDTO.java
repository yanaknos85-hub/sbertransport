package ru.sberbank.ditsib.transport.reports.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Schema(title = "Информация", description = "Информация о заказе для общественного транспорта")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PublicResponseDTO implements ReportResponseDTO {
    
    @Schema(description = "Идентификатор")
    private UUID id;
    
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    @Schema(description = "Идентификатор лимита (человекочитаемый) ")
    private String humanReadableLimitId;
    
    @Schema(description = "Автор")
    private EmployeeDTO author;
    
    @Schema(description = "Пассажир")
    private EmployeeDTO passenger;
    
    @Schema(description = "Характер деятельности сотрудника")
    private ItinerantType itinerantType;
    
    @Schema(description = "Место возникновения затрат")
    private String costCenter;
    
    @Schema(description = "Данные подразделения")
    private DepartmentShortDTO department;
    
    @Schema(description = "Данные должности сотрудника")
    private PositionShortDTO position;
    
    @Schema(description = "Время создания заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    @Schema(description = "Дата и время выезда", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime desiredDate;
    
    @Schema(description = "Время согласования заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime approveDate;
    
    @Schema(description = "Время согласования заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime orderPaymentFormationStartDate;
    
    @Schema(description = "Тип транспорта")
    private String transportType;
    
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;
    
    @Deprecated
    @Schema(description = "Тип компенсации", deprecated = true)
    private PublicCompensationType compensationType;
    
    @Schema(description = "Список типов компенсаций")
    private List<TransportCompensationDTO> transportCompensation;
    
    @Schema(description = "Признак существования документов для компенсации")
    private Boolean publicCompensationDocumentExist;
    
    @Schema(description = "Цель поездки")
    private TripPurposeDTO purpose;
    
    @Schema(description = "Расчетные данные по маршруту")
    private ExpectedDataDTO expected;
    
    @Schema(description = "Период выплаты, квартал")
    private Integer paymentQuarter;
    
    @Schema(description = "Оценка заявки")
    private RequestRatingDTO requestRating;
    
    @Schema(description = "Дата выплаты")
    private LocalDateTime paymentTime;
    
    @Schema(description = "Подразделение 1 уровня")
    String passengerDepartment1;
    
    @Schema(description = "Подразделение 2 уровня")
    String passengerDepartment2;
    
    @Schema(description = "Подразделение 3 уровня")
    String passengerDepartment3;
    
    @Schema(description = "Подразделение 4 уровня")
    String passengerDepartment4;
    
    @Schema(description = "Подразделение 5 уровня")
    String passengerDepartment5;
    
    @Schema(description = "Подразделение 6 уровня")
    String passengerDepartment6;
    
    @Schema(description = "Идентификатор используемого тарифа")
    private TariffShortDTO tariff;
    
    @Schema(description = "Адрес отправления")
    String departureAddress;
    
    @Schema(description = "Промежуточные адреса")
    String intermediateAddresses;
    
    @Schema(description = "Адрес назначения")
    String destinationAddress;
    
    @Schema(description = "Временная зона")
    private String timeZone;
    
    @Schema(description = "Код вида основной оплаты")
    private Integer paymentTypeCodeMain;
    
    @Schema(description = "Сумма основной оплаты, коп")
    private Long paymentPriceMain;
    
    @Schema(description = "Код вида дополнительной оплаты")
    private Integer paymentTypeCodeOptional;
    
    @Schema(description = "Сумма дополнительной оплаты, коп")
    private Long paymentPriceOptional;
    
    @Schema(description = "Источник заявки")
    private String source;
    
    @Schema(description = "Фиксированная стоимость минимально расчетного тарифа по такси")
    private Double minTaxiTariffCost;
    
    @Schema(description = "Комментарий к цели")
    private String commentForPurpose;
    
    @Schema(description = "Код причины отмены")
    private Integer requestStatusCode;
    
    @Schema(description = "Экономия")
    private Boolean savings;
    
    @Schema(description = "Нарушение КС (да/нет)")
    private String deadlineViolation;
    
    @Schema(description = "Контрольный срок")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime deadline;
    
    @Schema(description = "Дата закрытия обращения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime requestClosedDatetime;

    @Schema(description = "ID группы исполнителей")
    private UUID executorGroupId;

    @Schema(description = "Наименование группы исполнителей")
    private String executorGroupName;
}
