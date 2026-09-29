package ru.sberbank.transport.oto.cargo.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.controller.OtoEngineerController;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;
import ru.sberbank.transport.oto.cargo.dto.oto.GetTemplateForCargoForOtoDto;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;
import ru.sberbank.transport.oto.cargo.service.OtoEngineerService;
import ru.sberbank.transport.oto.cargo.service.ValidationService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class OtoEngineerControllerImpl implements OtoEngineerController {
    
    private final OtoEngineerService otoService;

    private final ValidationService validationService;

    @Override
    public Page<OtoEngineerCargoRequestDetailDTO> getCargoRequests(
            CargoRequestDto cargoRequestDto,
            JwtAuthenticationToken authentication) {
        validationService.validateRequestScopeVisibility(cargoRequestDto);
        validationService.validateEmptyExecutorGroups(cargoRequestDto);
        return otoService.getCargoRequests(cargoRequestDto, authentication);
    }
    
    @Override
    public Page<GetTemplateForCargoForOtoDto> getCargoTemplatesForOto(
            UUID organizationId,
            Integer size,
            Integer page,
            SortDirection direction,
            String field,
            String humanReadableId,
            TripRequestStatus[] statuses,
            ZonedDateTime creationTimeFrom,
            ZonedDateTime creationTimeTo,
            String senderName,
            String senderAddress,
            String recipientName,
            String recipientAddress,
            UUID authorDepartment) {
        return otoService.getTemplatesForCargo(organizationId, size, page, direction, field, humanReadableId,
                                               Optional.ofNullable(statuses).map(Arrays::asList).orElseGet(ArrayList::new),
                                               convertTime(creationTimeFrom), convertTime(creationTimeTo),
                                               senderName, senderAddress, recipientName, recipientAddress, authorDepartment);
    }
    
    private LocalDateTime convertTime(ZonedDateTime timeToConvert) {
        return Optional.ofNullable(timeToConvert)
                       .map(time -> time.toInstant().atZone(ZoneOffset.UTC))
                       .map(ZonedDateTime::toLocalDateTime).orElse(null);
    }
}