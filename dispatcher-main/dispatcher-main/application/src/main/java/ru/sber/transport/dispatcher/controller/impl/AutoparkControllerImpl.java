package ru.sber.transport.dispatcher.controller.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.dispatcher.dto.AutoparkDTO;
import ru.sber.transport.dispatcher.dto.NewAutoparkDTO;
import ru.sber.transport.dispatcher.dto.search.AutoparkSearchDTO;
import ru.sber.transport.dispatcher.service.AutoparkService;
import ru.sber.transport.dispatcher.controller.AutoparkController;

import jakarta.validation.Valid;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementation of autoparks controller.
 */
@RequiredArgsConstructor
@RestController
public class AutoparkControllerImpl implements AutoparkController {
    
    private final AutoparkService autoparkService;
    
    @Override
    public AutoparkDTO add(UUID contractorId, @Valid NewAutoparkDTO autopark) {
        return autoparkService.add(contractorId, autopark);
    }
    
    @Override
    public void edit(UUID contractorId, UUID autoparkId, @Valid NewAutoparkDTO autopark) {
        autoparkService.edit(contractorId, autoparkId, autopark);
    }
    
    @Override
    public void delete(UUID contractorId, UUID autoparkId) {
        autoparkService.delete(contractorId, autoparkId);
    }
    
    @Override
    public AutoparkDTO get(UUID contractorId, UUID autoparkId) {
        return autoparkService.get(contractorId, autoparkId);
    }
    
    @Override
    public Page<AutoparkDTO> getAll(UUID contractorId, AutoparkSearchDTO autoparkSearchDTO) {
        return autoparkService.get(contractorId, autoparkSearchDTO);
    }
    
}
