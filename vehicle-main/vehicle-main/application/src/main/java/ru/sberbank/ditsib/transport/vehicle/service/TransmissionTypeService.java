package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeRequestDto;

import java.util.UUID;

public interface TransmissionTypeService {
    TransmissionTypeDto get(UUID id);

    TransmissionTypeDto create(@Valid TransmissionTypeRequestDto requestDto);

    TransmissionTypeDto update(UUID id, @Valid TransmissionTypeRequestDto requestDto);

    void delete(UUID id);

    Page<TransmissionTypeDto> findAll(PaginationCommonRequestDto paginationRequest);
}
