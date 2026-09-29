package ru.sberbank.ditsib.transport.reports.dto.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class PersonalReportDTO {
    
    /**
     * МВЗ
     */
    @NullRender
    private String mvz;
    
    /**
     * ID поездки
     */
    @NullRender
    private String humanReadableId;
    
    /**
     * Дата и время создания поездки
     */
    @NullRender
    private LocalDateTime creationTime;
    
    /**
     * Тип поездки
     */
    @NullRender
    private String tripType;
    
    /**
     * Фактическая дата и время выезда
     */
    @NullRender
    private LocalDateTime actualDepartureDatetime;
    
    /**
     * Дата согласования поездки
     */
    @NullRender
    private LocalDateTime approvalDate;
    
    /**
     * Дата закрытия заявки
     */
    @NullRender
    private LocalDateTime paymentDate;
    
    /**
     * ФИО польз. К.К.
     */
    @NullRender
    private String fio;
    
    /**
     * Разъездной характер деятельности
     */
    @NullRender
    private String passengerActivity;
    
    /**
     * Цель поездки
     */
    @NullRender
    private String purpose;
    
    /**
     * Статус поездки
     */
    @NullRender
    private String status;
    
    /**
     * Код статуса с расшифровкой 201 - Отмена пользователем 202 - Не согласовано 203 - Не согласовано по истечению срока 204 - Не утверждено 205 - Не
     * утверждено по истечению срока 206 - По истечению срока 207 - Отмена водителем ЛТ
     */
    @NullRender
    private String statusCode;
    
    /**
     * Стоимость поездки, руб
     */
    @NullRender
    private Double requestExpectedCost;
    
    /**
     * Кол-во пунктов маршрута общее
     */
    @NullRender
    private Integer waypointsCount;
    
    /**
     * Кол-во пунктов маршрута поездки с совпадением координат "Отметиться"
     */
    @NullRender
    private Long waypointsCountWithCheckIn;
    
    /**
     * Кол-во пунктов поездки без совпадения координат "Отметиться"
     */
    @NullRender
    private Long waypointsCountWithoutCheckIn;
    
    /**
     * Период выплаты
     */
    @NullRender
    private Integer paymentPeriod;
    
    /**
     * Код подразделения
     */
    @NullRender
    private String departmentCode;
    
    /**
     * Подразделение 1 уровня
     */
    @NullRender
    private String passengerDepartment1;
    
    /**
     * Подразделение 2 уровня
     */
    @NullRender
    private String passengerDepartment2;
    
    /**
     * Подразделение 3 уровня
     */
    @NullRender
    private String passengerDepartment3;
    
    /**
     * Подразделение 4 уровня
     */
    @NullRender
    private String passengerDepartment4;
    
    /**
     * Подразделение 5 уровня
     */
    @NullRender
    private String passengerDepartment5;
    
    /**
     * Подразделение 6 уровня
     */
    @NullRender
    private String passengerDepartment6;
    
    /**
     * Номер тарифа
     */
    private String tariff;
    
    /**
     * Адрес отправления
     */
    @NullRender
    private String departureAddress;
    
    /**
     * Адрес назначения
     */
    @NullRender
    private String destinationAddress;
    
    /**
     * Промежуточные адреса
     */
    @NullRender
    private String intermediateAddresses;
    
    /**
     * Табельный номер пассажира
     */
    @NullRender
    private String passengerPersonnelNumber;
    
    /**
     * Дата утверждения поездки
     */
    @NullRender
    private LocalDateTime orderPaymentFormationStartDate;
    
    /**
     * Оценка поездки пользователем
     */
    @NullRender
    private String rating;
    
    /**
     * Комментарий пользователя к оценке
     */
    @NullRender
    private String ratingComment;
    
    /**
     * Право собственности на автомобиль
     */
    @NullRender
    private String ownershipOfCar;
    
    /**
     * Номер свидетельства о браке
     */
    @NullRender
    private String marriageCertificateNumber;
    
    /**
     * Регистрационный номер автомобиля
     */
    @NullRender
    private String carRegistrationNumber;
    
    /**
     * Марка автомобиля
     */
    @NullRender
    private String carBrandName;
    
    /**
     * Объем двигателя автомобиля
     */
    @NullRender
    private Integer carEngineVolume;
    
    /**
     * Номер полиса ОСАГО
     */
    @NullRender
    private String osagoNumber;
    
    /**
     * Расстояние, км
     */
    @NullRender
    private Double requestExpectedDistance;
    
    /**
     * Желаемая дата отправления
     */
    @NullRender
    private LocalDate desiredDate;
    
    /**
     * Желаемое время отправления
     */
    @NullRender
    private LocalTime desiredTime;
    
    /**
     * Доля в общей стоимости поездки для участника
     */
    @NullRender
    private Double costSharedPart;
    
    /**
     * Экономия в рублях
     */
    @NullRender
    private Double savingsCash;
    
    /**
     * Экономия в процентах
     */
    @NullRender
    private Long savingsProcent;
    
    /**
     * ФИО водителя
     */
    @NullRender
    private String driverFIO;
    
    /**
     * Инициатор совместной поездки
     */
    @NullRender
    private Boolean sharedRideOwner;
    
    /**
     * Признак водитель/пассажир
     */
    @NullRender
    private String driverOrPassenger;
    
    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    private Integer numberPassengersJoined;
    
    /**
     * Сумма доплаты за всех пассажиров в руб.
     */
    private Double additionalSum;
    
    /**
     * Сумма к выплате
     */
    private Double paymentCost;
    
    /**
     * Количество забронированных мест (без учета водителя)
     */
    private Integer passengerCount;
    
    /**
     * Сумма к выплате по коду 4661
     */
    private Double sum4661;
    
    /**
     * Сумма к выплате по коду 4664
     */
    private Double sum4664;
    
    /**
     * Сумма к выплате по коду 4665
     */
    private Double sum4665;
    
    /**
     * Список присоединённых пассажиров
     */
    private String joinedPassengers;
    
    /**
     * Источник данных
     */
    private String source;
    
    private Double minTaxiTariffCost;
    
    private Integer totalSharedRequestCount;
    
    private Double limitDebit;
    
    private Double departmentEconomy;
    
    private Double financialImpact;
    
    private Double financialImpactShared;
    
    private Double financialImpactJoined;
    
    private String commentForPurpose;
    /**
     * Причина отмены
     */
    @NullRender
    private Integer requestStatusCode;
    
    /**
     * Контрольный срок
     */
    private String deadlineState;
    
    /**
     * Контрольный срок подачи ТС
     */
    @NullRender
    private LocalDateTime deadline;
    
    /**
     * Номер договора
     */
    private String contractNumber;
    
    /**
     * Стоимость поездки на такси
     */
    @NullRender
    private Double taxiCost;
    
    /**
     * Экономия.
     */
    private String savings;
    
    /**
     * Экономия от совместные поездки, руб.
     */
    @NullRender
    private Double sharedSaving;
    
    /**
     * Экономия при указании пассажиров, руб.
     */
    @NullRender
    private Double passengerSaving;
    
    /**
     * Дата закрытия обращения
     */
    @NullRender
    private LocalDateTime requestClosedDatetime;
    
    /**
     * Количество точек с авто чек-ином
     */
    @NullRender
    private Long waypointsCountWithAutoCheckIn;
    
    /**
     * Количество точек с ручным чек-ином
     */
    @NullRender
    private Long waypointsCountWithManualCheckIn;

    /**
     * ID группы исполнителей
     */
    @NullRender
    private String executorGroupId;

    /**
     * Наименование группы исполнителей
     */
    @NullRender
    private String executorGroupName;
    
    @NullRender
    private String attachedOrderCount; // !!!
    
    private Integer departmentIndex; // !!!
}
