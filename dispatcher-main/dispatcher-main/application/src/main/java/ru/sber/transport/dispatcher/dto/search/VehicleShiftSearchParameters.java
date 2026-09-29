package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.dispatcher.database.model.CarModel_;
import ru.sber.transport.dispatcher.database.model.Vehicle_;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum VehicleShiftSearchParameters implements SortField {

    MODEL(Vehicle_.MODEL + "." + CarModel_.NAME),

    BRAND(Vehicle_.MODEL + "." + CarModel_.BRAND);

    private final String name;

}
