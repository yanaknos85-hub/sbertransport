package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;

import java.util.Set;

public interface RegistrySpecService {
    
    Specification<TaxiTripRegistry> getSpec(Set<String> taxiTripIds);
}
