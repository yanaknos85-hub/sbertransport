package ru.sber.transport.authentication.providers.refresh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.RefreshTokenData;
import ru.sber.transport.authentication.business.providers.RefreshProvider;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.records.SessionRecord;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.refresh.config.RtProperties;
import ru.sber.transport.authentication.providers.refresh.dao.SessionRepository;
import ru.sber.transport.authentication.providers.refresh.mapper.SessionMapper;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация провайдера токена обновления сессии.
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Component
public class RefreshProviderImpl implements RefreshProvider {

    private final SessionRepository sessionRepository;

    private final AccountRepository accountRepository;

    private final RtProperties rtProperties;

    private final SessionMapper sessionMapper;

    private final Clock clock;

    @Scheduled(cron = "0 */1 * * * *")
    void cleanupOldSessions() {
        var now = LocalDateTime.now(clock);
        var oldSessions = sessionRepository.findAllByExpiredAtBefore(now);
        sessionRepository.deleteAll(oldSessions);
        if (!oldSessions.isEmpty()) {
            log.info("%s expired sessions released".formatted(oldSessions.size()));
        }
    }

    @Override
    public RefreshTokenData generate(String accessToken, AccountDto accountDto) {
        log.debug("Generating refresh token for {} started at {}", accountDto.getLogin(), LocalDateTime.now(ZoneOffset.UTC));
        var expireData = rtProperties.getExpire();
        var expirationDate = LocalDateTime.now(ZoneOffset.UTC)
                .plusSeconds(expireData.getSeconds())
                .plusMinutes(expireData.getMinutes())
                .plusHours(expireData.getHours())
                .plusDays(expireData.getDays())
                .plusMonths(expireData.getMonths())
                .plusYears(expireData.getYears());

        var session = sessionRepository.findByTokenAndExpiredAtBefore(accessToken, LocalDateTime.now(ZoneOffset.UTC))
                .orElseGet(SessionRecord::new);

        var account = accountRepository.findByActiveTrueAndLogin(accountDto.getLogin())
                .orElseThrow(() -> new EntityNotFoundException(Account.class, accountDto.getLogin()));
        session.setId(UUID.randomUUID());
        session.setToken(accessToken);
        session.setExpireAt(expirationDate);
        session.setAccountId(account.getId());
        session.setCreationTime(LocalDateTime.now(ZoneOffset.UTC));

        session = sessionRepository.save(session);
        return new RefreshTokenData(session.getId().toString(), OffsetDateTime.of(session.getExpireAt(), ZoneOffset.UTC));
    }

    @Override
    public void release(AccountDto accountDto, String token) {
        var session = sessionRepository.findByToken(token);
        session.ifPresent(sessionRepository::delete);
    }

    @Override
    public Optional<AccountDto> search(String refresh) {
        return sessionRepository.findByIdAndExpiredAtAfter(UUID.fromString(refresh), LocalDateTime.now(ZoneOffset.UTC))
                .map(SessionRecord::getAccountId)
                .map(accountRepository::getById)
                .map(sessionMapper::toBusiness);
    }

    @Override
    public void release(String refresh) {
        sessionRepository.findById(UUID.fromString(refresh)).ifPresent(sessionRepository::delete);
    }

    @Override
    public Map<String, String> searchTokenData(AccountDto accountDto) {
        return sessionRepository
                .findAllByAccountAndExpiredAtAfter(accountDto.getLogin(), LocalDateTime.now(ZoneOffset.UTC))
                .stream().collect(Collectors.toMap(SessionRecord::getToken, session -> session.getId().toString()));
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Map<String, String> searchTokenData(Collection<AccountDto> accountDto) {
        return sessionRepository
                .findAllByAccountInAndExpiredAtAfter(accountDto.stream().map(AccountDto::getLogin).toList(), LocalDateTime.now(ZoneOffset.UTC))
                .stream().collect(Collectors.toMap(SessionRecord::getToken, session -> session.getId().toString()));
    }

    @Override
    public String getAccessToken(String refresh) {
        return sessionRepository.getById(UUID.fromString(refresh)).getToken();
    }

    @Override
    public Optional<AccountDto> getAccount(String refresh) {
        return sessionRepository.findById(UUID.fromString(refresh))
                .map(SessionRecord::getAccountId)
                .map(accountRepository::getById)
                .map(sessionMapper::toBusiness);
    }
}
