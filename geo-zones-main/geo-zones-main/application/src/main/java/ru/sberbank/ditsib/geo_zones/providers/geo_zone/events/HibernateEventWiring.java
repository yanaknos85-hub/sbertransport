package ru.sberbank.ditsib.geo_zones.providers.geo_zone.events;

import lombok.RequiredArgsConstructor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionImpl;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.listeners.FrequentlyAddressMergedListener;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.listeners.FrequentlyAddressPersistedListener;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.listeners.FrequentlyAddressRemovedListener;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

/**
 * Подключение событий к ОРМ.
 */
@Configuration
@RequiredArgsConstructor
public class HibernateEventWiring {
    
    private final EntityManager entityManager;
    
    private final FrequentlyAddressPersistedListener persistedListener;
    
    private final FrequentlyAddressMergedListener mergedListener;
    
    private final FrequentlyAddressRemovedListener removedListener;
    
    @PostConstruct
    public void registerListeners() {
        var sessionFactory = ((SessionImpl) entityManager.getDelegate()).getSessionFactory();
        var registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        
        registry.getEventListenerGroup(EventType.MERGE).prependListener(mergedListener);
        registry.getEventListenerGroup(EventType.PERSIST).prependListener(persistedListener);
        registry.getEventListenerGroup(EventType.DELETE).prependListener(removedListener);
    }
}