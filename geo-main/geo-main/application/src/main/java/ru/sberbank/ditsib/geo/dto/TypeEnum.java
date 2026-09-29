package ru.sberbank.ditsib.geo.dto;

/**
 * Available types of route.
 */
public enum TypeEnum {

    /**
     * Exclude jams.
     */
    JAM,

    /**
     * Request statistics.
     */
    STATISTIC,

    /**
     * Request shortest route.
     */
    SHORTEST
}