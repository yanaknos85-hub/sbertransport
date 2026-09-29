package ru.sber.transport.authentication.providers.two_factor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.authentication.business.dto.AccessTokenData;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.TwoFactor;
import ru.sber.transport.authentication.business.providers.TwoFactorProvider;
import ru.sber.transport.database.authentication.tables.records.TwoFactorRecord;
import ru.sber.transport.authentication.providers.two_factor.config.TwoFactorProperties;
import ru.sber.transport.authentication.providers.two_factor.mappers.TwoFactorBusinessMapper;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
@Transactional
public class TwoFactorProviderImpl implements TwoFactorProvider, JooqRepository<ru.sber.transport.database.authentication.tables.TwoFactor, TwoFactorRecord, UUID> {

    private final TwoFactorBusinessMapper mapper;

    private final TwoFactorProperties twoFactorProperties;

    private final Clock clock;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    void releaseOld() {
        var deleted = context().deleteFrom(table())
                .where(table().EXPIRE.lessThan(LocalDateTime.now(clock)))
                .execute();
        if (deleted > 0 && log.isDebugEnabled()) {
            log.debug("Released {} codes", deleted);
        }
    }

    @Override
    public TwoFactor generate(AccountDto account, AccessTokenData data) {
        var twoFactor = new TwoFactorRecord();
        twoFactor.setId(account.getId());
        twoFactor.setExpire(data.expiration().toLocalDateTime());
        twoFactor.setToken(data.value());

        var code = "";
        do {
            code = generateCode();
        } while (context().fetchExists(context().selectFrom(table()).where(table().CODE.eq(code))));
        twoFactor.setCode(generateCode());
        context().insertInto(table()).set(twoFactor)
                .onConflict(table().ID)
                .doUpdate().set(twoFactor)
                .execute();
        return mapper.map(twoFactor);
    }

    @Override
    public boolean checkCode(UUID userId, String code) {
        var selectFactor = context().selectFrom(table())
                .where(table().ID.eq(userId))
                .and(table().CODE.eq(code))
                .and(table().EXPIRE.greaterThan(LocalDateTime.now(clock)));
        return context().fetchExists(selectFactor);
    }

    @Override
    public void removeCode(UUID userId) {
        deleteById(userId);
    }

    @Override
    public String codeUrl() {
        var url = twoFactorProperties.getCode().getUrl();
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String generateCode() {
        var codeProperties = twoFactorProperties.getCode();
        var possibleChars = codeProperties.getChars();
        var code = new char[codeProperties.getLength()];
        var charsCount = possibleChars.length;
        var random = new SecureRandom();
        var lastIndex = charsCount - 1;
        for (var i = 0; i < code.length; i++) {
            var nextRandomInt = random.nextInt(lastIndex);
            code[i] = possibleChars[nextRandomInt];
        }
        return new String(code);
    }

    @Override
    public ru.sber.transport.database.authentication.tables.TwoFactor table() {
        return ru.sber.transport.database.authentication.tables.TwoFactor.TWO_FACTOR;
    }
}
