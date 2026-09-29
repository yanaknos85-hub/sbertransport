package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с типами двигателей траспортного средства
 */
@Validated
public interface EngineTypeService {
    EngineTypeDto get(UUID id);
    
    EngineTypeDto create(@Valid EngineTypeRequestDto requestDto);
    
    EngineTypeDto update(UUID id, @Valid EngineTypeRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<EngineTypeDto> findAll(PageSettingDto pageSetting);
}
