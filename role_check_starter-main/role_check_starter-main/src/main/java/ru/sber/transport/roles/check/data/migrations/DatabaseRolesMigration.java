package ru.sber.transport.roles.check.data.migrations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Конфигурация миграций.
 * <p>
 * Добавляет в конец сценария миграции таблицы ролевой модели.
 */
@Slf4j
class DatabaseRolesMigration implements InitializingBean {

    private final CompletableFuture<String> schemaNameFuture = new CompletableFuture<>();

    private final CompletableFuture<JdbcTemplate> jdbcTemplateFuture = new CompletableFuture<>();

    private final ConfigurableApplicationContext context;

    /**
     * Объект, проверяющий наличие таблицы ролей.
     *
     * @param context контекст.
     */
    public DatabaseRolesMigration(ConfigurableApplicationContext context) {
        this.context = context;
        Executors.newSingleThreadExecutor().submit(() -> {
            schemaNameFuture.complete(setRolesProperties(context.getEnvironment()));
            try {
                jdbcTemplateFuture.complete(context.getBean(JdbcTemplate.class));
            } catch (Exception e) {
                jdbcTemplateFuture.completeExceptionally(e);
            }
        });
    }

    private String setRolesProperties(ConfigurableEnvironment environment) {
        var name = environment.getProperty("spring.application.name");
        var appSchemaName = environment.getProperty("spring.datasource.hikari.schema");
        if (appSchemaName == null) {
            appSchemaName = environment.getProperty("spring.jpa.properties.hibernate.default_schema");
        }
        if (appSchemaName == null) {
            appSchemaName = name;
        }

        var properties = new HashMap<String, Object>();
        properties.put("spring.datasource.hikari.schema", appSchemaName);
        properties.put("spring.jpa.properties.hibernate.default_schema", appSchemaName);
        properties.put("spring.roles.check.schema", appSchemaName);

        var source = new MapPropertySource("authorization", properties);

        environment.getPropertySources().addFirst(source);

        return appSchemaName;
    }

    @Override
    public void afterPropertiesSet() {
        jdbcTemplateFuture.thenAccept(this::useJdbcTemplate).exceptionally(e -> {
            log.error("Creating database connection failed. Role checking is not available", e);
            context.close();
            return null;
        });
    }

    private void useJdbcTemplate(JdbcTemplate jdbcTemplate) {
        schemaNameFuture.thenAccept(schemaName -> checkRolesTable(jdbcTemplate, schemaName));
    }

    private void checkRolesTable(JdbcTemplate jdbcTemplate, String schemaName) {
        var existsSql = """
                select count(*)
                   from information_schema.tables
                   where table_schema= '%1$s' and table_name = 'urls'""".formatted(schemaName);
        Long count;
        try {
            count = jdbcTemplate.queryForObject(existsSql, Long.class);
        } catch (Exception e) {
            log.error("Checking for role table failed", e);
            context.close();
            return;
        }
        if (count == null || count == 0) {
            log.info("Tables for checking roles not found. New tables will be created.");
            try {
                addTables(jdbcTemplate, schemaName);
            } catch (Exception e) {
                log.error("Failed to add tables", e);
                context.close();
                return;
            }
        }
        log.info("Role checking is available now.");
    }

    private void addTables(JdbcTemplate jdbcTemplate, String schemaName) {
        try {
            var createUrls = """
                    CREATE TABLE IF NOT EXISTS "%1$s"."urls" (
                        "id" UUID PRIMARY KEY,
                        "url" varchar(255) NOT NULL,
                        "pattern" varchar(255) NOT NULL,
                        "method" varchar(255) NOT NULL,
                        constraint %1$s_urls_method_url_uk UNIQUE("url", "method"),
                        constraint %1$s_urls_method_url_pattern_uk UNIQUE("url", "method", "pattern")
                    )
                    """.formatted(schemaName);
            jdbcTemplate.update(createUrls);
        } catch (Exception e) {
            throw new IllegalStateException("Creating urls table failed", e);
        }
        try {
            var createRoles = """
                    CREATE TABLE IF NOT EXISTS "%1$s"."roles" (
                        "role" VARCHAR(255) NOT NULL,
                        "url_id" UUID NOT NULL,
                        primary key ("role", "url_id")
                    )
                    """.formatted(schemaName);
            jdbcTemplate.update(createRoles);
        } catch (Exception e) {
            throw new IllegalStateException("Creating roles table failed", e);
        }
    }
}
