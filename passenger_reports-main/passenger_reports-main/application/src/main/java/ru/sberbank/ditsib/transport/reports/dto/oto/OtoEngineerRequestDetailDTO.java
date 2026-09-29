package ru.sberbank.ditsib.transport.reports.dto.oto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.reports.dto.*;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Детальная информация по заявке для инженера ОТО", description = "Детальная информация по заявке для инженера ОТО")
public class OtoEngineerRequestDetailDTO {
    
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
    private String status;

    /**
     * Тип транспорта
     */
    @NotNull
    @Schema(description = "Тип транспорта")
    private String transportType;
    
    /**
     * Информация о перевозчике
     */
    @Schema(description = "Информация о перевозчике(контрагент)")
    private ContractorDTO contractor;
    
    /**
     * Адреса.
     */
    @Schema(description = "Адреса")
    @Builder.Default
    private List<AddressDto> addresses = new ArrayList<>();
    
    /**
     * Данные пассажира
     */
    @Schema(description = "Данные пассажира(Признаки сотрудника)")
    private EmployeeDTO passenger;
    
    @Schema(description = "Класс такси")
    private TaxiClass taxiClass;
    
    @Schema(description = "Время создания")
    private ZonedDateTime creationTime;
    
    @Schema(description = "Фактическое время выезда")
    private ZonedDateTime tripStartTime;
    
    @Schema(description = "Фактическая дата и время закрытия заявки")
    private ZonedDateTime tripFinishTime;
    
    @Schema(description = "Желаемое время поездки")
    private ZonedDateTime desiredDate;
    
    /**
     * Контрольный срок
     */
    @Schema(description = "Контрольный срок")
    private LocalDateTime deadline;

    /**
     * Идентификатор заявки
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Комментарий для водителя
     */
    @Schema(description = "Комментарий для водителя")
    private String commentForDriver;
    
    @Schema(description = "Идентификатор тарифа")
    private UUID tariffId;

    /**
     * Подразделение
     */
    @Schema(description = "Данные пассажира(Признаки сотрудника)")
    private DepartmentShortDTO department;

    /**
     * Время регистрации поездки
     */
    @Schema(description = "Дата и время получения заявки перевозчиком")
    private LocalDateTime dateTimeRegistered;
    
    @Schema(description = "Количество пассажиров")
    private int passengerCount;
    
    @Schema(description = "Фактические данные поездки")
    private FactDataDTO fact;
    
    @Schema(description = "Предварительные данные поездки")
    private ExpectedDataDTO expected;
    
    @Schema(description = "Состояние КС")
    private String deadlineState;

    /**
     * Данные водителя(имя рейтинг etc
     */
    @Schema(description = "Данные водителя")
    private DriverDTO driver;

    /**
     * Данные водителя(имя рейтинг etc
     */
    @Schema(description = "Данные автомобиля (Марка, цвет, гос. номер)")
    private CarInfoDTO vehicle;

    /**
     * Адрес назначения
     */
    @Schema(description = "Данные диспетчера(ФИО)")
    private String dispatcherInfo;

    /**
     * Данные аппрувера
     */
    @Schema(description = "Данные согласующего")
    private EmployeeDTO approvedBy;

    /**
     * Данные лимита
     */
    @Schema(description = "Данные лимита")
    private LimitShortDTO limit;
    
    @Schema(description = "Время ожидания")
    @JsonSerialize(using = DurationMillisConverter.class)
    @Builder.Default
    private Duration waitTime = Duration.ZERO;
    
}
