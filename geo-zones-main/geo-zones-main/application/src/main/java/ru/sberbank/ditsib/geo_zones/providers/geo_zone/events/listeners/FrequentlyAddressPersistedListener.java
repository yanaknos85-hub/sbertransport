package ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.listeners;

import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultPersistEventListener;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo_zones.messaging.senders.GeoZoneSender;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Слушатель событий персиста.
 */
@Component
@RequiredArgsConstructor
public class FrequentlyAddressPersistedListener extends DefaultPersistEventListener {
    
    private final transient GeoZoneSender sender;
    
    @Override
    public void onPersist(PersistEvent event) throws HibernateException {
        super.onPersist(event);
        var entity = event.getObject();
        var entityClass = entity.getClass();
        var annotations = ReflectionUtils.getAnnotation(Indexed.class, entityClass);
        if (annotations != null) {
            var session = Search.session(event.getSession());
            var indexingPlan = session.indexingPlan();
            indexingPlan.addOrUpdate(entity);
            indexingPlan.execute();
        }
        sender.send(ReflectionUtils.cast(entity), false);
    }
    
}
