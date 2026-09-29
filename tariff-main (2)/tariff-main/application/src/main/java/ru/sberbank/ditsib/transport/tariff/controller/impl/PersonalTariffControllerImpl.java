package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.PersonalTariffController;
import ru.sberbank.ditsib.transport.tariff.dto.NewPersonalTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.PersonalTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация контроллера личного транспорта
 */
@RestController
@RequiredArgsConstructor
public class PersonalTariffControllerImpl implements PersonalTariffController {
    
    private final TariffControllerService service;
    
    
    @Override
    public PersonalTariffDTO add(@Valid NewPersonalTariffDTO newData) {
        return service.addPersonal(newData);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewPersonalTariffDTO newData) {
        service.edit(TransportTypeEnum.PERSONAL.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.PERSONAL.getId(), tariffId);
    }
    
    @Override
    public PersonalTariffDTO get(UUID tariffId) {
        return service.getPersonal(tariffId);
    }
    
    @Override
    public List<? extends PersonalTariffDTO> getAll(UUID regionId) {
        return service.getAllPersonal(regionId);
    }
}
