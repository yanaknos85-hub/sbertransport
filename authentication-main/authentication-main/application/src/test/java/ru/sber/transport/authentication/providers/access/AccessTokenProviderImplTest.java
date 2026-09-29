package ru.sber.transport.authentication.providers.access;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.qameta.allure.Feature;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.providers.AccessTokenProvider;
import ru.sber.transport.authentication.messaging.senders.BlackListSender;
import ru.sber.transport.authentication.providers.access.config.JwtProperties;
import ru.sber.transport.authentication.providers.config.TokenProperties;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.authentication.providers.two_factor.config.TwoFactorProperties;

import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static ru.sber.transport.authentication.providers.access.AccessTokenProviderImpl.ROLES_CLAIM_NAME;
import static ru.sber.transport.authentication.providers.access.AccessTokenProviderImpl.TRANSPORT_CLAIM_NAME;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@DisplayName("Проверка провайдера токена доступа")
class AccessTokenProviderImplTest {

    private final BlackListSender blackListSender = mock(BlackListSender.class);

    private final RoleRepository roleRepository = mock(RoleRepository.class);

    private final Map<AuthType, TokenProperties> properties = Map.of(
        AuthType.BASIC, new JwtProperties(),
        AuthType.TWO_FA, new TwoFactorProperties()
    );

    private final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

    private final KeyPair jwtKeyPair = generator.generateKeyPair();

    private final Key key = jwtKeyPair.getPrivate();

    private final KeyPair twoFactorKeyPair = generator.generateKeyPair();

    private final Key twoFactorPrivateKey = twoFactorKeyPair.getPrivate();

    private final Tracer tracer = mock(Tracer.class);

    private final AccessTokenProvider provider = new AccessTokenProviderImpl(properties, blackListSender, roleRepository, tracer, key, twoFactorPrivateKey, KeyFactory.getInstance("RSA"));

    AccessTokenProviderImplTest() throws NoSuchAlgorithmException {
    }

    @DisplayName("Проверка формирования токена")
    @Test
    void test_generate() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        when(tracer.currentSpan()).thenReturn(new TestSpan());
        var properties = (JwtProperties) this.properties.get(AuthType.BASIC);

        properties.setIssuer("Issuer");
        properties.getExpire().setSeconds(30);
        properties.getExpire().setMinutes(1);
        properties.getExpire().setHours(2);
        properties.getExpire().setDays(3);
        properties.getExpire().setMonths(4);
        properties.getExpire().setYears(5);

        var map = new HashMap<String, Boolean>();
        map.put("Role1", false);
        map.put("Role2", false);
        map.put("Role3", true);

        var account = AccountDto.builder().id(UUID.randomUUID()).login("Login")
            .roles(map).transferPassword(true).build();
        when(roleRepository.findAllByAccountId(account.getId())).thenReturn(map.entrySet().stream().map(this::createRoleRecord).toList());

        var token = provider.generate(account, false);

        var tokenParts = token.value().split("\\.");

        assertThat(tokenParts).hasSize(3);

        var header = tokenParts[0];
        var payload = tokenParts[1];

        var mappedHeader = new ObjectMapper().readValue(Base64.getDecoder().decode(header),
            new TypeReference<Map<String, Object>>() {
            });

        assertThat(mappedHeader).containsEntry("alg", SignatureAlgorithm.RS512.name());

        var mappedPayload = new ObjectMapper().readValue(Base64.getDecoder().decode(payload),
            new TypeReference<Map<String, Object>>() {
            });

        try {
            NimbusJwtDecoder.withPublicKey((RSAPublicKey) twoFactorKeyPair.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS512)
                .build().decode(token.value());
            fail();
        } catch (Exception ignore) {
            // Нормальная ситуация. Обычный токен не должен расшифровываться 2-хфакторкой.
        }
        try {
            NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS512)
                .build().decode(token.value());
        } catch (Exception e) {
            fail(e);
        }

        assertThat(((List<String>) mappedPayload.get(ROLES_CLAIM_NAME)))
            .contains("Role1")
            .contains("Role2")
            .contains("Role3");
        assertThat(mappedPayload)
            .containsEntry(TRANSPORT_CLAIM_NAME, true)
            .containsEntry("sub", account.getLogin())
            .containsEntry("iss", properties.getIssuer())
            .containsKey("iat")
            .containsKey("exp");
    }

    @DisplayName("Проверка формирования токена второго фактора - принудительное назначение")
    @Test
    void test_generate_2nd_factor_enforced() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        when(tracer.currentSpan()).thenReturn(new TestSpan());
        var properties = (JwtProperties) this.properties.get(AuthType.BASIC);

        properties.setIssuer("Issuer");
        properties.getExpire().setSeconds(30);
        properties.getExpire().setMinutes(1);
        properties.getExpire().setHours(2);
        properties.getExpire().setDays(3);
        properties.getExpire().setMonths(4);
        properties.getExpire().setYears(5);

        var map = new HashMap<String, Boolean>();
        map.put("Role1", false);
        map.put("Role2", false);
        map.put("Role3", true);

        var account = AccountDto.builder().id(UUID.randomUUID()).login("Login")
            .roles(map).transferPassword(true).build();
        when(roleRepository.findAllByAccountId(account.getId())).thenReturn(map.entrySet().stream().map(this::createRoleRecord).toList());

        var token = provider.generate(account, true);

        var expicationExpected = LocalDateTime.now(ZoneOffset.UTC)
            .plusSeconds(30)
            .plusMinutes(1)
            .plusHours(2)
            .plusDays(3)
            .plusMonths(4)
            .plusYears(5);

        var tokenParts = token.value().split("\\.");

        assertThat(tokenParts).hasSize(3);

        var header = tokenParts[0];
        var payload = tokenParts[1];

        var mappedHeader = new ObjectMapper().readValue(Base64.getDecoder().decode(header),
            new TypeReference<Map<String, Object>>() {
            });

        assertThat(mappedHeader).containsEntry("alg", SignatureAlgorithm.RS512.name());

        var mappedPayload = new ObjectMapper().readValue(Base64.getDecoder().decode(payload),
            new TypeReference<Map<String, Object>>() {
            });

        try {
            NimbusJwtDecoder.withPublicKey((RSAPublicKey) twoFactorKeyPair.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS512)
                .build().decode(token.value());
            fail();
        } catch (Exception ignore) {
            // Нормальная ситуация. Обычный токен не должен расшифровываться 2-хфакторкой.
        }
        try {
            NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS512)
                .build().decode(token.value());
        } catch (Exception e) {
            fail(e);
        }

        assertThat(((List<String>) mappedPayload.get(ROLES_CLAIM_NAME)))
            .contains("Role1")
            .contains("Role2")
            .contains("Role3");
        assertThat(mappedPayload)
            .containsEntry(TRANSPORT_CLAIM_NAME, true)
            .containsEntry("sub", account.getLogin())
            .containsEntry("iss", properties.getIssuer())
            .containsKey("iat")
            .containsKey("exp");
    }

    @DisplayName("Проверка формирования токена второго фактора")
    @Test
    void test_generate_2nd_factor() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        when(tracer.currentSpan()).thenReturn(new TestSpan());
        var properties = (TwoFactorProperties) this.properties.get(AuthType.TWO_FA);

        properties.setIssuer("Issuer");
        properties.getExpire().setSeconds(30);
        properties.getExpire().setMinutes(1);
        properties.getExpire().setHours(2);
        properties.getExpire().setDays(3);
        properties.getExpire().setMonths(4);
        properties.getExpire().setYears(5);

        var map = new HashMap<String, Boolean>();
        map.put("Role1", false);
        map.put("Role2", false);
        map.put("Role3", true);

        var account = AccountDto.builder().id(UUID.randomUUID()).login("Login")
            .roles(map).transferPassword(true)
            .authType(AuthType.TWO_FA).build();
        when(roleRepository.findAllByAccountId(account.getId())).thenReturn(map.entrySet().stream().map(this::createRoleRecord).toList());

        var token = provider.generate(account, false);

        var tokenParts = token.value().split("\\.");

        assertThat(tokenParts).hasSize(3);

        var header = tokenParts[0];
        var payload = tokenParts[1];

        var mappedHeader = new ObjectMapper().readValue(Base64.getDecoder().decode(header),
            new TypeReference<Map<String, Object>>() {
            });

        assertThat(mappedHeader).containsEntry("alg", SignatureAlgorithm.RS512.name());

        var mappedPayload = new ObjectMapper().readValue(Base64.getDecoder().decode(payload),
            new TypeReference<Map<String, Object>>() {
            });

        NimbusJwtDecoder.withPublicKey((RSAPublicKey) twoFactorKeyPair.getPublic())
            .signatureAlgorithm(SignatureAlgorithm.RS512)
            .build().decode(token.value());
        try {
            NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic())
                .build().decode(token.value());
            fail();
        } catch (Exception e) {
            // Нормальная ситуация. Токен второго фактора не должен расшифровываться обычным.
        }

        assertThat(((List<String>) mappedPayload.get(ROLES_CLAIM_NAME)))
            .contains("Role1")
            .contains("Role2")
            .contains("Role3");
        assertThat(mappedPayload)
            .containsEntry("sub", account.getLogin())
            .containsEntry("iss", properties.getIssuer())
            .containsKey("iat")
            .containsKey("exp");
    }

    @Test
    @DisplayName("Отправка токена в ЧС")
    void test_toBlackList() {
        provider.toBlackList("token");

        var tokenCaptor = ArgumentCaptor.forClass(String.class);

        verify(blackListSender).send(tokenCaptor.capture());

        assertThat(tokenCaptor.getValue()).isEqualTo("token");
    }

    private RoleRecord createRoleRecord(Map.Entry<String, Boolean> entry) {
        var role = new RoleRecord();
        role.setCode(entry.getKey());
        role.setName(entry.getKey());
        role.setDataMaster(entry.getValue());
        role.setDefaultFor(JSON.valueOf(entry.getValue() ? "[]" : "[\"EMPLOYEE\"]"));
        return role;
    }

    private final static class TestSpan implements Span {
        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public @NonNull TraceContext context() {
            return new TraceContext() {
                @Override
                public @NonNull String traceId() {
                    return "traceId";
                }

                @Override
                public String parentId() {
                    return "parentId";
                }

                @Override
                public @NonNull String spanId() {
                    return "spanId";
                }

                @Override
                public @NonNull Boolean sampled() {
                    return false;
                }
            };
        }

        @Override
        public Span start() {
            return null;
        }

        @Override
        public Span name(String name) {
            return null;
        }

        @Override
        public Span event(String value) {
            return null;
        }

        @Override
        public Span event(String s, long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public Span tag(String key, String value) {
            return null;
        }

        @Override
        public Span error(Throwable throwable) {
            return null;
        }

        @Override
        public void end() {
        }

        @Override
        public void end(long l, TimeUnit timeUnit) {

        }

        @Override
        public void abandon() {
        }

        @Override
        public Span remoteServiceName(String s) {
            return null;
        }

        @Override
        public Span remoteIpAndPort(String s, int i) {
            return null;
        }
    }
}