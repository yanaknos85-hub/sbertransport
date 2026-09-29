package ru.sberbank.ditsib.transport.reports.dto.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

@Getter
@Setter
@NoArgsConstructor
public class CarsharingReportRegisterDTO {
    
    @NullRender
    private String humanReadableId;
    
    @NullRender
    private String organization;
    
    @NullRender
    private String departmentCode;
    
    @NullRender
    private String contractor;
    
    @NullRender
    private String rentId;
    
    @NullRender
    private String fio;
    
    @NullRender
    private String personnelNumber;
    
    @NullRender
    private String position;
    
    @NullRender
    private String phoneNumber;
    
    private String creationTime;
    
    private String desiredDate;
    
    private String approveDate;
    
    private boolean coopTrip;
    
    private String mvz;
    
    @NullRender
    private String tripType;
    
    private String status;
    
    private String car;
    
    private String rentCreatedTime;
    
    private String rentFinishedTime;
    
    private String startAddress;
    
    private String finishAddress;
    
    private long expectedTime;
    
    private Integer reserveTime;
    
    private Integer drivingTime;
    
    private Integer parkingTime;
    
    private Double expectedDistance;
    
    private Integer drivingLength;
    
    private Double reserveTimeCost;
    
    private Double expectedCost;
    
    private Double drivingTimeCost;
    
    private Double parkingTimeCost;
    
    private Double drivingLengthCost;
    
    private Double totalCost;
    
    private String departureAddress;
    
    private String intermediateAddresses;
    
    private String destinationAddress;
    
    private String passengerDepartment1;
    
    private String passengerDepartment2;
    
    private String passengerDepartment3;
    
    private String passengerDepartment4;
    
    private String passengerDepartment5;
    
    private String passengerDepartment6;
    
    private Integer passengerCount;
    
    /**
     * Список присоединённых пассажиров
     */
    private String joinedPassengers;
    
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
     * Экономия в рублях
     */
    @NullRender
    private Double savingsCash;
    
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
    private String requestClosedDatetime;
    
    /**
     * Cумма в списания лимита
     */
    private Double limitDebit;
    
    /**
     * Экономия бюджета для подразделения
     */
    private Double departmentEconomy;
    
    /**
     * Итоговый финансовый эффект для организации
     */
    private Double financialImpact;
    
    /**
     * Итоговый финансовый эффект для организации (попутчики)
     */
    private Double financialImpactJoined;

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
