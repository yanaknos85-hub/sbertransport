package ru.sberbank.ditsib.transport.srm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.srm.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.srm.service.TaxiTariffService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiTariffServiceImpl implements TaxiTariffService {
    
    private final TaxiTariffRepository taxiTariffRepository;
    
    @Override
    public TaxiTariff save(TaxiTariff taxiTariff) {
        return taxiTariffRepository.save(taxiTariff);
    }
    
    @Override
    public Optional<TaxiTariff> findById(UUID id) {
        return taxiTariffRepository.findById(id);
    }
    
    @Override
    public void delete(TaxiTariff taxiTariff) {
        taxiTariffRepository.delete(taxiTariff);
    }

}
