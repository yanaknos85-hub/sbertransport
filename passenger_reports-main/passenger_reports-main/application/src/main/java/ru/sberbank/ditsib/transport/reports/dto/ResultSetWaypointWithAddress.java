package ru.sberbank.ditsib.transport.reports.dto;

import lombok.Getter;

import java.time.Duration;
import java.util.UUID;

@Getter
public class ResultSetWaypointWithAddress {
    final UUID request_id;
    final Integer ordering_index;
    final String building;
    final String city;
    final String country;
    final String house;
    final String region;
    final String street;
    final String structure;
    final Boolean checkin_automatic;
    final Boolean checkin_manual;
    final Duration wait_time;
    final Boolean exist_in_vsp_tb_registry;
    
    public ResultSetWaypointWithAddress(
            UUID request_id,
            Integer ordering_index,
            String building,
            String city,
            String country,
            String house,
            String region,
            String street,
            String structure,
            Boolean checkin_automatic,
            Boolean checkin_manual,
            Duration wait_time,
            Boolean exist_in_vsp_tb_registry
                                       ) {
        this.request_id = request_id;
        this.ordering_index = ordering_index;
        this.building = building;
        this.city = city;
        this.country = country;
        this.house = house;
        this.region = region;
        this.street = street;
        this.structure = structure;
        this.checkin_automatic = checkin_automatic;
        this.checkin_manual = checkin_manual;
        this.wait_time = wait_time;
        this.exist_in_vsp_tb_registry = exist_in_vsp_tb_registry;
    }
}
