package ru.sber.transport.contractor.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceType {

    AUTOSERVICE("Обслуживание автомобилей", false),
    EMPLOYEE_TRANSPORTATION("Пассажирские перевозки", false),
    CARGO_TRANSPORTATION("Перевозки грузов", false),
    INTERNAL_AUTO_PARK("Внутренний автопарк", true);

    private final String rusName;
    private final boolean specialService;

}
