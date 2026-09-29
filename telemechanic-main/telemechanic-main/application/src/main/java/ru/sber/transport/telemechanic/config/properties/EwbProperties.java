package ru.sber.transport.telemechanic.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ewb")
public record EwbProperties(boolean path) {
}
