package ru.sber.transport.telemechanic.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "korus")
public record KorusProperties (String url, String login, String password, String token) {}
