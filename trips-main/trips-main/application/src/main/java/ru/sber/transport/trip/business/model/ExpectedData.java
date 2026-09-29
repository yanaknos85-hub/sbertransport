package ru.sber.transport.trip.business.model;

import java.time.Duration;

public record ExpectedData(
        long cost,
        double distance,
        Duration time
) {
}
