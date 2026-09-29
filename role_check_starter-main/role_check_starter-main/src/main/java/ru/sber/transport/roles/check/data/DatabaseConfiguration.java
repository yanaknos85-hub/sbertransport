package ru.sber.transport.roles.check.data;

import org.springframework.context.annotation.Import;
import ru.sber.transport.roles.check.data.dao.impl.RepositoryConfiguration;
import ru.sber.transport.roles.check.data.migrations.MigrationsConfiguration;

/**
 * Конфигурация базы данных.
 */
@Import({MigrationsConfiguration.class, RepositoryConfiguration.class})
public class DatabaseConfiguration {
}
