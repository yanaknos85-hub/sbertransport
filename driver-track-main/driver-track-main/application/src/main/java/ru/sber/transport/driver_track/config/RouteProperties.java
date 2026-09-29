package ru.sber.transport.driver_track.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "route")
@Validated
public record RouteProperties(
        @Min(1)
        @Max(10)
        int maxGenerationAttempts
) {}

