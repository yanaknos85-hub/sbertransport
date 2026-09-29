package ru.sber.transport.telemechanic.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EwbTransportationSubtype {
    /**
     * РП – регулярная перевозка пассажиров и багажа
     */
    REGULAR_PASSENGER_TRANSPORTATION("РП"),
    
    /**
     * ПГ – перевозка грузов на основании договора перевозки грузов или договора фрахтования
     * (в том числе по договору аренды транспортного средства с экипажем)
     */
    CARGO_TRANSPORTATION("ГП"),
    
    /**
     * ЗП – перевозка пассажиров и багажа по заказу
     */
    ON_DEMAND_PASSENGER_TRANSPORTATION("ЗП"),
    
    /**
     * ЛТ – перевозка пассажиров и багажа легковым такси
     */
    PASSENGER_TAXI_TRANSPORTATION("ЛТ"),
    
    /**
     * ПД – организованная перевозка групп детей автобусами
     * (если организованная перевозка группы детей осуществляется по договору фрахтования)
     */
    BUS_TRANSPORTATION_OF_GROUPS_OF_CHILDREN("ПД");
    
    private final String code;
}
