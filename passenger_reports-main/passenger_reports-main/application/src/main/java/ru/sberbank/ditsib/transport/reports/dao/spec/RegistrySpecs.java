package ru.sberbank.ditsib.transport.reports.dao.spec;

import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.reports.model.excel.*;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import java.time.LocalDate;
import java.util.Set;

public class RegistrySpecs {
    
    public static Specification<TaxiTripRegistry> createdBetween(LocalDate creationFrom, LocalDate creationTo){
        return (root, query, cb) -> {
            if (creationTo != null) {
                return cb.and(cb.between(root.get(TaxiTripRegistry_.date), creationFrom, creationTo));
            } else {
                return cb.and(cb.greaterThan(root.get(TaxiTripRegistry_.date),
                                             creationFrom));
            }
        };
    };
    
    public static Specification<TaxiTripRegistry> withTaxiTripIds(Set<String> taxiTripIds){
        return (root, query, cb) -> {
            ListJoin<TaxiTripRegistry, TaxiTripRegistryString> taxiTripRegistryStringListJoin =
                    root.join(TaxiTripRegistry_.registryStrings);
            Join<TaxiTripRegistryString, TaxiTripRegistryStringTemplate> templateJoin =
                    taxiTripRegistryStringListJoin.join(TaxiTripRegistryString_.parsedString);
                
            return cb.and(templateJoin.get(TaxiTripRegistryStringTemplate_.taxiTripId).in(taxiTripIds));
        };
    };
}
