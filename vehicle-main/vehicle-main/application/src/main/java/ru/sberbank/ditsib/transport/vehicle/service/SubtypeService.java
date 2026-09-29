package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с видами траспортного средства
 */
@Validated
public interface SubtypeService {
    SubtypeDto get(UUID id);
    
    SubtypeDto create(@Valid SubtypeRequestDto requestDto);
    
    SubtypeDto update(UUID id, @Valid SubtypeRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<SubtypeDto> findAll(PaginationCommonRequestDto paginationRequest);
}
