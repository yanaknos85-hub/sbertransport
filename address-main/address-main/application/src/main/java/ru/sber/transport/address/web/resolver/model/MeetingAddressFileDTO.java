package ru.sber.transport.address.web.resolver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Объект адреса филиала из файла.
 */
@Setter
@Getter
@NoArgsConstructor
public class MeetingAddressFileDTO {

    /**
     * Country.
     */
    private String country;

    /**
     * Region.
     */
    private String region;

    /**
     * City.
     */
    private String city;

    /**
     * Street.
     */
    private String street;

    /**
     * House.
     */
    private String house;

    /**
     * Building.
     */
    private String building;

    /**
     * Structure.
     */
    private String structure;

    /**
     * Label
     */
    private String label;
}