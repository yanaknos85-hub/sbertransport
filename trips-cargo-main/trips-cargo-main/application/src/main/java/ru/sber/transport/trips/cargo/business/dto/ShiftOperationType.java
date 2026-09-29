package ru.sber.transport.trips.cargo.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Тип операции со сменой
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ShiftOperationType {

    DEACTIVATION("деактивация"),

    ACTIVATION("активация"),

    EXIT("Выход"),

    ENTER("Вход"),

    TRIP_PLANNING("Планирование поездки");

    private String name;

}
