package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.GroupTransferTariffController;
import ru.sberbank.ditsib.transport.tariff.dto.GroupTransferTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewGroupTransferTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Имплементация контроллера тарифов группового трансфера
 */
@RestController
@RequiredArgsConstructor
public class GroupTransferTariffControllerImpl implements GroupTransferTariffController {
    
    private final TariffControllerService service;
    
    
    @Override
    public GroupTransferTariffDTO add(@Valid NewGroupTransferTariffDTO newData) {
        return service.addGroupTransfer(newData);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewGroupTransferTariffDTO newData) {
        service.edit(TransportTypeEnum.GROUP_TRANSFER.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.GROUP_TRANSFER.getId(), tariffId);
    }
    
    @Override
    public GroupTransferTariffDTO get(UUID tariffId) {
        return service.getGroupTransfer(tariffId);
    }
    
    @Override
    public List<? extends GroupTransferTariffDTO> getAll(UUID regionId) {
        return service.getAllGroupTransfer(regionId);
    }
}
