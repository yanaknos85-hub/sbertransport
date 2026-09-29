package ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao.SearchRepository;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone_;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * Реализация репозитория с поиском.
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Repository
class SearchRepositoryImpl implements SearchRepository {
    
    private final EntityManager entityManager;

    @SuppressWarnings("java:S3776")
    @Override
    public Optional<GeoZone> search(String region, String district, String city, String street, String house) {
        var session = Search.session(entityManager);
    
        /*
         * От частного к общему.
         * Если полнотекстным поиском находится геозона минимального размера, то проверяется цепочка родителей
         * запрошенной геозоны. Если цепочка совпадает, это искомая геозона. Если нет, повторяем поиск геозоны более
         * общего уровня.
         */
        var hits = getHits(house, session);
        for (var hit : hits) {
            if (checkHit(hit, region, district, city, street)) {
                return Optional.of(hit);
            }
        }
        
        hits = getHits(street, session);
        for (var hit : hits) {
            if (checkHit(hit, region, district, city)) {
                return Optional.of(hit);
            }
        }
        
        hits = getHits(city, session);
        var cityHits = new ArrayList<>(hits);
        for (var hit: hits){
            if (checkHit(hit, region, district)) {
                return Optional.of(hit);
            }
            if (checkHit(hit, region)) {
                return Optional.of(hit);
            }
            cityHits.remove(hit);
        }

        hits = getHits(district, session);
        var districtHits = new ArrayList<>(hits);
        for (var hit : hits){
            if (checkHit(hit, region)){
                return Optional.of(hit);
            } else {
                districtHits.remove(hit);
            }
        }

        List<GeoZone> regionHits = Collections.emptyList();
        if (districtHits.isEmpty() && cityHits.isEmpty()) {
            regionHits = getHits(region, session);
        }
        if (regionHits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(regionHits.get(0));
    }
    
    /**
     * Проверка результата.
     *
     * @param geoZone найденная зона.
     * @param checkStrings список проверок.
     * @return результат.
     */
    private boolean checkHit(GeoZone geoZone, String... checkStrings) {
        if (geoZone == null) {
            return false;
        }
        var newLength = checkStrings.length - 1;
        var lastWord = checkStrings[newLength];
        var parent = geoZone.getParent();
        var check = Arrays.copyOf(checkStrings, newLength);
        if (lastWord == null && parent != null && check.length > 0) {
            return checkHit(parent, check);
        }
        if (lastWord != null && parent != null && parent.getName().toLowerCase(Locale.ROOT).contains(lastWord.toLowerCase(Locale.ROOT))) {
            if (check.length > 0) {
                return checkHit(parent, check);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Получение результатов.
     *
     * @param data данные для получения данных.
     * @param session сессия для получения данных.
     * @return список результатов.
     */
    private List<GeoZone> getHits(String data, SearchSession session) {
        if (data == null) {
            return Collections.emptyList();
        }
        var select = session.search(GeoZone.class).extension(LuceneExtension.get());
        var query = select.where(f -> createWhere(f, data));
        return query.fetchAllHits();
    }
    
    /**
     * Создает выражение условия.
     *
     * @param f фабрика условий.
     * @param searchString поисковая строка.
     * @return объект финального шага для построения запроса.
     */
    private PredicateFinalStep createWhere(
            SearchPredicateFactory f, String searchString
                                          ) {
        return f.phrase().fields(GeoZone_.NAME).matching(searchString);
    }
}
