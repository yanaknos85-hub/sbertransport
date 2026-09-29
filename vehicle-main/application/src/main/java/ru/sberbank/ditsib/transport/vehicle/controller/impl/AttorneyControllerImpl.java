package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.vehicle.controller.AttorneyController;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyPaginationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.AttorneyService;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Slf4j
public class AttorneyControllerImpl implements AttorneyController {

    private final AttorneyService attorneyService;

    @Override
    public Page<AttorneyDto> getAll(AttorneyPaginationRequestDto paginationRequest, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return attorneyService.getAll(paginationRequest, userId);
    }

    @Override
    public AttorneyDto add(AttorneyCreateDto requestDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return attorneyService.create(requestDto, userId);
    }

    @Override
    public AttorneyDto update(AttorneyUpdateDto requestDto, UUID attorneyId, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return attorneyService.update(requestDto, attorneyId, userId);
    }
    
    @Override
    public AttorneyDto getAttorneyByTelemechanicId(Authentication authentication) {
        var telemechanicId = UserAuthorizationHelper.getUserId(authentication);
        return attorneyService.getAttorneyByTelemechanicId(telemechanicId);
    }
}
