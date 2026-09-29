package ru.sber.transport.authentication.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.dto.TwoFactor;
import ru.sber.transport.authentication.business.providers.TwoFactorProvider;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.refresh.dao.SessionRepository;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.authentication.web.exceptions.LoginException;
import ru.sber.transport.authentication.web.model.Token;
import ru.sber.transport.authorization.service.BlackListService;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.database.authentication.tables.records.SessionRecord;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.messages.BlackListMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.sber.transport.authentication.business.dto.AuthType.TWO_FA;
import static ru.sber.transport.authentication.business.providers.AccessTokenProvider.*;

@SuppressWarnings("unchecked")
@SpringBootTest
@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка аутентификации")
@MockitoBean(types = BlackListService.class)
@AutoConfigureMockMvc
class AuthenticationControllerTest extends AbstractContextedTest {

    private static final String USER_ID = "95a9ddc6-e62d-4061-9e65-47982ec2cf4c";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @MockitoBean
    private TwoFactorProvider twoFactorProvider;

    @Autowired
    @Qualifier("twoFactorJwtPrivate")
    private Key twoFactorKeyPrivate;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean(name = "blackListOutput")
    private OutputBridge outputBridge;

    public static Stream<Arguments> wrongData() {
        return Stream.of(
            Arguments.of("Basic login:password:password2"),
            Arguments.of("Basic login:password password2"),
            Arguments.of("Basic login"),
            Arguments.of("NoBasic login:password"),
            Arguments.of("Basic login:password2"),
            Arguments.of("Basic bG9naW46cGFzc3dvcmQy"),
            Arguments.of("Basic fadsfdasfasdfsaf")
        );
    }

    @Test
    @DisplayName("Проверка входа с текстовыми данными")
    void login() throws Exception {
        UUID userId = UUID.randomUUID();
        var account = new AccountRecord();
        account.setId(userId);
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        for (var index = 0; index < 5; index++) {
            var role = new RoleRecord();
            role.setCode("ROLE_" + index);
            role.setName("Name " + index);
            roleRepository.save(role);
            roleRepository.add(account.getId(), role);
        }

        var response = mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);

        var session = sessionRepository.findAll().getFirst();

        assertThat(actual.getRefreshToken()).isEqualTo(session.getId().toString());
        assertThat(actual.getAccessToken()).isEqualTo(session.getToken());

        var tokenParts = actual.getAccessToken().split("\\.");
        var encodedPayload = tokenParts[1];
        var payload = objectMapper.readValue(new String(Base64.getMimeDecoder().decode(encodedPayload)),
            new TypeReference<Map<String, Object>>() {
            });
        var roles = (ArrayList<String>) payload.get("roles");
        var expectedRoles = roleRepository.findAllByAccountId(account.getId());
        assertThat(roles).hasSize(expectedRoles.size());
        for (var role : roles) {
            assertThat(expectedRoles.stream().anyMatch(e -> e.getCode().equals(role))).isTrue();
        }
    }

    @Test
    @DisplayName("Проверка входа с текстовыми данными (метод GET)")
    void getLogin() throws Exception {
        UUID userId = UUID.randomUUID();
        var account = new AccountRecord();
        account.setId(userId);
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        for (var index = 0; index < 5; index++) {
            var role = new RoleRecord();
            role.setCode("ROLE_" + index);
            role.setName("Name " + index);
            roleRepository.save(role);
            roleRepository.add(account.getId(), role);
        }

        var response = mockMvc.perform(get("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);

        var session = sessionRepository.findAll().getFirst();

        assertThat(actual.getRefreshToken()).isEqualTo(session.getId().toString());
        assertThat(actual.getAccessToken()).isEqualTo(session.getToken());

        var tokenParts = actual.getAccessToken().split("\\.");
        var encodedPayload = tokenParts[1];
        var payload = objectMapper.readValue(new String(Base64.getMimeDecoder().decode(encodedPayload)),
                new TypeReference<Map<String, Object>>() {
                });
        var roles = (ArrayList<String>) payload.get("roles");
        var expectedRoles = roleRepository.findAllByAccountId(account.getId());
        assertThat(roles).hasSize(expectedRoles.size());
        for (var role : roles) {
            assertThat(expectedRoles.stream().anyMatch(e -> e.getCode().equals(role))).isTrue();
        }
    }

    @Test
    @DisplayName("Проверка входа со вторым фактором")
    void test_code() throws Exception {
        UUID userId = UUID.randomUUID();
        var account = new AccountRecord();
        account.setId(userId);
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setAuthType(TWO_FA.name());
        account.setEmail("some@email.com");

        var twoFactor = Instancio.create(TwoFactor.class);

        var code = "1234";
        when(twoFactorProvider.checkCode(account.getId(), code)).thenReturn(true);
        when(twoFactorProvider.generate(any(), any())).thenReturn(twoFactor);

        accountRepository.save(account);

        var issuedAt = OffsetDateTime.now();
        var expireAt = issuedAt.plusDays(1);
        var jwsHeaders = JwsHeader
            .with(SignatureAlgorithm.RS512)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .type("JWT")
            .keyId("second")
            .build();
        var jwsClaims = JwtClaimsSet.builder()
            .claim(ROLES_CLAIM_NAME, Set.of("ROLE_TEST"))
            .claim(RANDOM_CLAIM_NAME, UUID.randomUUID())
            .claim(FACTOR_CLAIM_NAME, TWO_FA.name())
            .expiresAt(expireAt.toInstant())
            .issuer("issuer")
            .issuedAt(issuedAt.toInstant())
            .notBefore(issuedAt.toInstant())
            .subject("subject")
            .id(account.getId().toString());

        var parameters = JwtEncoderParameters.from(jwsHeaders, jwsClaims.build());

        var jwk = createJwk((PrivateKey) twoFactorKeyPrivate);
        var jwksList = new JWKSet(jwk);
        var jwks = new ImmutableJWKSet<>(jwksList);
        var encoder = new NimbusJwtEncoder(jwks);
        var jwt = encoder.encode(parameters).getTokenValue();

        var response = mockMvc.perform(post("/code")
                .header("Authorization", "Bearer %s".formatted(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "code": "%s"
                    }
                    """.formatted(code)))
            .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);

        var session = sessionRepository.findAll().getFirst();

        assertThat(actual.getRefreshToken()).isEqualTo(session.getId().toString());
        assertThat(actual.getAccessToken()).isEqualTo(session.getToken()).isNotEqualTo(jwt);

        var tokenParts = actual.getAccessToken().split("\\.");
        var encodedPayload = tokenParts[1];
        var payload = objectMapper.readValue(new String(Base64.getMimeDecoder().decode(encodedPayload)),
            new TypeReference<Map<String, Object>>() {
            });
        var roles = (ArrayList<String>) payload.get("roles");
        var expectedRoles = roleRepository.findAllByAccountId(account.getId());
        assertThat(roles).hasSize(expectedRoles.size());
        for (var role : roles) {
            assertThat(expectedRoles.stream().anyMatch(e -> e.getCode().equals(role))).isTrue();
        }
    }

    @Test
    @DisplayName("Проверка входа со вторым фактором. Неверный код")
    void test_code_wrong() throws Exception {
        UUID userId = UUID.randomUUID();
        var account = new AccountRecord();
        account.setId(userId);
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setAuthType(TWO_FA.name());
        account.setEmail("some@email.com");

        var twoFactor = Instancio.create(TwoFactor.class);

        var code = "1234";
        when(twoFactorProvider.checkCode(account.getId(), code)).thenReturn(false);
        when(twoFactorProvider.generate(any(), any())).thenReturn(twoFactor);

        accountRepository.save(account);

        var issuedAt = OffsetDateTime.now();
        var expireAt = issuedAt.plusDays(1);
        var jwsHeaders = JwsHeader
            .with(SignatureAlgorithm.RS512)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .type("JWT")
            .keyId("second")
            .build();
        var jwsClaims = JwtClaimsSet.builder()
            .claim(ROLES_CLAIM_NAME, Set.of("ROLE_TEST"))
            .claim(RANDOM_CLAIM_NAME, UUID.randomUUID())
            .claim(FACTOR_CLAIM_NAME, TWO_FA.name())
            .expiresAt(expireAt.toInstant())
            .issuer("issuer")
            .issuedAt(issuedAt.toInstant())
            .notBefore(issuedAt.toInstant())
            .subject("subject")
            .id(account.getId().toString());

        var parameters = JwtEncoderParameters.from(jwsHeaders, jwsClaims.build());

        var jwk = createJwk((PrivateKey) twoFactorKeyPrivate);
        var jwksList = new JWKSet(jwk);
        var jwks = new ImmutableJWKSet<>(jwksList);
        var encoder = new NimbusJwtEncoder(jwks);
        var jwt = encoder.encode(parameters).getTokenValue();

        var response = mockMvc.perform(post("/code")
                .header("Authorization", "Bearer %s".formatted(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "code": "%s"
                    }
                    """.formatted(code)))
            .andExpect(status().isUnauthorized());

        assertThat(response).isNotNull();

        var result = response.andReturn();
        assertTrue(result.getResponse().getContentAsString()
            .contains(LoginException.Type.WRONG_TWO_FA.name()));
    }

    @Test
    @DisplayName("Проверка двойного входа с текстовыми данными")
    void login_double() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        for (var index = 0; index < 5; index++) {
            var role = new RoleRecord();
            role.setCode("ROLE_" + index);
            role.setName("Name " + index);
            roleRepository.save(role);
            roleRepository.add(account.getId(), role);
        }

        mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk());

        mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("wrongData")
    @DisplayName("Проверка входа с текстовыми данными. Некорректные данные входа.")
    void login_wrongData(String wrongData) throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        mockMvc.perform(post("/login").header("Authorization", wrongData).header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Проверка входа с текстовыми данными. Большое количсетво попыток авторизации.")
    void login_tooManyTimes() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(0);
        account.setActive(true);

        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        for (int i = 0; i < 4; i++) {
            var result = mockMvc.perform(post("/login").header("Authorization", "Basic login:WRONGpassword").header("User-Agent", "WEB_CORP").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andReturn();

            assertTrue(result.getResponse().getContentAsString()
                .contains(LoginException.Type.WRONG_CREDENTIALS.name()));
        }

        var result = mockMvc.perform(post("/login").header("Authorization", "Basic login:WRONGpassword").header("User-Agent", "WEB_CORP"))
            .andExpect(status().isUnauthorized())
            .andReturn();

        assertTrue(result.getResponse().getContentAsString()
            .contains(LoginException.Type.TOO_MANY_LOGIN_TRIES.name()));
    }

    @Test
    @DisplayName("Проверка входа с текстовыми данными. Обнуление счетчика после успешной авторизации.")
    void login_clearLoginTriesAfterSuccess() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        for (int i = 0; i < 4; i++) {
            var result = mockMvc.perform(post("/login").header("Authorization", "Basic login:WRONGpassword").header("User-Agent", "WEB_CORP").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andReturn();

            assertTrue(result.getResponse().getContentAsString()
                .contains(LoginException.Type.WRONG_CREDENTIALS.name()));
        }

        mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk());

        var number = accountRepository.findById(account.getId()).orElseThrow().getNumberOfLoginAttempts();
        assertEquals(0, number);
    }

    @Test
    @DisplayName("Проверка входа с текстовыми данными. Пользователя не существует")
    void login_noUser() throws Exception {
        var result = mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isUnauthorized())
            .andReturn();

        assertTrue(result.getResponse().getContentAsString()
            .contains(LoginException.Type.WRONG_CREDENTIALS.name()));
    }

    @Test
    @DisplayName("Проверка входа с закодированными данными")
    void testLogin() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        var response = mockMvc.perform(post("/login").header("Authorization", "Basic bG9naW46cGFzc3dvcmQ=").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk()).andReturn();

        var tokenDto = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);

        var session = sessionRepository.findAll().getFirst();

        assertThat(tokenDto.getRefreshToken()).isEqualTo(session.getId().toString());
        assertThat(tokenDto.getAccessToken()).isEqualTo(session.getToken());
        assertThat(tokenDto.isTransferPassword()).isFalse();
    }

    @Test
    @DisplayName("Проверка входа с закодированными данными. Нет пользователя")
    void testLogin_noUser() throws Exception {
        var result = mockMvc.perform(post("/login").header("Authorization", "Basic bG9naW46cGFzc3dvcmQ=").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isUnauthorized())
            .andReturn();

        assertTrue(result.getResponse().getContentAsString()
            .contains(LoginException.Type.WRONG_CREDENTIALS.name()));
    }

    @Test
    @DisplayName("Проверка входа с токеном обновления")
    void testLogin_refresh() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        account = accountRepository.save(account);

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setExpireAt(LocalDateTime.now().plusDays(1));
        session.setToken("old token");

        session = sessionRepository.save(session);

        var response =
            mockMvc.perform(post("/login").header("Authorization", "Token " + session.getId().toString()).header("User-Agent", "WEB_CORP").with(csrf()))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);

        assertThat(sessionRepository.count()).isEqualTo(1);
        assertThat(sessionRepository.findAll().getFirst()).isNotEqualTo(session);
        assertThat(actual.getRefreshToken()).isNotEqualTo(session.getId().toString());
        assertThat(actual.getAccessToken()).isNotEqualTo(session.getToken());

        mockMvc.perform(post("/login").header("Authorization", "Token " + session.getId().toString()).header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk());

        var messageCaptor = ArgumentCaptor.forClass(BlackListMessage.class);
        verify(outputBridge).send(messageCaptor.capture());
        var message = messageCaptor.getValue();

        assertThat(message.getToken()).isEqualTo("old token");
    }

    @Test
    @DisplayName("Проверка входа с истекшим токеном обновления")
    void testLogin_refresh_expired() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        account = accountRepository.save(account);

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setExpireAt(LocalDateTime.now().minusMonths(1));
        session.setToken("old token");

        session = sessionRepository.save(session);

        var result = mockMvc.perform(post("/login").header("Authorization", "Token " + session.getId().toString()).header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isUnauthorized())
            .andReturn();

        assertTrue(result.getResponse().getContentAsString()
            .contains(LoginException.Type.REFRESH_EXPIRED.name()));
    }

    @Test
    @DisplayName("Проверка входа с неверным токеном обновления")
    void testLogin_refresh_wrongToken() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setAuthType(AuthType.BASIC.name());

        account = accountRepository.save(account);

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setExpireAt(LocalDateTime.now().plusDays(1));
        session.setToken("old token");

        sessionRepository.save(session);

        var token = UUID.randomUUID().toString();
        var result = mockMvc.perform(post("/login").header("Authorization", "Token " + token).header("User-Agent", "WEB_CORP"))
            .andExpect(status().isUnauthorized())
            .andReturn();

        assertTrue(result.getResponse().getContentAsString()
            .contains(LoginException.Type.REFRESH_EXPIRED.name()));
    }

    @Test
    @DisplayName("Выход")
    @WithMockUser(value = USER_ID, roles = "GUEST")
    void logout() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.fromString(USER_ID));
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        assertThat(sessionRepository.count()).isZero();

        var response = mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk()).andReturn();

        assertThat(sessionRepository.count()).isEqualTo(1);

        var jwt = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class).getAccessToken();

        mockMvc.perform(get("/logout").header("Authorization", "Bearer " + jwt))
            .andExpect(status().isOk());

        assertThat(sessionRepository.count()).isZero();

        var messageCaptor = ArgumentCaptor.forClass(BlackListMessage.class);
        verify(outputBridge).send(messageCaptor.capture());
        var message = messageCaptor.getValue();

        assertThat(message.getToken()).isEqualTo(jwt);
    }

    @Test
    @DisplayName("Выход. Неверный токен")
    @WithMockUser(value = USER_ID, roles = "GUEST")
    void logout_wrong_token() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.fromString(USER_ID));
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        assertThat(sessionRepository.count()).isZero();

        mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk()).andReturn();

        assertThat(sessionRepository.count()).isEqualTo(1);

        var jwt = "%s.%s.%s".formatted(Base64.getEncoder().encodeToString("header".getBytes(StandardCharsets.UTF_8)), Base64.getEncoder().encodeToString("wrong payload".getBytes(StandardCharsets.UTF_8)), "signature");

        mockMvc.perform(get("/logout").header("Authorization", "Bearer " + jwt))
            .andExpect(status().isUnauthorized());

        verify(outputBridge, never()).send(any());
    }

    @Test
    @DisplayName("Выход. Не входил")
    void logout_noEntered() throws Exception {
        mockMvc.perform(get("/logout")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка входа с транспортным паролем")
    void testLogin_transferPassword() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setTransferPassword(true);

        accountRepository.save(account);

        var response = mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk()).andReturn();

        var tokenDto = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);
        var accessToken = tokenDto.getAccessToken();

        var session = sessionRepository.findAll().getFirst();
        assertThat(tokenDto.getRefreshToken()).isEqualTo(session.getId().toString());
        assertThat(tokenDto.getAccessToken()).isEqualTo(session.getToken());
        assertThat(tokenDto.isTransferPassword()).isTrue();

        assertChangePassword("Basic login:login", accessToken);
        assertChangePassword("Basic login:jr3X", accessToken);
        assertChangePassword("Basic login:41673465", accessToken);
        assertChangePassword("Basic login:uenfscfg", accessToken);
        assertChangePassword("Basic login:aaaa1111", accessToken);
        assertChangePassword("Basic login:aqwe3719", accessToken);
        assertChangePassword("Basic login:a456df52", accessToken);

        mockMvc.perform(post("/changePassword")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .header("x-changePassword", "Basic login:a4d2kf52"))
            .andExpect(status().isOk()).andReturn();

        mockMvc.perform(post("/changePassword")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .header("x-changePassword", "Basic bG9naW46YTRkMmtmNTI="))
            .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    @DisplayName("Проверка входа с транспортным паролем. Смена невозможна")
    void testLogin_transferPassword_noChange() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("login");
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setNumberOfLoginAttempts(1);
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setTransferPassword(false);

        accountRepository.save(account);

        var response = mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
                .andExpect(status().isOk()).andReturn();

        var tokenDto = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);
        var accessToken = tokenDto.getAccessToken();

        mockMvc.perform(post("/changePassword").with(csrf())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("x-changePassword", "Basic login:newPassword"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Password check failed"))
            .andExpect(jsonPath("$.problems.length()").value(1))
            .andExpect(jsonPath("$.problems.[0].field").value("password"))
            .andExpect(jsonPath("$.problems.[0].value").value("[protected]"))
            .andExpect(jsonPath("$.problems.[0].constraints.length()").value(1))
            .andExpect(jsonPath("$.problems.[0].constraints.[0].type").value("STATE_NON_TRANSFER"))
        ;
    }

    @Test
    @DisplayName("Проверка смены пароля. Нет УЗ")
    void testLogin_transferPassword_noAccountRecord() throws Exception {
        mockMvc.perform(post("/changePassword").header("x-changePassword", "Basic login:a4d2kf52").with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json("{}"));
    }

    private void assertChangePassword(String newCredentials, String accessToken) throws Exception {
        var response = mockMvc.perform(post("/changePassword")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .header("Authorization", newCredentials))
            .andExpect(status().isBadRequest()).andReturn();
        assertThat(response.getResolvedException()).isNotNull();
        assertThat(response.getResolvedException().getMessage().length()).isGreaterThan(10);
    }

    @Test
    @DisplayName("Проверка сброса пароля")
    void testLogin_resetPassword() throws Exception {
        UUID userId = UUID.randomUUID();
        var account = new AccountRecord();
        account.setId(userId);
        account.setLogin("login");
        account.setNumberOfLoginAttempts(3);
        account.setTransferPassword(false);
        account.setHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        var account1 = accountRepository.findById(userId).orElse(null);
        assertThat(account1).isNotNull();
        assertThat(account1.getNumberOfLoginAttempts()).isEqualTo(3);
        assertThat(account1.getTransferPassword()).isFalse();

        var response = mockMvc.perform(post("/login").header("Authorization", "Basic login:password").header("User-Agent", "WEB_CORP").with(csrf()))
            .andExpect(status().isOk()).andReturn();

        var jwt = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class).getAccessToken();

        mockMvc.perform(put("/resetPassword/" + userId)
                .header("Authorization", "Bearer " + jwt)
            )
            .andExpect(status().isOk()).andReturn();

        var account2 = accountRepository.findById(userId).orElse(null);
        assertThat(account2).isNotNull();
        assertThat(account2.getNumberOfLoginAttempts()).isEqualTo(0);
        assertThat(account2.getTransferPassword()).isTrue();

    }

    @Test
    @DisplayName("Проверка сброса пароля. Нет УЗ")
    @WithMockUser(value = USER_ID, roles = "GUEST")
    void testLogin_resetPassword_noAccountRecord() throws Exception {
        mockMvc.perform(put("/resetPassword/" + USER_ID).with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    private JWK createJwk(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        var publicKey = createPublic(privateKey);
        var use = KeyUse.SIGNATURE;
        var ops = Set.of(KeyOperation.SIGN);
        var alg = Algorithm.parse(SignatureAlgorithm.RS512.name());
        return new RSAKey(
            publicKey,
            privateKey,
            use,
            ops,
            alg,
            "second",
            null, null, null, null, null, null, null, null);
    }

    private RSAPublicKey createPublic(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
    }
}