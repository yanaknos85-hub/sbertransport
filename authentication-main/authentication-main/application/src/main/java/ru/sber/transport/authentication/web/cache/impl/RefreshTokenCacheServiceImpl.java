package ru.sber.transport.authentication.web.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.business.dto.Token;
import ru.sber.transport.authentication.web.cache.RefreshTokenCacheService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCacheServiceImpl implements RefreshTokenCacheService {

    private final Map<String, Token> cache = new ConcurrentHashMap<>();

    private final Map<String, OffsetDateTime> deathTime = new ConcurrentHashMap<>();

    @Value("${cache.lifeTimeSeconds:5}")
    private int cacheLifeTimeSeconds;

    @Scheduled(fixedRate = 1000)
    void remove(){
        log.trace("Removing cached sessions started");
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var iterator = deathTime.entrySet().iterator();
        while (iterator.hasNext()){
            var item = iterator.next();
            if (item.getValue().isEqual(now) || item.getValue().isBefore(now)) {
                iterator.remove();
                cache.remove(item.getKey());
            }
        }
    }

    @Override
    public void add(String key, Token value){
        cache.put(key, value);
        deathTime.put(key, OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(cacheLifeTimeSeconds));
    }

    @Override
    public Token get(String key) {
        return cache.get(key);
    }

}
