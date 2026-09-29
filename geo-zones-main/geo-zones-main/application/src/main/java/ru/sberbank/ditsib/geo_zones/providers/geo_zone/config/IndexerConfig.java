package ru.sberbank.ditsib.geo_zones.providers.geo_zone.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Конфигурация поискового движка. Включает индексирование базы и прицепляет его к менеджеру сущностей.
 */
@Slf4j
@Transactional
@Component
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.search.enabled", havingValue = "true")
public class IndexerConfig implements ApplicationListener<ApplicationReadyEvent> {
    
    private final EntityManager entityManager;
    
    @SneakyThrows(InterruptedException.class)
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent applicationReadyEvent) {
        log.info("Enabling indexer");
        var fullTextEntityManager = Search.session(entityManager);
        var indexer = fullTextEntityManager.massIndexer();
        indexer.startAndWait();
    }
}
