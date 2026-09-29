package ru.sber.transport.etrn.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextHelperTest {

    private static Map<String, Object> jwtClaims;

    @Mock
    private Authentication plainAuth;

    @BeforeAll
    static void generateSignedJwt() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        // Простой JWT без подписи — только для тестирования извлечения данных
        Map<String, Object> headers = Map.of(
                "alg", "RS256",
                "typ", "JWT"
        );
        Map<String, Object> claims = Map.of(
                "sub", userId.toString(),
                "jti", userId.toString(),
                "scp", Set.of("ROLE_ETRN_SIGNER"),
                "iss", "test-issuer",
                "exp", Date.from(now.plusSeconds(3600)).getTime() / 1000,
                "iat", Date.from(now).getTime() / 1000
        );

        String header = Base64.getEncoder().withoutPadding()
                .encodeToString("""
                        {"alg":"RS256","typ":"JWT"}
                        """.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getEncoder().withoutPadding()
                .encodeToString("""
                        {"sub":"%s","jti":"%s","scp":["ROLE_ETRN_SIGNER"],"iss":"test-issuer"}
                        """.formatted(userId, userId).getBytes(StandardCharsets.UTF_8));

        // Не подписанный JWT для тестов — достаточен для проверки извлечения данных
        String tokenValue = header + "." + payload + ".";

        Jwt jwt = new Jwt(
                tokenValue,
                now.minusSeconds(10),
                now.plusSeconds(3590),
                headers,
                claims
        );

        jwtClaims = claims;
    }

    @Test
    @DisplayName("getUserId — извлечение userId из JWT по jti")
    void getUserId_fromJwt_extractJti_success() {
        var token = new JwtAuthenticationToken(
                new Jwt(
                        "header.payload.sig",
                        Instant.now().minusSeconds(10),
                        Instant.now().plusSeconds(3590),
                        Map.of("alg", "RS256"),
                        Map.of("jti", UUID.randomUUID().toString(), "sub", "test-sub")
                ),
                Set.of(() -> "ROLE_ETRN_SIGNER")
        );

        UUID userId = ContextHelper.getUserId(token);

        assertThat(userId).isNotNull();
    }

    @Test
    @DisplayName("getUserId — извлечение userId из Authentication по name")
    void getUserId_fromPlainAuth_extractName_success() {
        UUID expectedId = UUID.randomUUID();
        when(plainAuth.getName()).thenReturn(expectedId.toString());

        UUID userId = ContextHelper.getUserId(plainAuth);

        assertThat(userId).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("getUserId — null при пустом name в Authentication")
    void getUserId_fromPlainAuth_emptyName_returnsNull() {
        when(plainAuth.getName()).thenReturn(null);

        UUID userId = ContextHelper.getUserId(plainAuth);

        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("getUserId — null при null authentication")
    void getUserId_nullAuthentication_returnsNull() {
        UUID userId = ContextHelper.getUserId(null);
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("getRoles — извлечение ролей из Authentication")
    void getRoles_extractAuthorities_success() {
        UUID userId = UUID.randomUUID();
        var token = new JwtAuthenticationToken(
                new Jwt(
                        "header.payload.sig",
                        Instant.now().minusSeconds(10),
                        Instant.now().plusSeconds(3590),
                        Map.of("alg", "RS256"),
                        Map.of("jti", userId.toString())
                ),
                Set.of(
                        () -> "ROLE_ETRN_SIGNER",
                        () -> "ROLE_ADMIN"
                )
        );

        Set<String> roles = ContextHelper.getRoles(token);

        assertThat(roles).containsExactlyInAnyOrder("ROLE_ETRN_SIGNER", "ROLE_ADMIN");
    }
}
