package ru.sberbank.ditsib.transport.vehicle.service;

import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyPaginationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyUpdateDto;

import java.util.UUID;

public interface AttorneyService {

    Page<AttorneyDto> getAll(AttorneyPaginationRequestDto paginationRequest, UUID userId);

    AttorneyDto create(AttorneyCreateDto requestDto, UUID userId);

    AttorneyDto update(AttorneyUpdateDto requestDto, UUID attorneyId, UUID userId);
    
    AttorneyDto getAttorneyByTelemechanicId(UUID telemechanicId);
}
