package ru.sberbank.ditsib.transport.request.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.ConstantController;
import ru.sberbank.ditsib.transport.request.dto.constant.TransportClassDTO;
import ru.sberbank.ditsib.transport.request.mappers.ConstantMapper;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@E2EController
public class ConstantControllerImpl implements ConstantController {
    
    private final ConstantMapper mapper;
    
    private List<TransportClassDTO> taxiClass;
    
    private List<TransportClassDTO> transferClass;
    
    @Override
    public List<TransportClassDTO> taxiClass() {
        if (taxiClass == null) {
            taxiClass = Arrays.stream(TaxiClass.values()).map(mapper::toTaxi).toList();
        }
        return taxiClass;
    }
    
    @Override
    public List<TransportClassDTO> transferClass() {
        if (transferClass == null) {
            transferClass = Arrays.stream(GroupTransferClass.values()).map(mapper::toTransfer).toList();
        }
        return transferClass;
    }
}
