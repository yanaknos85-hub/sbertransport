package ru.sber.transport.trip.business.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DriverLocationSearchParameters implements SortField {

    LATITUDE("latitude"),

    LONGITUDE("longitude"),

    DEADLINE("deadline"),

    NAME("name");

    private final String name;

}
