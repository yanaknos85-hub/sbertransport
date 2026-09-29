package ru.sber.transport.telemechanic.dto;

import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.time.LocalDate;

public record EwbContractDetails(
        InspectionType inspectionType,
        LocalDate contractStart,
        LocalDate contractEnd
) {
}
