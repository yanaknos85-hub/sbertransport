package ru.sberbank.ditsib.transport.request.controller.publicTransport.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.publicTransport.PublicTransportRequestController;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.RegionDto;
import ru.sberbank.ditsib.transport.request.dto.RequestProjection;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.NewRequestForCompensationDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.RequestForCompensationDTO;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.ApprovalsSettingsInjectionService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.publicTransport.impl.RequestForPublicServiceImpl;

import java.time.ZoneId;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Transactional
@Slf4j
@E2EController
public class PublicTransportRequestControllerImpl implements PublicTransportRequestController {
    
    private final EmployeeService employeeService;
    private final RequestForPublicServiceImpl requestService;
    private final ApprovalsSettingsInjectionService approvalsSettingsInjectionService;
    private final RegionDataResolver regionDataResolver;
    
    @Override
    public RequestForCompensationDTO saveRequest(
            @Valid @RequestBody NewRequestForCompensationDTO newRequest, JwtAuthenticationToken authentication
                                                ) {
        var regionBranch = regionDataResolver.getRegionBranch(newRequest.getWaypoints().get(0));
        formatDesiredDateBasedOnRegionTimeZone(newRequest, regionBranch);
        return requestService.addCompensationRequest(authentication, newRequest);
    }
    
    @Override
    public void editRequest(
            UUID requestId, RequestForCompensationDTO newData, JwtAuthenticationToken authentication
                           ) {
        Employee initiator = employeeService.getAuthenticatedEmployee(authentication);
        requestService.edit(requestId, initiator, newData);
    }
    
    @Override
    public RequestForCompensationDTO getRequest(@PathVariable("requestId") UUID requestId, RequestProjection projection) {
        return requestService.getRequestForPublicCompensation(requestId, projection);
    }
    
    @Override
    public void confirmRequest(
            UUID requestId, List<CompensationDocumentDTO> documents, JwtAuthenticationToken authentication
                              ) {
        var initiator = employeeService.getAuthenticatedEmployee(authentication);
        requestService.confirm(requestId, initiator, documents);
    }
    
    private void checkConfirmationDocs(Employee initiator, List<CompensationDocumentDTO> documents) {
        // Если требуется прикрепление - убедиться, что документ был прикреплен
        if (approvalsSettingsInjectionService.isPublicTrConfirmationDocumentRequired(initiator)) {
            if (documents == null || documents.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не были прикреплены подтверждающие документы!");
            }
            validateDocs(documents);
        }
    }
    
    /**
     * Ручной вызов валидатора
     */
    private void validateDocs(List<CompensationDocumentDTO> documents) {
        try (final var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final var validator = validatorFactory.getValidator();
            Set<ConstraintViolation<CompensationDocumentDTO>> violations = new HashSet<>();
            documents.stream().map(doc -> validator.validate(doc)).forEach(violations::addAll);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
    }
    
    private void formatDesiredDateBasedOnRegionTimeZone(NewRequestForCompensationDTO dto, List<RegionDto> regionBranch) {
        var regionTimeZone = regionBranch.stream().map(RegionDto::getTimeZone).filter(Objects::nonNull).findFirst().orElse(null);
        if (regionTimeZone == null) {
            throw new RuntimeException("Не удалось получить таймзону для региона");
        }
        /*
         * Приводим время относительно региона
         * C фронта приходит UTC 07:30
         * Приводим ко времени пользователя 10:30 для GMT+3
         * Меняем на зону для региона (например GMT+10), получаем 10:30 GMT+10
         * Приводим к UTC и получаем 00:30
         */
        var utcTripDateBasedOnRegionTimeZone = dto.getDesiredDate()
                                                  .atZone(ZoneId.of("UTC"))
                                                  .withZoneSameInstant(ZoneId.of(dto.getTimeZone()))
                                                  .withZoneSameLocal(ZoneId.of(regionTimeZone))
                                                  .withZoneSameInstant(ZoneId.of("UTC"))
                                                  .toLocalDateTime();
        
        log.debug("tripTime: {}, client time zone: {}, region time zone: {}, final time {}", dto.getDesiredDate(), dto.getTimeZone(),
                  regionTimeZone, utcTripDateBasedOnRegionTimeZone);
        
        //Сохраняем таймзону устройства
        dto.setEmployeeDeviceTimeZone(dto.getTimeZone());
        
        dto.setDesiredDate(utcTripDateBasedOnRegionTimeZone);
        dto.setTimeZone("GMT" + ZoneId.of(regionTimeZone).normalized());
    }
}
