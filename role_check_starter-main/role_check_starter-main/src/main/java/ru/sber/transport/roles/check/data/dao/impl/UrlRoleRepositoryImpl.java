package ru.sber.transport.roles.check.data.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.sber.transport.roles.check.data.dao.UrlRoleRepository;
import ru.sber.transport.roles.common.model.Url;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Repository
class UrlRoleRepositoryImpl implements UrlRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.roles.check.schema:${spring.datasource.hikari.schema:${spring.application.name}}}")
    private String schema;

    @Override
    public Collection<Url> findAllByMethodAndPattern(HttpMethod method, String requestURI) {
        var sql = """
        SELECT * FROM %s INNER JOIN %s ON %s = %s WHERE %s=? AND ? ilike %s
        """.formatted(this.createTable(Url.URL_TABLE), this.createTable(Url.ROLE_TABLE), createSqlPath(Url.URL_TABLE, Url.URL_ID),
                createSqlPath(Url.ROLE_TABLE, Url.ROLE_URL_ID), createSqlPath(Url.URL_TABLE, Url.URL_METHOD), createSqlPath(Url.URL_TABLE, Url.URL_PATTERN));
        return jdbcTemplate.query(sql, new UrlExtractor(), method.name(), requestURI);
    }

    @Override
    public Optional<Url> findByMethodAndRestrictedUrl(HttpMethod method, String requestURI) {
        var sql = """
        SELECT * FROM %s INNER JOIN %s ON %s = %s WHERE %s=? AND ? = %s
        """.formatted(this.createTable(Url.URL_TABLE), this.createTable(Url.ROLE_TABLE), createSqlPath(Url.URL_TABLE, Url.URL_ID),
                createSqlPath(Url.ROLE_TABLE, Url.ROLE_URL_ID), createSqlPath(Url.URL_TABLE, Url.URL_METHOD), createSqlPath(Url.URL_TABLE, Url.URL_URL));
        var result = jdbcTemplate.query(sql, new UrlExtractor(), method.name(), requestURI);
        if (result == null) {
            return Optional.empty();
        }
        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1);
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result.iterator().next());
    }

    @Override
    public Collection<Url> findAllByRole(String role) {
        var sql = """
        SELECT id FROM %s INNER JOIN %s ON %s = %s WHERE %s = ?
        """.formatted(this.createTable(Url.URL_TABLE), this.createTable(Url.ROLE_TABLE), createSqlPath(Url.URL_TABLE, Url.URL_ID),
                createSqlPath(Url.ROLE_TABLE, Url.ROLE_URL_ID), createSqlPath(Url.ROLE_TABLE, Url.ROLE_ROLE));
        var ids = jdbcTemplate.queryForList(sql, UUID.class, role);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        sql = """
        SELECT * FROM %s INNER JOIN %s ON %s = %s WHERE %s in (%s)
        """.formatted(this.createTable(Url.URL_TABLE), this.createTable(Url.ROLE_TABLE), createSqlPath(Url.URL_TABLE, Url.URL_ID),
                createSqlPath(Url.ROLE_TABLE, Url.ROLE_URL_ID), createSqlPath(Url.URL_TABLE, Url.URL_ID), createSqlRequestList(ids));
        return jdbcTemplate.query(sql, new UrlExtractor());
    }

    private String createSqlRequestList(List<UUID> ids) {
        var sqlBuilder = new StringBuilder();
        for (var id : ids) {
            if (!sqlBuilder.isEmpty()) {
                sqlBuilder.append(",");
            }
            sqlBuilder.append("'%s'".formatted(id.toString()));
        }
        return sqlBuilder.toString();
    }

    @Override
    public Url save(Url urlInfo) {
        if (urlInfo.getId() == null) {
            urlInfo.setId(UUID.randomUUID());
            var sql = """
                    INSERT INTO %s ("%s", "%s", "%s", "%s") VALUES (?, ?, ?, ?)
                    """.formatted(this.createTable(Url.URL_TABLE), Url.URL_ID, Url.URL_URL, Url.URL_PATTERN, Url.URL_METHOD);
            jdbcTemplate.update(sql, urlInfo.getId(), urlInfo.getRestrictedUrl(), urlInfo.getPattern(), urlInfo.getMethod().name());
        } else {
            var sql = """
                    UPDATE %s SET "%s" = ?, "%s" = ?,  "%s" = ? WHERE "%s" = ?
                    """.formatted(this.createTable(Url.URL_TABLE), Url.URL_URL, Url.URL_PATTERN, Url.URL_METHOD, Url.URL_ID);
            jdbcTemplate.update(sql, urlInfo.getRestrictedUrl(), urlInfo.getPattern(), urlInfo.getMethod().name(), urlInfo.getId());
        }
        for (var role : urlInfo.getRoles()) {
            var sql = """
                    INSERT INTO %s ("%s", "%s") VALUES (?, ?) ON CONFLICT DO NOTHING
                    """.formatted(this.createTable(Url.ROLE_TABLE), Url.ROLE_ROLE, Url.ROLE_URL_ID);
            jdbcTemplate.update(sql, role, urlInfo.getId());
        }
        return urlInfo;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("""
                TRUNCATE %s CASCADE
                """.formatted(this.createTable(Url.URL_TABLE)));
        jdbcTemplate.update("""
                TRUNCATE %s CASCADE
                """.formatted(this.createTable(Url.ROLE_TABLE)));
    }

    private static class UrlExtractor implements ResultSetExtractor<Collection<Url>> {
        @Override
        public Collection<Url> extractData(@NonNull ResultSet resultSet) throws SQLException, DataAccessException {
            var urls = new HashMap<UUID, Url>();
            while (resultSet.next()) {
                var id = resultSet.getObject(Url.URL_ID, UUID.class);
                var existUrl = urls.computeIfAbsent(id, urlId -> getUrl(urlId, resultSet));
                existUrl.getRoles().add(resultSet.getObject(Url.ROLE_ROLE, String.class));
            }
            return urls.values();
        }

        @SneakyThrows(SQLException.class)
        private Url getUrl(UUID id, ResultSet resultSet) {
            return Url.create(
                    id,
                    HttpMethod.valueOf(resultSet.getObject(Url.URL_METHOD, String.class)),
                    resultSet.getObject(Url.URL_URL, String.class),
                    resultSet.getObject(Url.URL_PATTERN, String.class)
            );
        }

    }

    private String createTable(String table) {
        return createSqlPath(schema, table);
    }

    private String createSqlPath(String container, String child) {
        return """
                "%s"."%s"
                """.formatted(container, child);
    }
}
