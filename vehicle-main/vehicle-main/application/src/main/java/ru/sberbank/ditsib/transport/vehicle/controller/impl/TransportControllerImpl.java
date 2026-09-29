package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.vehicle.controller.TransportController;
import ru.sberbank.ditsib.transport.vehicle.dto.StateNumberSearchRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransportInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.DeactivationDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchWithStructureRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSelfSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.TransportUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.*;
import ru.sberbank.ditsib.transport.vehicle.exception.ExploitationDateException;
import ru.sberbank.ditsib.transport.vehicle.exception.MileageNotIncrementedException;
import ru.sberbank.ditsib.transport.vehicle.exception.OrgDepRelationValidationException;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.AccessiblePositionService;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransportControllerImpl implements TransportController {

    private final EmployeeService employeeService;
    private final TransportService transportService;
    private final AccessiblePositionService accessiblePositionService;

    @Override
    public TransportResponseDto getById(UUID transportId, Authentication authentication) {
        return transportService.getById(transportId);
    }

    @Override
    public void add(TransportCreateDto requestDto, Authentication authentication) {
        transportService.create(requestDto);
    }

    @Override
    public TransportResponseDto update(UUID transportId, TransportUpdateDto requestDto, Authentication authentication) {
        return transportService.update(transportId, requestDto);
    }

    @Override
    public void deactivate(UUID transportId, DeactivationDto deactivationDto, Authentication authentication) {
        transportService.deactivate(transportId, deactivationDto);
    }

    @Override
    public Page<TransportSearchResponseDto> searchAllOrganizations(TransportSearchingRequestDto searchingRequestDto, Authentication authentication) {
        return transportService.searchAllOrganizations(searchingRequestDto);
    }

    @Override
    public Page<TransportSearchResponseDto> searchSelfOrganizations(TransportSelfSearchingRequestDto selfSearchingRequestDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return transportService.searchSelfOrganizations(selfSearchingRequestDto, userId);
    }

    @Override
    public Page<TransportSearchResponseDto> search(TransportSearchingRequestDto searchingRequestDto, Authentication authentication) {
        return transportService.search(searchingRequestDto);
    }

    @Override
    public Page<TransportSearchResponseDtoV2> searchWithBrandAndModel(TransportSearchingRequestDto searchingRequestDto, Authentication authentication) {
        return transportService.searchWithBrandAndModel(searchingRequestDto);
    }

    @Override
    public Page<TransportInfoDto> getTransportByStateNumber(StateNumberSearchRequestDto requestDto, Authentication authentication) {
        return transportService.searchByStateNumber(requestDto);
    }

    @Override
    public Page<TransportSearchWithStructureResponseDto> searchWithStructure(TransportSearchWithStructureRequestDto requestDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return transportService.searchWithStructure(requestDto, userId);
    }

    @Override
    public List<AccessiblePositionDto> getAccessiblePositions() {
        return accessiblePositionService.findAll();
    }

    @ExceptionHandler(ExploitationDateException.class)
    public ResponseEntity<Object> handleException(ExploitationDateException exception) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                exception.getMessage())).build();
    }

    @ExceptionHandler(OrgDepRelationValidationException.class)
    public ResponseEntity<Object> handleException(OrgDepRelationValidationException exception) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                exception.getMessage())).build();
    }

    @ExceptionHandler(MileageNotIncrementedException.class)
    public ResponseEntity<Object> handleException(MileageNotIncrementedException exception) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                                                                  exception.getMessage())).build();
    }

}
