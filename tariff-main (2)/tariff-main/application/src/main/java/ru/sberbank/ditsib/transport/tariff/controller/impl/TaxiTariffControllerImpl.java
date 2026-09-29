package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.TaxiTariffController;
import ru.sberbank.ditsib.transport.tariff.dto.NewTaxiTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TaxiTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация контроллера тарифов такси
 */
@RestController
@RequiredArgsConstructor
public class TaxiTariffControllerImpl implements TaxiTariffController {
    
    private final TariffControllerService service;
    
    
    @Override
    public TaxiTariffDTO add(@Valid NewTaxiTariffDTO newData) {
        return service.addTaxi(newData);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewTaxiTariffDTO newData) {
        service.edit(TransportTypeEnum.TAXI.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.TAXI.getId(), tariffId);
    }
    
    @Override
    public TaxiTariffDTO get(UUID tariffId) {
        return service.getTaxi(tariffId);
    }
    
    @Override
    public List<? extends TaxiTariffDTO> getAll(UUID regionId) {
        return service.getAllTaxi(regionId);
    }
}
