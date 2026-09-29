package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.reports.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.TaxiTariffListener;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.service.ContractService;
import ru.sberbank.ditsib.transport.reports.service.OrganizationService;
import ru.sberbank.ditsib.transport.reports.service.TariffService;

import java.util.UUID;

@RequiredArgsConstructor
@Component("taxiTariffInput")
public class TaxiTariffListenerImpl implements TaxiTariffListener   {
    
    private final TariffService<TaxiTariff> tariffService;
    private final TariffMapper tariffMapper;
    private final OrganizationService organizationService;
    private final ContractService contractService;
    
    @Override
    public void handle(UUID key, TaxiTariffMessage message) {
    }
}
