package ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.listeners;

import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo_zones.messaging.senders.GeoZoneSender;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Слушатель событий мержа.
 */
@Component
@RequiredArgsConstructor
public class FrequentlyAddressMergedListener extends DefaultMergeEventListener {
    
    private final transient GeoZoneSender sender;
    
    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        super.onMerge(event);
        var entity = event.getOriginal();
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
