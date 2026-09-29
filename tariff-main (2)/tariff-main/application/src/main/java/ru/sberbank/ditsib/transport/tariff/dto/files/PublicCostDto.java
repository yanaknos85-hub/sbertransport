package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Объект с ценами тарифа за общественный транспорт.
 */
@Getter
@Setter
public class PublicCostDto {

    /**
     * Цена билета на автобус.
     */
    private Double bus;

    /**
     * Цена билета на троллейбус.
     */
    private Double trolleybus;

    /**
     * Цена билета на трамвай.
     */
    private Double tram;

    /**
     * Цена билета на метро.
     */
    private Double metro;

    /**
     * Цена проездного на автобус.
     */
    private Double travelCardBus;

    /**
     * Цена проездного на троллейбус.
     */
    private Double travelCardTrolleybus;

    /**
     * Цена проездного на трамвай.
     */
    private Double travelCardTram;

    /**
     * Цена проездного на метро.
     */
    private Double travelCardMetro;

}
