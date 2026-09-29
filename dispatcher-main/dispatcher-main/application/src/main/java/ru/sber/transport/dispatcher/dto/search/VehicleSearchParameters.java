package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum VehicleSearchParameters implements SortField {

    STATE_NUMBER("stateNumber"),
    AUTOPARK("autopark"),
    MANUFACTURE_YEAR("manufactureYear"),
    MANUFACTURE_YEAR_START("startDateManufactureYear"),
    MANUFACTURE_YEAR_END("endDateManufactureYear"),
    TRANSMISSION("transmissionType"),
    BRAND("brand"),
    MODEL("model"),
    VEHICLE_TYPE("vehicleType"),
    IN_EXPLOITATION("inExploitation");

    private final String name;

}
