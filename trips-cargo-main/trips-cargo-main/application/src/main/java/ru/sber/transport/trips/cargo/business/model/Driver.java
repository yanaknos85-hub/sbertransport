package ru.sber.transport.trips.cargo.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver implements HasName{

    private UUID id;

    private String humanReadableId;

    private String lastName;

    private String firstName;

    private String patronymic;

    private UUID contractorId;

    private boolean active;

    private int rating;

    private String driverLicenseNumber;

    private String cargoLicenceNumber;

    private String serviceLicenseNumber;

    private Double latitude;

    private Double longitude;

    private ZonedDateTime pointTime;

    private String timeZone;

    private boolean serving;

    private boolean online;

    private UUID activeTripId;

    private UUID shiftId;

    private String experience;

    private String contactPhone;

    private String email;

    private String passport;

    private boolean consent;

    private Double azimuth;

    private UUID oauthId;

    private UUID autoparkId;
}
