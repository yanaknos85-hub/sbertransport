package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.PublicTariffController;
import ru.sberbank.ditsib.transport.tariff.dto.NewPublicTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.PublicTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация контроллера тарифов общественного транспорта
 */
@RestController
@RequiredArgsConstructor
public class PublicTariffControllerImpl implements PublicTariffController {
    
    private final TariffControllerService service;
    
    @Override
    public PublicTariffDTO add(@Valid NewPublicTariffDTO newData) {
        return service.addPublic(newData);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewPublicTariffDTO newData) {
        service.edit(TransportTypeEnum.PUBLIC.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.PUBLIC.getId(), tariffId);
    }
    
    @Override
    public PublicTariffDTO get(UUID tariffId) {
        return service.getPublic(tariffId);
    }
    
    @Override
    public List<? extends PublicTariffDTO> getAll(UUID regionId) {
        return service.getAllPublic(regionId);
    }
}
