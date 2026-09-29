package ru.sber.transport.driver_track.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "geo")
public record GeoProperties(
    @DefaultValue("500")
    @Positive
    int batchSize,

    @DefaultValue("0.00001")
    @Positive
    double coordinateEpsilon
) {
}
