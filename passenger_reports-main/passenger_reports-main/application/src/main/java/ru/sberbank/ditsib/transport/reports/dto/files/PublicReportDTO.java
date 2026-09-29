package ru.sberbank.ditsib.transport.reports.dto.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PublicReportDTO {
    
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
     * Дата согласования поездки
     */
    @NullRender
    private LocalDateTime approvalDate;
    
    /**
     * Дата выплаты
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
     * Выбранный вид транспорта
     */
    @NullRender
    private String transportType;
    
    /**
     * Статус поездки
     */
    @NullRender
    private String status;
    
    /**
     * Стоимость поездки, руб
     */
    @NullRender
    private Double requestExpectedCost;
    
    /**
     * Расстояние, КМ
     */
    @NullRender
    private Double requestExpectedDistance;
    
    /**
     * Есть вложение (в заявку вложен билет/картинка)
     */
    @NullRender
    private String hasAttachment;
    
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
     * Выбранный вид компенсации
     */
    @NullRender
    private String compensationType;
    
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
    private String orderPaymentFormationStartDate;
    
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
     * Сумма к выплате
     */
    private Double paymentCost;
    
    /**
     * Сумма к выплате по коду 4664
     */
    private Double sum4664;
    
    /**
     * Сумма к выплате по коду 4666
     */
    private Double sum4666;
    
    /**
     * Источник данных
     */
    private String source;
    
    private Double minTaxiTariffCost;
    
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
     * Экономия.
     */
    private String savings;
    
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
     * Контрольный срок подачи ТС
     */
    @NullRender
    private LocalDateTime deadline;
    
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
     * Количество точек удаленных из маршрута
     */
    @NullRender
    private Long waypointsCountWithoutCheckIn;
    
    /**
     * Кол-во пунктов маршрута общее
     */
    @NullRender
    private Integer waypointsCount;

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
    
    /**
     * Наименование тарифа
     */
    @NullRender
    private String tariffName;
    
    @NullRender
    private String publicTransportType;
    
    private boolean publicCompensationDocumentExist;
    
    private int attachedDocuments;
    
    private int departmentIndex;
}
