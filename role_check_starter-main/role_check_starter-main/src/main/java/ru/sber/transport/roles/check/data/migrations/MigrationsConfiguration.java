package ru.sber.transport.roles.check.data.migrations;

import org.springframework.context.annotation.Import;

/**
 * Конфигурация миграций.
 */
@Import(DatabaseRolesMigration.class)
public class MigrationsConfiguration {
}
