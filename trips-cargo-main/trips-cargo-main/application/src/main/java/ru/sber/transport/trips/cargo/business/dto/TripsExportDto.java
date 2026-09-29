package ru.sber.transport.trips.cargo.business.dto;

import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Описание структуры данных импорта/экспорта.
 */
public record TripsExportDto(
        int number,
        String id,
        String routeId,
        String requestIds,
        StartTimeDto start,
        Route route,
        Checkin checkin,
        String status,
        String comment,
        String author,
        String vehicleNumber,
        NameDto driver,
        @NullRender
        StartTimeDto creationTime,
        Expected expected,
        Fact fact,
        String tripType,
        @NullRender
        String contractNumber,
        @NullRender
        Tariff tariff,
        @NullRender
        String payPlace,
        DispatcherDto dispatcher,
        DispatcherDataDto dispatcherData,
        Integer loaders
) {

    public record StartTimeDto(LocalDate date, LocalTime time) {}

    public record NameDto(String fullName) {}

    public record DispatcherDto(String fullName, String phone) {}

    public record DispatcherDataDto(@NullRender @TimeFormat(TimeFormat.Format.MINUTES) Duration driverWaitTime) {}

    public record Route(@NullRender String start, @NullRender String waypoints, @NullRender String end) {}

    public record Expected(@NullRender Double distances, @TimeFormat(TimeFormat.Format.MINUTES) Duration waitTime, @NullRender Double costs) {}

    public record Fact(@NullRender Double distance, @NullRender Integer cost, @NullRender @TimeFormat(TimeFormat.Format.MINUTES) Duration time, @TimeFormat(TimeFormat.Format.MINUTES) Duration waitTime) {}

    public record Tariff(@NullRender @TimeFormat(TimeFormat.Format.MINUTES) Duration waitTime, @NullRender Double cost) {}

    public record Checkin(@NullRender Integer quantity, @NullRender String status, @NullRender String waypointConfirmation) {}

}