package ru.sberbank.ditsib.transport.reports.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Slf4j
@RequiredArgsConstructor
public class PersonalResponseSqlResultSetMapping {
    
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
    
    @JsonProperty("order_payment_formation_start_date")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime orderPaymentFormationStartDate;
    
    @JsonProperty("transport_type")
    private final String transportType;
    
    @JsonProperty("request_status")
    private final String requestStatus;
    
    @JsonProperty("request_status_code")
    private final Integer requestStatusCode;
    
    @JsonProperty("purpose_id")
    private final UUID purposeId;
    
    @JsonProperty("coop_trip")
    private final boolean coopTrip;
    
    @JsonProperty("shared_ride_id")
    private final UUID sharedRideId;
    
    @JsonProperty("passenger_count")
    private final Integer passengerCount;
    
    @JsonProperty("expected_cost")
    private final Double expectedCost;
    
    @JsonProperty("expected_distance")
    private final Double expectedDistance;
    
    @JsonProperty("expected_time")
    private final Duration expectedTime;
    
    @JsonProperty("payment_price_insurance")
    private final Long paymentPriceInsurance;
    
    @JsonProperty("payment_price_main")
    private final Long paymentPriceMain;
    
    @JsonProperty("payment_price_optional")
    private final Long paymentPriceOptional;
    
    @JsonProperty("payment_type_code_insurance")
    private final Integer paymentTypeCodeInsurance;
    
    @JsonProperty("payment_type_code_main")
    private final Integer paymentTypeCodeMain;
    
    @JsonProperty("payment_type_code_optional")
    private final Integer paymentTypeCodeOptional;
    
    @JsonProperty("rating_mark")
    private final Integer ratingMark;
    
    @JsonProperty("rating_comment")
    private final String ratingComment;
    
    @JsonProperty("change_date")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime changeDate;
    
    @JsonProperty("time_zone")
    private final String timeZone;
    
    @JsonProperty("cost_share_part")
    private final Double costSharePart;
    
    @JsonProperty("savings_cash")
    private final Long savingsCash;
    
    @JsonProperty("savings_procents")
    private final Long savingsProcents;
    
    @JsonProperty("shared_ride_owner")
    private final Boolean sharedRideOwner;
    
    @JsonProperty("number_passengers_joined")
    private final Integer numberPassengersJoined;
    
    @JsonProperty("additional_sum")
    private final Long additionalSum;
    
    @JsonProperty("tariff_id")
    private final UUID tariffId;
    
    @JsonProperty("employee_driver_id")
    private final UUID employeeDriverId;
    
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
    
    @JsonProperty("cost_center")
    private final String costCenter;
    
    @JsonProperty("joined_passengers")
    private final String joinedPassengers;
    
    private final String source;
    
    @JsonProperty("min_taxi_tariff_cost")
    private final Long minTaxiTariffCost;
    
    @JsonProperty("comment_for_purpose")
    private final String commentForPurpose;
    
    @JsonProperty("deadline_state")
    private final String deadlineState;
    
    @JsonProperty("finished_time")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime finishedTime;
    
    @JsonProperty("request_closed_datetime")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime requestClosedDatetime;
    
    @JsonProperty("executor_group_id")
    private final UUID executorGroupId;
    
    @JsonProperty("executor_group_name")
    private final String executorGroupName;
    
    @JsonProperty("trip_end_time")
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime tripStartTime;
    
    @JsonDeserialize(using = DatabaseLocalDateTimeDeserializer.class)
    private final LocalDateTime deadline;
    
    private final Set<String> ratingAdvantages = new HashSet<>();
    
    private final Set<String> ratingDrawbacks = new HashSet<>();
}
