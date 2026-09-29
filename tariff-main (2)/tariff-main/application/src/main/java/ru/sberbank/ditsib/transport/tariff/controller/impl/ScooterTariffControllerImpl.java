package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.ScooterTariffController;
import ru.sberbank.ditsib.transport.tariff.dto.NewScooterTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.ScooterTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация контроллера тарифов самоката
 */
@RestController
@RequiredArgsConstructor
public class ScooterTariffControllerImpl implements ScooterTariffController {
    
    private final TariffControllerService service;
    
    
    @Override
    public ScooterTariffDTO add(@Valid NewScooterTariffDTO newData) {
        return service.addScooter(newData);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewScooterTariffDTO newData) {
        service.edit(TransportTypeEnum.SCOOTER.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.SCOOTER.getId(), tariffId);
    }
    
    @Override
    public ScooterTariffDTO get(UUID tariffId) {
        return service.getScooter(tariffId);
    }
    
    @Override
    public List<? extends ScooterTariffDTO> getAll(UUID regionId) {
        return service.getAllScooter(regionId);
    }
}
