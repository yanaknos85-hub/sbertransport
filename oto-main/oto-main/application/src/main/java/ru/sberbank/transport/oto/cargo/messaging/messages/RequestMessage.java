package ru.sberbank.transport.oto.cargo.messaging.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

@JsonDeserialize
@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Getter
@Setter
@Builder
public class RequestMessage implements Message<UUID> {
    private UUID id;
    private String humanReadableId;
    private UUID authorId;
    private Employee author;
    private UUID passengerId;
    private Employee passenger;
    private UUID organizationId;
    private String resolution;
    private UUID dispatcherId;
    private LocalDateTime creationTime;
    private String timeZone;
    private LocalDateTime finishedTime;
    private String transportType;
    private List<Waypoint> waypoints;
    private UUID approvalId;
    private UUID tariffId;
    private UUID outcomeTariffId;
    private Map<String, Object> tariff;
    private Map<String, Object> outcomeTariff;
    private LocalDateTime approvalDate;
    private boolean coopTrip;
    private String taxiTripHrId;
    private LocalDateTime desiredDate;
    private LocalDateTime tripConfirmationDate;
    private boolean isSlaExpired;
    private LocalDateTime orderPaymentFormationStartDate;
    private LocalDateTime orderPaymentFormationFinishingDate;
    private String deadlineState;
    private boolean isSuburbTrip;
    private String status;
    private Integer statusCode;
    private int passengerCount;
    private String approvalState;
    private ExpectedData expected;
    private String commentForDriver;
    private boolean sharedRideOwner;
    private UUID rideId;
    private boolean deleted;
    private UUID contractorId;
    private LocalDateTime deadline;
    private LocalDateTime taxiAwaitingSearchStartDate;
    private UUID driverId;
    private UUID autoparkId;
    private UUID vehicleId;
    private VehicleData vehicleData;
    private DriverData driverData;
    private UUID employeeDriverId;
    private Duration tripFactDuration;
    private Double factDistance;
    private EconomyData economyData;
    private Duration driverWaitingTime;
    private Integer numberPassengersJoined;
    private Long additionalSum;
    private Integer rentId;
    private String phoneNumber;
    private LocalDateTime driverArrivedDatetime;
    private String groupTransferClass;
    private boolean vip;
    private Map<String, Object> information;
    private UUID executorGroupId;
    private String executorGroupName;
    private String fraudMessage;

    @JsonIgnoreProperties(
            ignoreUnknown = true
    )
    @Builder
    @Getter
    @Setter
    public static class Employee {
        private String firstName;
        private String lastName;
        private String patronymic;
        private String personnelNumber;
        private UUID departmentId;
        private UUID userId;
        private UUID positionId;
        private UUID delegatedById;
        private String positionName;
        private UUID supervisorId;
        private UUID organizationId;
        private String departmentName;
        private String mvz;
        private String humanReadableId;
        private String mobilePhone;
    }

    public record ExpectedData(double cost, double outcomeCost, double distance, Duration time) {
    }

    @Builder
    @Getter
    @Setter
    public static class VehicleData {
        private String brand;
        private String model;
        private String stateNumber;
        private String color;
    }

    @Builder
    @Getter
    @Setter
    public static class DriverData {
        private String lastName;
        private String firstName;
        private String patronymic;
        private String phoneNumber;
    }

    @Builder
    @Getter
    @Setter
    public static class EconomyData {
        private Double costSharePart;
        private Long savingsCash;
        private Long savingsProcents;
        private Boolean sharedRideOwner;
    }

    public record Waypoint(UUID id, AddressMessage address, Duration waitTime, Boolean checkinAutomatic,
                           Boolean checkinManual, String absenceReason, Integer orderingIndex) {
    }
}
