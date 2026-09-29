package ru.sber.transport.audit.writer.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.audit.Result;
import ru.sber.transport.audit.resolver.AuthenticatedResolver;
import ru.sber.transport.audit.resolver.DbResolver;
import ru.sber.transport.test.log.TestLogger;
import ru.sber.transport.utils.collections.MapUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Проверка транслятора аудита")
@UnitTest
@IsolatedTest
@Feature("lib_audit")
class AuditWriterImplTest {

    private final AuthenticatedResolver authenticatedResolver = mock(AuthenticatedResolver.class);
    private final DbResolver dbResolver = mock(DbResolver.class);

    @Test
    @DisplayName("Проверка записи")
    void test_write() {
        var infos = new ArrayList<String>();
        var logger = TestLogger.builder().infoConsumer((text, ignore) -> infos.add(text)).build();

        when(authenticatedResolver.resolveUser(any())).thenAnswer(inv -> inv.getArgument(0, Authentication.class).getName());
        doNothing().when(dbResolver).save(anyString(), anyString(), any(LocalDateTime.class), anyString());

        var writer = new AuditWriterImpl(
                new ObjectProvider<>() {
                    @Override
                    public AuthenticatedResolver getObject(Object... args) throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfAvailable() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfUnique() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getObject() throws BeansException {
                        return authenticatedResolver;
                    }
                },
                new ObjectProvider<DbResolver>() {
                    @Override
                    public DbResolver getObject(Object... args) throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfAvailable() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfUnique() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getObject() throws BeansException {
                        return dbResolver;
                    }
                },
                mock(MapUtils.class)) {

            @Override
            Logger log() {
                return logger;
            }

        };

        writer.write("source", "action {param1}", "", new String[]{}, new UsernamePasswordAuthenticationToken("user", "password"),
                Result.SUCCESS, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, "10.6.7.55");

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0)).isEqualTo("AUDIT: source = source; user = user; id = user; IPv4 = 10.6.7.55; action = action arg1; result = SUCCESS");
        verifyNoInteractions(dbResolver);

        var jwt = Jwt
                .withTokenValue("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9FTkdJTkVFUl9DT1JQX0NMSUVOVCIsIlJPTEVfREVMRUdBVEVfUkVRVUVTVF9DT1JQX0NMSUVOVCIsIlJPTEVfQ0hJRUZfQ09SUF9DTElFTlQiLCJST0xFX0RJU1BBVENIRVJfQ09SUF9DTElFTlQiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIiwiUk9MRV9ESVNQQVRDSEVSX0NPTlRSQUNUT1IiLCJST0xFX0RSSVZFUl9DT05UUkFDVE9SIiwiUk9MRV9NQUlOVEVOQU5DRV9FTkdJTkVFUiIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiXSwicmFuZG9tIjoiNzZlNWFjNzMtMmQ0YS00Njk5LTk1YWMtZGRjNjllOThhNDE0IiwiZmFjdG9yIjoiQkFTSUMiLCJyZXF1ZXN0SWQiOiIyOGFjZjNjNTZhYjQxYzM1IiwiZXhwIjoxNzA5NzA0MTk1LCJpc3MiOiJTYmVyVHJhbnNwb3J0IiwiaWF0IjoxNzA5NzAzMjk1LCJzdWIiOiJCbGFuZGluQS1EIiwianRpIjoiMzBkNmJhYzMtYzFiYy00Y2UzLWE2YTQtZTE1ZjNkNDg5OTE0IiwidHJhbnNwb3J0IjpmYWxzZSwiZGF0YV9tYXN0ZXIiOnRydWV9.V7-JyQQDw_Ewog078hf4qPDUMypjYXO5cGsB4TtrEYWa0ZdJDZyJ3J9UwXPOypezAMEJ-8RhnNP1yMEwkUfUJ4jrhZpMtsCDB_pyM6V4XPas5hMNHYmC0vttiMcc2fNI5VOpMAvqjT1XolgQ5QoolgUEAlCydz_zAibVNbe0V_dT_E6Cr4oAXQINikOM3pdvhnrix8hM4MqTXgSJaVo-xyPcl0Zb0m1kZZ_A5v4xFoSuivD3vQN3GJbcOdgmFuSLVjL1qIbLyU8afmv31646CCtzcKElxBaC0cGrS2f7OsHPS8292cZffzBZk7Au1bkttmpGHg5czMcTBKrSfaA3orpD1vo7SDwYJQNwxEDPz_l8CL_gRN9-LOK-iey2EWnIW98es9RnAynHmTPHSUErDdpB7tEF8aGnO86uTXsIBpMmfhE_0vYusaM9XYHZBN1buF0bOiSoTzR0wKHHTvQ3EP-Wp0LRTBg7GXER_9R1gz2rQgoNEcMahC7x9rmmqL-nBeTGSFWPSqWjVwSNqJvUTT1KJYJJpwq1kKjvIw2mquWaQLleC8ulNABMdnBDsJVm-yUED_MTKiPrzThFu-Zt0vyxhL3rxcrKwUtmDmt-B3vub-GWZ70EDx8XmAiVVvTCr2Mu9-FTpp3rjbrwAJv-Q_0z_SoLEHdXK4zqEA6Id6Y")
                .header("typ", "JWT")
                .header("alg","RS512")
                .claim("iss","SberTransport")
                .claim("random","76e5ac73-2d4a-4699-95ac-ddc69e98a414")
                .claim("factor","BASIC")
                .claim("requestId","28acf3c56ab41c35")
                .claim("sub","BlandinA-D")
                .claim("jti","30d6bac3-c1bc-4ce3-a6a4-e15f3d489914")
                .claim("transport",false)
                .claim("data_master",true).build();
        writer.write("source", "action {param1}", "", new String[]{}, new JwtAuthenticationToken(jwt),
                Result.SUCCESS, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, "10.6.7.55");

        assertThat(infos).hasSize(2);
        assertThat(infos.get(1)).isEqualTo("AUDIT: source = source; user = BlandinA-D; id = 30d6bac3-c1bc-4ce3-a6a4-e15f3d489914; IPv4 = 10.6.7.55; action = action arg1; result = SUCCESS");
    }

    @Test
    @DisplayName("Проверка записи. Нет пользователя")
    void test_write_no_user() {
        var infos = new ArrayList<String>();
        var logger = TestLogger.builder().infoConsumer((text, ignore) -> infos.add(text)).build();

        var writer = new AuditWriterImpl(
                new ObjectProvider<>() {
                    @Override
                    public AuthenticatedResolver getObject(Object... args) throws BeansException {
                        return null;
                    }

                    @Override
                    public AuthenticatedResolver getIfAvailable() throws BeansException {
                        return null;
                    }

                    @Override
                    public AuthenticatedResolver getIfUnique() throws BeansException {
                        return null;
                    }

                    @Override
                    public AuthenticatedResolver getObject() throws BeansException {
                        return null;
                    }
                },
                new ObjectProvider<DbResolver>() {
                    @Override
                    public DbResolver getObject(Object... args) throws BeansException {
                        return null;
                    }

                    @Override
                    public DbResolver getIfAvailable() throws BeansException {
                        return null;
                    }

                    @Override
                    public DbResolver getIfUnique() throws BeansException {
                        return null;
                    }

                    @Override
                    public DbResolver getObject() throws BeansException {
                        return null;
                    }
                },
                mock(MapUtils.class)) {

            @Override
            Logger log() {
                return logger;
            }

        };

        writer.write("source", "action {success ? param1 : param2}", "", new String[]{}, null,
                Result.SUCCESS, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, null);

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0)).isEqualTo("AUDIT: source = source; user = anonymous; id = anonymous; IPv4 = null; action = action arg1; result = SUCCESS");
        verifyNoInteractions(dbResolver);

        infos.clear();

        writer.write("source", "action {success ? param1 : param2}", "", new String[]{}, null,
                Result.FAIL, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, "10.6.7.55");

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0)).isEqualTo("AUDIT: source = source; user = anonymous; id = anonymous; IPv4 = 10.6.7.55; action = action arg2; result = FAIL");
    }

    @Test
    @DisplayName("Проверка записи (шаблон сообщения)")
    void test_write_formatted() {
        var infos = new ArrayList<String>();
        var logger = TestLogger.builder().infoConsumer((text, ignore) -> infos.add(text)).build();

        when(authenticatedResolver.resolveUser(any())).thenAnswer(inv -> inv.getArgument(0, Authentication.class).getName());
        doNothing().when(dbResolver).save(anyString(), anyString(), any(LocalDateTime.class), anyString());

        var writer = new AuditWriterImpl(
                new ObjectProvider<>() {
                    @Override
                    public AuthenticatedResolver getObject(Object... args) throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfAvailable() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfUnique() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getObject() throws BeansException {
                        return authenticatedResolver;
                    }
                },
                new ObjectProvider<DbResolver>() {
                    @Override
                    public DbResolver getObject(Object... args) throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfAvailable() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfUnique() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getObject() throws BeansException {
                        return dbResolver;
                    }
                },
                mock(MapUtils.class)) {

            @Override
            Logger log() {
                return logger;
            }

        };

        writer.write("source", "", "action: %s", new String[]{"param1"}, new UsernamePasswordAuthenticationToken("user", "password"),
                Result.SUCCESS, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, null);

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0)).isEqualTo("AUDIT: source = source; user = user; id = user; IPv4 = null; action = action: arg1; result = SUCCESS");
        verify(dbResolver).save(eq("source"), eq("user"), any(LocalDateTime.class), eq("action: arg1"));
    }

    @Test
    @DisplayName("Проверка записи (не заполнены параметры)")
    void test_write_formatted_no_params() {
        var infos = new ArrayList<String>();
        var logger = TestLogger.builder().infoConsumer((text, ignore) -> infos.add(text)).build();

        when(authenticatedResolver.resolveUser(any())).thenAnswer(inv -> inv.getArgument(0, Authentication.class).getName());
        doNothing().when(dbResolver).save(anyString(), anyString(), any(LocalDateTime.class), anyString());

        var writer = new AuditWriterImpl(
                new ObjectProvider<>() {
                    @Override
                    public AuthenticatedResolver getObject(Object... args) throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfAvailable() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfUnique() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getObject() throws BeansException {
                        return authenticatedResolver;
                    }
                },
                new ObjectProvider<DbResolver>() {
                    @Override
                    public DbResolver getObject(Object... args) throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfAvailable() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfUnique() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getObject() throws BeansException {
                        return dbResolver;
                    }
                },
                mock(MapUtils.class)) {

            @Override
            Logger log() {
                return logger;
            }

        };

        writer.write("source", "", "action: %s", new String[]{}, new UsernamePasswordAuthenticationToken("user", "password"),
                Result.SUCCESS, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, null);

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0)).isEqualTo("AUDIT: source = source; user = user; id = user; IPv4 = null; action = ; result = SUCCESS");
        verify(dbResolver).save(eq("source"), eq("user"), any(LocalDateTime.class), eq(""));
    }

    @Test
    @DisplayName("Проверка записи (исключение при записи в БД)")
    void test_write_formatted_db_exception() {
        var infos = new ArrayList<String>();
        var errors = new ArrayList<String>();
        var logger = TestLogger.builder()
                .infoConsumer((text, ignore) -> infos.add(text))
                .errorConsumer((text, ignore) -> errors.add(text))
                .build();

        when(authenticatedResolver.resolveUser(any())).thenAnswer(inv -> inv.getArgument(0, Authentication.class).getName());
        doNothing().when(dbResolver).save(anyString(), anyString(), any(LocalDateTime.class), anyString());
        doThrow(new RuntimeException("test")).when(dbResolver).save(anyString(), anyString(), any(LocalDateTime.class), anyString());

        var writer = new AuditWriterImpl(
                new ObjectProvider<>() {
                    @Override
                    public AuthenticatedResolver getObject(Object... args) throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfAvailable() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getIfUnique() throws BeansException {
                        return authenticatedResolver;
                    }

                    @Override
                    public AuthenticatedResolver getObject() throws BeansException {
                        return authenticatedResolver;
                    }
                },
                new ObjectProvider<DbResolver>() {
                    @Override
                    public DbResolver getObject(Object... args) throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfAvailable() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getIfUnique() throws BeansException {
                        return dbResolver;
                    }

                    @Override
                    public DbResolver getObject() throws BeansException {
                        return dbResolver;
                    }
                },
                mock(MapUtils.class)) {

            @Override
            Logger log() {
                return logger;
            }

        };

        writer.write("source", "", "action: %s", new String[]{}, new UsernamePasswordAuthenticationToken("user", "password"),
                Result.SUCCESS, "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, null);

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0)).isEqualTo("AUDIT: source = source; user = user; id = user; IPv4 = null; action = ; result = SUCCESS");

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo("Ошибка при записи в БД");
    }

    @Test
    @DisplayName("Проверка записи. Нет БД резолвера")
    void test_write_no_dbresolver() {
        var writer = new AuditWriterImpl(
                new ObjectProvider<>() {
                    @Override
                    public AuthenticatedResolver getObject(Object... args) throws BeansException {
                        return null;
                    }

                    @Override
                    public AuthenticatedResolver getIfAvailable() throws BeansException {
                        return null;
                    }

                    @Override
                    public AuthenticatedResolver getIfUnique() throws BeansException {
                        return null;
                    }

                    @Override
                    public AuthenticatedResolver getObject() throws BeansException {
                        return null;
                    }
                },
                new ObjectProvider<DbResolver>() {
                    @Override
                    public DbResolver getObject(Object... args) throws BeansException {
                        return null;
                    }

                    @Override
                    public DbResolver getIfAvailable() throws BeansException {
                        return null;
                    }

                    @Override
                    public DbResolver getIfUnique() throws BeansException {
                        return null;
                    }

                    @Override
                    public DbResolver getObject() throws BeansException {
                        return null;
                    }
                },
                mock(MapUtils.class)) {

            @Override
            Logger log() {
                return TestLogger.builder().build();
            }

        };

        writer.write("source", "action {success ? param1 : param2}", "", new String[]{},
                new UsernamePasswordAuthenticationToken("user", "password"), Result.SUCCESS,
                "return", new String[]{"param1", "param2"}, new Object[] {"arg1", "arg2"}, null);
        verifyNoInteractions(dbResolver);
    }
}