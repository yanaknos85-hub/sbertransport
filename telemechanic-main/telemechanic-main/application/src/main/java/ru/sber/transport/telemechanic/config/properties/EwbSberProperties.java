package ru.sber.transport.telemechanic.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * Настройки для формирования полей ЭПЛ в случае, если выбрана одна из организаций Сбербанка
 */
@ConfigurationProperties(prefix = "ewb-sber")
public record EwbSberProperties(UUID id, String name, String msrn, String tin, String phone) {}
