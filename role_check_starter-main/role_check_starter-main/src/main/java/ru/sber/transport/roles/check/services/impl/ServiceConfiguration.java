package ru.sber.transport.roles.check.services.impl;

import org.springframework.context.annotation.Import;

/**
 * Конфигурация сервисов.
 */
@Import({RoleCheckServiceImpl.class, UrlRolesServiceImpl.class})
public class ServiceConfiguration {
}
