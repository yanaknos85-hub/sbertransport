package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveDto;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с типами двигателей траспортного средства
 */
@Validated
public interface DriveService {
    DriveDto get(UUID id);
    
    DriveDto create(@Valid DriveRequestDto requestDto);
    
    DriveDto update(UUID id, @Valid DriveRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<DriveDto> findAll(PageSettingDto pageSetting);
}
