package ru.sberbank.ditsib.transport.request.controller.carsharing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.carsharing.CarsharingJoinRequestTextController;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestTextDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.UpdateCarsharingJoinRequestTextDTO;
import ru.sberbank.ditsib.transport.request.mappers.CarsharingJoinRequestMapper;
import ru.sberbank.ditsib.transport.request.service.CarsharingJoinRequestTextService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@E2EController
public class CarsharingJoinRequestTextControllerImpl implements CarsharingJoinRequestTextController {
    
    private final CarsharingJoinRequestTextService textService;
    
    private final OrganizationService organizationService;
    
    private final CarsharingJoinRequestMapper mapper;
    
    @CheckOrganizationAccess
    @Override
    public GetCarsharingJoinRequestTextDTO getCreatedTextFieldsOrDefaults(@Organization UUID organizationId) {
        organizationService.check(organizationId);
        CarsharingJoinRequestText text = textService.getOrCreateDefaults(organizationId);
        return mapper.joinRequestTextToDto(text);
    }
    
    @CheckOrganizationAccess
    @Override
    public GetCarsharingJoinRequestTextDTO setToDefaults(@Organization UUID organizationId) {
        organizationService.check(organizationId);
        CarsharingJoinRequestText text = textService.setToDefaults(organizationId);
        return mapper.joinRequestTextToDto(text);
    }
    
    @CheckOrganizationAccess
    @Override
    public void updateTextFields(UpdateCarsharingJoinRequestTextDTO updateTextDto, @Organization UUID organizationId) {
        organizationService.check(organizationId);
        CarsharingJoinRequestText text = textService.getOrThrowException(organizationId);
        CarsharingJoinRequestText updatedText = mapper.updateJoinRequestByTextDto(text, updateTextDto);
        textService.save(updatedText);
    }
}
