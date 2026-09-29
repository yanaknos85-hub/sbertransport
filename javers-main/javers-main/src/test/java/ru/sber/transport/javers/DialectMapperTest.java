package ru.sber.transport.javers;

import io.qameta.allure.Feature;
import org.javers.common.exception.JaversException;
import org.javers.common.exception.JaversExceptionCode;
import org.javers.repository.sql.DialectName;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.jooq.tools.jdbc.MockConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.jdbc.PgDatabaseMetaData;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.sql.DatabaseMetaData;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings({"DataFlowIssue", "CatchMayIgnoreException"})
@UnitTest
@Isolated
@IsolatedTest
@Feature("lib_javers")
@DisplayName("Проверка определения диалекта")
class DialectMapperTest {

    private final DialectMapper mapper = new DialectMapper();

    public static Stream<Arguments> configsSource() {
        return Stream.of(
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.H2)).findFirst().get(), DialectName.H2),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.MYSQL)).findFirst().get(), DialectName.MYSQL),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.MARIADB)).findFirst().get(), DialectName.MYSQL),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.DEFAULT)).findFirst().get(), null),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.POSTGRES)).findFirst().get(), DialectName.POSTGRES),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.DEFAULT).set(new DefaultConnectionProvider(new MockConnection(null) {
                @Override
                public DatabaseMetaData getMetaData() {
                    return new PgDatabaseMetaData(null) {

                        @Override
                        public String getURL() {
                            return "jdbc:h2:mem";
                        }
                    };
                }
            }))).findFirst().get(), DialectName.H2),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.DEFAULT).set(new DefaultConnectionProvider(new MockConnection(null) {
                @Override
                public DatabaseMetaData getMetaData() {
                    return new PgDatabaseMetaData(null) {

                        @Override
                        public String getURL() {
                            return "jdbc:mysql://localhost:3306";
                        }
                    };
                }
            }))).findFirst().get(), DialectName.MYSQL),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.DEFAULT).set(new DefaultConnectionProvider(new MockConnection(null) {
                @Override
                public DatabaseMetaData getMetaData() {
                    return new PgDatabaseMetaData(null) {

                        @Override
                        public String getURL() {
                            return "jdbc:mariadb://localhost:3306";
                        }
                    };
                }
            }))).findFirst().get(), DialectName.MYSQL),
            Arguments.arguments(Optional.of(new DefaultConfiguration()).stream().peek(c -> c.set(SQLDialect.DEFAULT).set(new DefaultConnectionProvider(new MockConnection(null) {
                @Override
                public DatabaseMetaData getMetaData() {
                    return new PgDatabaseMetaData(null) {

                        @Override
                        public String getURL() {
                            return "jdbc:postgresql://localhost:5432";
                        }
                    };
                }
            }))).findFirst().get(), DialectName.POSTGRES)
        );
    }

    @ParameterizedTest
    @MethodSource("configsSource")
    @DisplayName("Проверка определения диалекта")
    void test_dialect_fromUrl(Configuration config, DialectName dialectName) {
        try {
            var converted = mapper.map(config);
            if (dialectName != null) {
                assertThat(converted).isEqualTo(dialectName);
            } else {
                fail("Dialect mapping MUST be failed");
            }
        } catch (Exception e) {
            if (dialectName == null) {
                assertThat(e).isInstanceOf(JaversException.class)
                    .hasFieldOrPropertyWithValue("code", JaversExceptionCode.UNSUPPORTED_SQL_DIALECT);
            } else {
                fail("Dialect mapping failed", e);
            }
        }
    }

}