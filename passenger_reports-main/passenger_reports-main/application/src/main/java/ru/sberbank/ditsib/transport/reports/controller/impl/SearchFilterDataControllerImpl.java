package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sberbank.ditsib.transport.reports.controller.SearchFilterDataController;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentDTO;
import ru.sberbank.ditsib.transport.reports.dto.RequestStatusCodeListDTO;
import ru.sberbank.ditsib.transport.reports.model.Department;
import ru.sberbank.ditsib.transport.reports.model.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import java.util.List;
import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
@Slf4j
public class SearchFilterDataControllerImpl implements SearchFilterDataController {
    
    private final DepartmentService departmentService;
    
    @Override
    public List<String> getDepartmentList(
            DepartmentDTO departmentDTO,
            UUID organizationId,
            @E2EUser("principal") JwtAuthenticationToken authentication) {
    
        return departmentService.findDepartments(organizationId, departmentDTO).stream()
                                .map(Department::getDepartmentName)
                                .toList();
    }
    
    @Override
    public RequestStatusCodeListDTO getRequestStatusCodeList(TransportTypeEnum transportType, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return RequestServiceStaticHelper.getRequestStatusCodesByTransportType(transportType);
    }
}
