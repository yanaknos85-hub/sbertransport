package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsRequestDto;

import java.util.UUID;

public interface TelematicsService {
    TelematicsDto get(UUID id);

    TelematicsDto create(@Valid TelematicsRequestDto requestDto);

    TelematicsDto update(UUID id, @Valid TelematicsRequestDto requestDto);

    void delete(UUID id);

    Page<TelematicsDto> findAll(PaginationCommonRequestDto paginationRequest);
}
