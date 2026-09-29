package ru.sberbank.ditsib.transport.reports.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Slf4j
@RequiredArgsConstructor
public class GroupTransferResponseSqlResultSetMapping {
    
    private final UUID id;
    
    private final String humanreadableid;
    
    @JsonProperty("author_id")
    private final UUID authorId;
    
    @JsonProperty("passenger_id")
    private final UUID passengerId;
    
    @JsonProperty("personal_car")
    private final UUID personalCar;
    
    @JsonProperty("creation_time")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime creationTime;
    
    @JsonProperty("desired_date")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime desiredDate;
    
    @JsonProperty("approval_date")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime approvalDate;
    
    @JsonProperty("transport_type")
    private final String transportType;
    
    @JsonProperty("request_status")
    private final String requestStatus;
    
    @JsonProperty("request_status_code")
    private final Integer requestStatusCode;
    
    @JsonProperty("purpose_id")
    private final UUID purposeId;
    
    @JsonProperty("passenger_count")
    private final Integer passengerCount;
    
    @JsonProperty("expected_cost")
    private final Double expectedCost;
    
    @JsonProperty("expected_distance")
    private final Double expectedDistance;
    
    @JsonProperty("expected_time")
    private final Duration expectedTime;
    
    @JsonProperty("rating_mark")
    private final Integer ratingMark;
    
    @JsonProperty("rating_comment")
    private final String ratingComment;
    
    @JsonProperty("change_date")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime changeDate;
    
    @JsonProperty("time_zone")
    private final String timeZone;
    
    @JsonProperty("tariff_id")
    private final UUID tariffId;
    
    @JsonProperty("group_transfer_class")
    private final String groupTransferClass;
    
    @JsonProperty("approval_state")
    private final String approvalState;
    
    @JsonProperty("approved_by")
    private final UUID approvedBy;
    
    @JsonProperty("contractor_id")
    private final UUID contractorId;
    
    @JsonProperty("comment_for_driver")
    private final String commentForDriver;
    
    @JsonProperty("finished_time")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime finishedTime;
    
    private final String driver; //jsonb
    
    private final String vehicle; //jsonb
    
    @JsonProperty("passenger_department1")
    private final String passengerDepartment1;
    
    @JsonProperty("passenger_department2")
    private final String passengerDepartment2;
    
    @JsonProperty("passenger_department3")
    private final String passengerDepartment3;
    
    @JsonProperty("passenger_department4")
    private final String passengerDepartment4;
    
    @JsonProperty("passenger_department5")
    private final String passengerDepartment5;
    
    @JsonProperty("passenger_department6")
    private final String passengerDepartment6;
    
    @JsonProperty("departure_address")
    private final String departureAddress;
    
    @JsonProperty("intermediate_addresses")
    private final String intermediateAddresses;
    
    @JsonProperty("destination_address")
    private final String destinationAddress;
    
    private final String resolution;
    
    @JsonProperty("organization_id")
    private final UUID organizationId;
    
    @JsonProperty("limit_id")
    private final UUID limitId;
    
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime deadline;
    
    @JsonProperty("deadline_state")
    private final String deadlineState;
    
    @JsonProperty("driver_arrived_datetime")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime driverArrivedDatetime;
    
    @JsonProperty("cost_center")
    private final String costCenter;
    
    @JsonProperty("request_closed_datetime")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime requestClosedDatetime;
    
    private final Boolean vip;
    
    private final String source;
    
    @JsonProperty("comment_for_purpose")
    private final String commentForPurpose;
    
    @JsonProperty("savings_cash")
    private final Long savingsCash;
    
    @JsonProperty("min_taxi_tariff_cost")
    private final Long minTaxiTariffCost;
    
    @JsonProperty("contract_number")
    private final String contractNumber;
    
    @JsonProperty("executor_group_id")
    private final UUID executorGroupId;
    
    @JsonProperty("executor_group_name")
    private final String executorGroupName;
}
