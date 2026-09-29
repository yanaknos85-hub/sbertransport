package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.service.ContractService;
import ru.sberbank.ditsib.transport.reports.service.TariffService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiTariffServiceImpl implements TariffService<TaxiTariff> {
    private final TaxiTariffRepository taxiTariffRepository;
    private final ContractService contractService;

    @Override
    public TaxiTariff save(TaxiTariff taxiTariff) {
        return taxiTariffRepository.save(taxiTariff);
    }

    @Override
    public TaxiTariff updateOrCreate(TariffMessage message) {
        var taxiTariff = findById(message.getId()).orElse(null);
        if (taxiTariff == null) {
            taxiTariff = new TaxiTariff();
            taxiTariff.setId(message.getId());
        }

        var contract = contractService.findById(message.getContractId()).orElse(null);
        if (contract == null) {
            log.warn("Договор не найден: {}", message.getContractId());
            return null;
        }
        taxiTariff = taxiTariff.toBuilder()
                .humanReadableId(message.getHumanReadableId())
                .transportType(getTransportType())
                .regionId(message.getRegionId())
                .workGroup(message.getWorkGroup())
                .contract(contract)
                .build();

        return save(taxiTariff);
    }
    
    @Override
    public Optional<TaxiTariff> findById(UUID id) {
        return taxiTariffRepository.findById(id);
    }
    
    @Override
    public void deactivate(UUID id) {
        taxiTariffRepository.deactivate(id);
    }
    
    @Override
    public TransportTypeEnum getTransportType() {
        return TransportTypeEnum.TAXI;
    }
}
