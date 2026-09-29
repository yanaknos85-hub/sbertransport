package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;
import ru.sberbank.ditsib.transport.reports.service.RegistrySpecService;

import java.time.LocalDate;
import java.util.Set;

import static ru.sberbank.ditsib.transport.reports.dao.spec.RegistrySpecs.createdBetween;
import static ru.sberbank.ditsib.transport.reports.dao.spec.RegistrySpecs.withTaxiTripIds;


@Slf4j
@RequiredArgsConstructor
@Service
public class RegistrySpecServiceImpl implements RegistrySpecService {
    
    @Override
    public Specification<TaxiTripRegistry> getSpec(Set<String> taxiTripIds) {
        return withTaxiTripIds(taxiTripIds);
    }
}
