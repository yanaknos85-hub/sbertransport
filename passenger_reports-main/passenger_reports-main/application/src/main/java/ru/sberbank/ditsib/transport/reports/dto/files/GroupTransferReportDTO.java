package ru.sberbank.ditsib.transport.reports.dto.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

@Getter
@Setter
@NoArgsConstructor
public class GroupTransferReportDTO {
    
    /**
     * Номер заявки
     */
    @NullRender
    private String requestId;
    
    /**
     * Статус поездки
     */
    @NullRender
    private String requestStatus;
    
    /**
     * Код статуса с расшифровкой 201 - Отмена пользователем 202 - Не согласовано 203 - Не согласовано по истечению срока 204 - Не утверждено 205 - Не
     * утверждено по истечению срока 206 - По истечению срока 207 - Отмена водителем ЛТ
     */
    @NullRender
    private String statusCode;
    
    /**
     * Вид тарифа
     */
    @NullRender
    private String groupTransferClass;
    
    /**
     * Количество точек в маршруте
     */
    @NullRender
    private Integer waypointsCount;
    
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
     * Комментарий для водителя
     */
    @NullRender
    private String commentForDriver;
    
    /**
     * МВЗ
     */
    @NullRender
    private String mvz;
    
    /**
     * ID лимита
     */
    @NullRender
    private String limitId;
    
    /**
     * Код подразделения. ОЕ
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
     * Табельный номер ВК
     */
    @NullRender
    private String passengerPersonnelNumber;
    
    /**
     * ФИО пассажира
     */
    @NullRender
    private String passengerFio;
    
    /**
     * Разъездной характер деятельности
     */
    @NullRender
    private String passengerActivity;
    
    /**
     * Должность пассажира
     */
    @NullRender
    private String passengerPosition;
    
    /**
     * Табельный номер Заявителя
     */
    @NullRender
    private String customerPersonnelNumber;
    
    /**
     * Заявитель ФИО
     */
    @NullRender
    private String customerFio;
    
    /**
     * Табельный номер Согласующего
     */
    @NullRender
    private String approverPersonnelNumber;
    
    /**
     * Согласующий ФИО
     */
    @NullRender
    private String approverFio;
    
    /**
     * ID тарифа
     */
    @NullRender
    private String tariffId;
    
    /**
     * Цель поездки
     */
    @NullRender
    private String tripPurpose;
    
    /**
     * Адрес отправления
     */
    @NullRender
    private String departureAddress;
    
    /**
     * Промежуточные адреса
     */
    @NullRender
    private String intermediateAddresses;
    
    /**
     * Адрес прибытия
     */
    @NullRender
    private String destinationAddress;
    
    /**
     * Дата и время создания заявки
     */
    @NullRender
    private String creationDateTime;
    
    /**
     * Дата и время выполнения заявки
     */
    @NullRender
    private String executionDateOfRequest;
    
    /**
     * Дата закрытия обращения
     */
    @NullRender
    private String finishedTime;
    
    /**
     * Желаемое время отправления
     */
    @NullRender
    private String desiredTime;
    
    /**
     * Желаемая дата отправления
     */
    @NullRender
    private String desiredDate;
    
    /**
     * Мобильный телефон ВК-пассажира
     */
    @NullRender
    private String passengerMobilePhone;
    
    /**
     * Исполнитель
     */
    @NullRender
    private String contractorName;
    
    /**
     * Предварительный километраж (обращ), км
     */
    @NullRender
    private Double requestExpectedDistance;
    
    /**
     * Фактический километраж (обращ)
     */
    @NullRender
    private Double tripFactDistance;
    
    /**
     * Условная стоимость (обращ)
     */
    @NullRender
    private Double requestExpectedCost;
    
    /**
     * Фактическая стоимость (обращ)
     */
    @NullRender
    private Double tripFactPrice;
    
    /**
     * Предварительное время поездки (обращ)
     */
    @NullRender
    private String requestExpectedTime;
    
    /**
     * Фактическое время поездки (обращ), мин.
     */
    @NullRender
    private String tripFactDuration;
    
    /**
     * Время ожидания в промежуточной точке, мин
     */
    @NullRender
    private String waypointWaitTime;
    
    /**
     * Фактическое время ожидания (обращ), мин.
     */
    @NullRender
    private String actualWaitingTime;
    
    /**
     * Тип поездки
     */
    @NullRender
    private String tripType;
    
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
     * Инициатор совместной поездки
     */
    @NullRender
    private String sharedRideOwner;
    
    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    private Integer numberPassengersJoined;
    
    /**
     * Контрольный срок подачи ТС
     */
    @NullRender
    private String deadline;
    
    /**
     * Фактическое время подачи ТС
     */
    @NullRender
    private String driverArrivedDatetime;
    
    /**
     * Нарушение КС (да/нет)
     */
    private String deadlineViolation;
    
    /**
     * Наименование организации
     */
    private String organizationOfficialName;
    
    /**
     * Рабочая группа
     */
    private String workGroup;
    
    /**
     * Количество забронированных мест (без учета водителя)
     */
    private Integer passengerCount;
    
    private String requestClosedDatetime;
    
    /**
     * Признак vip
     */
    private boolean vip;
    
    /**
     * Источник данных
     */
    private String source;
    
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
     * Стоимость минимальной поездки на такси
     */
    private Double minTaxiTariffCost;

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
}
