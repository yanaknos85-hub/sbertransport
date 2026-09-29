package ru.sber.transport.authentication.providers.refresh;

import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractDatabaseTest;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.providers.RefreshProvider;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.refresh.config.RtProperties;
import ru.sber.transport.authentication.providers.refresh.dao.SessionRepository;
import ru.sber.transport.authentication.providers.refresh.mapper.SessionMapper;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.SessionRecord;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@DisplayName("Проверка провайдере токенов обновления")
class RefreshProviderImplTest extends AbstractDatabaseTest {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    private final RtProperties properties = new RtProperties();
    
    private final SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);
    
    private final Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

    private RefreshProvider refreshProvider;
    
    @BeforeEach
    void setup() {
        properties.getExpire().setMinutes(2);
        properties.getExpire().setHours(3);
        properties.getExpire().setDays(4);
        properties.getExpire().setMonths(5);
        properties.getExpire().setYears(6);
        
        refreshProvider = new RefreshProviderImpl(sessionRepository, accountRepository, properties, sessionMapper, clock);
    }
    
    @DisplayName("Очистка старых сессий")
    @Test
    void test_cleanupOldSessions() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setActive(true);
        account.setLogin("Login");
        account.setHash("Hash");
        account.setEmail("Email");
        
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
        
        var now = LocalDateTime.now();
        
        var count = 100;
        for (var i = 0; i < count; i++) {
            var session = new SessionRecord();
            session.setId(UUID.randomUUID());
            session.setAccountId(account.getId());
            session.setToken("blablabla"  + i);
            session.setExpireAt(now.minusDays(count / 2).plusDays(i));
            
            sessionRepository.save(session);
        }
        
        Assertions.assertThat(sessionRepository.count()).isEqualTo(count);
        
        ((RefreshProviderImpl) refreshProvider).cleanupOldSessions();
        
        Assertions.assertThat(sessionRepository.count()).isEqualTo(count / 2);
        
        for (var session : sessionRepository.findAll()) {
            assertThat(session.getExpireAt().truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
        }
    }
    
    @Test
    @DisplayName("Генерация")
    void test_generate() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        account.setActive(true);

        account = accountRepository.save(account);
        
        var bAccount = AccountDto.builder()
                .login("Login").build();
        
        assertThat(sessionRepository.count()).isZero();
        
        var token = refreshProvider.generate("JWT", bAccount);
        var expectedDate = LocalDateTime.now(ZoneOffset.UTC)
                               .plusSeconds(properties.getExpire().getSeconds())
                               .plusMinutes(properties.getExpire().getMinutes())
                               .plusHours(properties.getExpire().getHours())
                               .plusDays(properties.getExpire().getDays())
                               .plusMonths(properties.getExpire().getMonths())
                               .plusYears(properties.getExpire().getYears());
        
        assertThat(sessionRepository.count()).isEqualTo(1);
        assertThat(sessionRepository.findAll().getFirst().getId()).hasToString(token.value());
        assertThat(sessionRepository.findAll().getFirst().getToken()).isEqualTo("JWT");
        assertThat(sessionRepository.findAll().getFirst().getAccountId()).isEqualTo(account.getId());
        assertThat(sessionRepository.findAll().getFirst().getExpireAt().getYear()).isEqualTo(expectedDate.getYear());
        assertThat(sessionRepository.findAll().getFirst().getExpireAt().getMonthValue()).isEqualTo(expectedDate.getMonthValue());
        assertThat(sessionRepository.findAll().getFirst().getExpireAt().getDayOfMonth()).isEqualTo(expectedDate.getDayOfMonth());
        assertThat(sessionRepository.findAll().getFirst().getExpireAt().getHour()).isEqualTo(expectedDate.getHour());
        assertThat(sessionRepository.findAll().getFirst().getExpireAt().getMinute()).isEqualTo(expectedDate.getMinute());
    }
    
    @Test
    @DisplayName("Отмена сессии")
    void test_release() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
        
        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setToken("Token");
        session.setExpireAt(LocalDateTime.now(ZoneOffset.UTC));
        sessionRepository.save(session);
        
        assertThat(sessionRepository.count()).isEqualTo(1);
    
        var bAccount = AccountDto.builder().login("Login").build();
        refreshProvider.release(bAccount, "Token");
        
        assertThat(sessionRepository.count()).isZero();
    }
    
    @Test
    @DisplayName("Поиск УЗ по токену обновления сессии")
    void test_searchSession() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
    
        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setToken("Token");
        session.setExpireAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        session = sessionRepository.save(session);
        
        var found = refreshProvider.search(session.getId().toString());
        
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo(account.getLogin());
        assertThat(found.get().getHash()).isEqualTo(account.getHash());
    }
    
    @Test
    @DisplayName("Отмена точной сессии")
    void test_release_session() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
    
        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setToken("Token");
        session.setExpireAt(LocalDateTime.now(ZoneOffset.UTC));
        session = sessionRepository.save(session);
    
        assertThat(sessionRepository.count()).isEqualTo(1);
        
        refreshProvider.release(session.getId().toString());
    
        assertThat(sessionRepository.count()).isZero();
    }
    
    @Test
    @DisplayName("Поиск данных о токенах")
    void test_search_tokenData() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
    
        for (var i = 0; i < 3; i++) {
            var session = new SessionRecord();
            session.setId(UUID.randomUUID());
            session.setAccountId(account.getId());
            session.setToken("Token" + i);
            session.setExpireAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
            sessionRepository.save(session);
        }
        
        var bAccount = AccountDto.builder()
                                                                                .login("Login").build();
        
        var data = refreshProvider.searchTokenData(bAccount);
        var expectedSessions = sessionRepository.findAll(Sort.by("token"));
        var actualList = new ArrayList<>(data.entrySet());
        actualList.sort(Map.Entry.comparingByKey());
        
        assertThat(actualList).hasSize(3);
        for (var i = 0; i < 3; i++) {
            var expected = expectedSessions.get(i);
            var actual = actualList.get(i);
            assertThat(actual.getKey()).isEqualTo(expected.getToken());
            assertThat(actual.getValue()).isEqualTo(expected.getId().toString());
        }
    }
    
    @DisplayName("Проверка получения токена доступа")
    @Test
    void test_getAccessToken() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
    
        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setToken("Token");
        session.setExpireAt(LocalDateTime.now(ZoneOffset.UTC));
        session = sessionRepository.save(session);
        
        var actual = refreshProvider.getAccessToken(session.getId().toString());
        assertThat(actual).isEqualTo(session.getToken());
    }
    
    @DisplayName("Проверка получения УЗ по токену обновления")
    @Test
    void test_getAccountRecord() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        
        account = accountRepository.save(account);
    
        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setToken("Token");
        session.setExpireAt(LocalDateTime.now(ZoneOffset.UTC));
        session = sessionRepository.save(session);
        
        var actual = refreshProvider.getAccount(session.getId().toString());
        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(account.getId());
        assertThat(actual.get().getLogin()).isEqualTo(account.getLogin());
        assertThat(actual.get().getEmail()).isEqualTo(account.getEmail());
        assertThat(actual.get().getHash()).isEqualTo(account.getHash());
    }
    
}