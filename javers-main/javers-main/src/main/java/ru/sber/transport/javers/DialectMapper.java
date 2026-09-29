package ru.sber.transport.javers;

import lombok.SneakyThrows;
import org.javers.common.exception.JaversException;
import org.javers.common.exception.JaversExceptionCode;
import org.javers.repository.sql.DialectName;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.springframework.lang.NonNull;

import java.sql.SQLException;

/**
 *  Маппер диалектов.
 */
public class DialectMapper {

    /**
     * Получить Javers-диалект по диалекту jOOQ'а.
     *
     * @param config конфигурация приложения.
     * @return Javers-диалект.
     */
    public DialectName map(@NonNull Configuration config) {
        var dialect = getDialect(config);
        return switch (dialect) {
            case H2 -> DialectName.H2;
            case MYSQL, MARIADB -> DialectName.MYSQL;
            case POSTGRES -> DialectName.POSTGRES;
            default -> throw new JaversException(JaversExceptionCode.UNSUPPORTED_SQL_DIALECT, dialect);
        };
    }

    @SneakyThrows({SQLException.class})
    private @NonNull SQLDialect getDialect(Configuration config) {
        var configuredDialect = config.dialect();
        if (SQLDialect.DEFAULT.equals(configuredDialect)) {
            try (var connection = config.connectionProvider().acquire()) {
                if (connection != null) {
                    var metadata = connection.getMetaData();
                    var url = metadata.getURL();
                    if (url.contains(":postgresql:")) {
                        return SQLDialect.POSTGRES;
                    }
                    if (url.contains(":h2:")) {
                        return SQLDialect.H2;
                    }
                    if (url.contains(":mysql:")) {
                        return SQLDialect.MYSQL;
                    }
                    if (url.contains(":mariadb:")) {
                        return SQLDialect.MARIADB;
                    }
                }
            }
        }
        return configuredDialect;
    }
}
