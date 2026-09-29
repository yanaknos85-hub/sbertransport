package ru.sber.transport.trip.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Марка
     */
    private String brand;

    /**
     * Модель
     */
    private String model;

    /**
     * Гос. номер
     */
    private String stateNumber;

    /**
     * Цвет
     */
    private String color;

    /**
     * Идентификатор контрагента
     */
    private UUID contractorId;

    /**
     * Признак удаления
     */
    private boolean deleted;

    /**
     * Идентификатор филиала
     */
    private UUID autoparkId;

}
