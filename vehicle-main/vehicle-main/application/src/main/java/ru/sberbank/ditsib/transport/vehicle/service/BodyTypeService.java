package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeRequestDto;

import java.util.UUID;

public interface BodyTypeService {
    BodyTypeDto get(UUID id);

    BodyTypeDto create(@Valid BodyTypeRequestDto requestDto);

    BodyTypeDto update(UUID id, @Valid BodyTypeRequestDto requestDto);

    void delete(UUID id);

    Page<BodyTypeDto> findAll(PaginationCommonRequestDto paginationRequest);
}
