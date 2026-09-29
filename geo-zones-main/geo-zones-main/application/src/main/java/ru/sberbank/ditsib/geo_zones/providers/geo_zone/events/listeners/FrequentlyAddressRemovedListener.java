package ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.listeners;

import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultDeleteEventListener;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo_zones.messaging.senders.GeoZoneSender;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Слушатель событий удаления.
 */
@Component
@RequiredArgsConstructor
public class FrequentlyAddressRemovedListener extends DefaultDeleteEventListener {
    
    private final transient GeoZoneSender sender;
    
    @Override
    public void onDelete(DeleteEvent event) throws HibernateException {
        super.onDelete(event);
        var entity = event.getObject();
        var entityClass = entity.getClass();
        var annotations = ReflectionUtils.getAnnotation(Indexed.class, entityClass);
        if (annotations != null) {
            var session = Search.session(event.getSession());
            var indexingPlan = session.indexingPlan();
            indexingPlan.delete(entity);
            indexingPlan.execute();
        }
        sender.send(ReflectionUtils.cast(entity), true);
    }
    
}
