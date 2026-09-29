package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.BicycleTariffController;
import ru.sberbank.ditsib.transport.tariff.dto.BicycleTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewBicycleTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация контроллера тарифов велосипеда
 */
@RestController
@RequiredArgsConstructor
public class BicycleTariffControllerImpl implements BicycleTariffController {
    
    private final TariffControllerService service;
    
    
    @Override
    public BicycleTariffDTO add(@Valid NewBicycleTariffDTO newData) {
        return service.addBicycle(newData);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewBicycleTariffDTO newData) {
        service.edit(TransportTypeEnum.BICYCLE.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.BICYCLE.getId(), tariffId);
    }
    
    @Override
    public BicycleTariffDTO get(UUID tariffId) {
        return service.getBicycle(tariffId);
    }
    
    @Override
    public List<? extends BicycleTariffDTO> getAll(UUID regionId) {
        return service.getAllBicycle(regionId);
    }
}
