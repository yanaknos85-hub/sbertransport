package ru.sberbank.ditsib.transport.request.controller.carsharing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.carsharing.CarsharingJoinRequestEngineerController;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestShortDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.ProcessedContractorAndJoinStatusDTO;
import ru.sberbank.ditsib.transport.request.service.CarsharingJoinRequestService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@E2EController
public class CarsharingJoinRequestEngineerControllerImpl implements CarsharingJoinRequestEngineerController {
    
    private final CarsharingJoinRequestService joinRequestService;
    
    private final OrganizationService organizationService;
    
    @Override
    public List<GetCarsharingJoinRequestShortDTO> getDoneRequests() {
        return joinRequestService.getByStatus(CarsharingJoinRequestStatus.DONE);
    }
    
    @Override
    public List<GetCarsharingJoinRequestShortDTO> getAwaitingRequests() {
        return joinRequestService.getByStatus(CarsharingJoinRequestStatus.UNDER_CONSIDERATION);
    }
    
    @Override
    public List<GetCarsharingJoinRequestShortDTO> getCancelledRequests() {
        return joinRequestService.getByStatus(CarsharingJoinRequestStatus.CANCELLED);
    }
    
    @Override
    public GetCarsharingJoinRequestDTO get(UUID joinRequestId) {
        return joinRequestService.getDtoById(joinRequestId);
    }
    
    @CheckOrganizationAccess
    @Override
    public GetCarsharingJoinRequestDTO process(
            Set<ProcessedContractorAndJoinStatusDTO> processedDtos,
            UUID joinRequestId,
            @ru.sber.transport.authorization.annotations.Organization UUID organizationId
                                              ) {
        Organization organization = organizationService.getOrganization(organizationId);
        return joinRequestService.process(processedDtos, joinRequestId, organization);
    }
}
