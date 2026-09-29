package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.TransportController;
import ru.sber.transport.telemechanic.dto.transport.GetTransportRequest;
import ru.sber.transport.telemechanic.dto.transport.GetTransportResponse;
import ru.sber.transport.telemechanic.dto.transport.TransportSearchDto;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.TransportService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

@RestController
@E2EController
@RequiredArgsConstructor
public class TransportControllerImpl implements TransportController {
    
    private final TransportService transportService;
    
    @Override
    public Page<GetTransportResponse> getTransportByStateNumber(GetTransportRequest request, @E2EUser("principal") Authentication authentication) {
        return getTransportByStateNumberSelfOrganization(request, authentication);
    }
    
    @Override
    public Page<GetTransportResponse> getTransportByStateNumberSelfOrganization(
            GetTransportRequest request, @E2EUser("principal") Authentication authentication
                                                                               ) {
        var employeeId = UserAuthorizationHelper.getUserId(authentication);
        return transportService.getTransportByStateNumberSelfOrganization(request, employeeId);
    }
    
    @Override
    public Page<GetTransportResponse> getTransportByStateNumberAllOrganizations(GetTransportRequest request) {
        return transportService.getTransportByStateNumberAllOrganizations(request);
    }
    
    @Override
    public Page<GetTransportResponse> getTransport(TransportSearchDto searchDto, Authentication authentication) {
        return transportService.getTransportByFilters(searchDto, authentication);
    }
}
