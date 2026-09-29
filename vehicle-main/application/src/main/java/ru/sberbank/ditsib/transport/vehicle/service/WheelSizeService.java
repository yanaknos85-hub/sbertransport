package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeRequestDto;

import java.util.UUID;

public interface WheelSizeService {
    WheelSizeDto get(UUID id);

    WheelSizeDto create(@Valid WheelSizeRequestDto requestDto);

    WheelSizeDto update(UUID id, @Valid WheelSizeRequestDto requestDto);

    void delete(UUID id);

    Page<WheelSizeDto> findAll(PaginationCommonRequestDto paginationRequest);
}
