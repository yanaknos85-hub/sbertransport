package ru.sber.transport.roles.check.controller.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация контроллеров.
 */
@Configuration
@Import(UrlEndpointAllowControllerImpl.class)
public class ControllersConfiguration {
}
