package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DriverSearchParameters implements SortField {

    DRIVER_HUMAN_ID("driverHumanId"),
    DRIVER_FULL_NAME("driverFullName"),
    ACTIVE("isActive"),
    RATING("ratingFrom"),

    DRIVER_LICENSES("driverLicenses"),
    DRIVER_SPECIALITY("driverSpeciality"),
    AUTOPARK_ID("autoparkId"),
    PERSONNEL_NUMBER("personnelNumber");

    private final String name;

}
