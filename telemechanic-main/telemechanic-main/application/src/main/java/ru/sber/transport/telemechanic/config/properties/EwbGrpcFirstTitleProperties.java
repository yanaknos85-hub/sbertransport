package ru.sber.transport.telemechanic.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ewb-grpc.first-title")
public record EwbGrpcFirstTitleProperties(int maxRequestSize) {
}
