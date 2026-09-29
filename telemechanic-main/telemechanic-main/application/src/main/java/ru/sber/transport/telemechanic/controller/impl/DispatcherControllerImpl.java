package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.DispatcherController;
import ru.sber.transport.telemechanic.dto.dispatcher.*;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.DispatcherService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class DispatcherControllerImpl implements DispatcherController {
    
    private final DispatcherService dispatcherService;
    
    @Override
    public void addDispatcher(AddDispatcherRequest request) {
        dispatcherService.addDispatcher(request);
    }
    
    @Override
    public void editDispatcher(UUID dispatcherId, EditDispatcherRequest request) {
        dispatcherService.editDispatcher(dispatcherId, request);
    }
    
    @Override
    public GetDispatcherResponse getDispatcher(UUID dispatcherId) {
        return dispatcherService.getDispatcher(dispatcherId);
    }
    
    @Override
    public void deactivateDispatcher(UUID dispatcherId) {
        dispatcherService.deactivateDispatcher(dispatcherId);
    }
    
    @Override
    public Page<GetDispatcherResponse> search(SearchDispatcherRequest request) {
        return dispatcherService.search(request);
    }

    @Override
    public GetOrganizationDispatcherResponse getSelfOrganizationInfo(Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return dispatcherService.getSelfOrganizationInfo(userId);
    }
}
