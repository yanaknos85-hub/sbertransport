package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.ReportController;
import ru.sber.transport.telemechanic.dto.RegistryDto;
import ru.sber.transport.telemechanic.dto.ReportSearchDto;
import ru.sber.transport.telemechanic.helper.CheckRoleHelper;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.mapper.ReportMapper;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.RequestService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

@RequiredArgsConstructor
@RestController
@E2EController
public class ReportControllerImpl implements ReportController {

    private final EmployeeService employeeService;
    private final RequestService requestService;
    private final ReportMapper reportMapper;
    @Override
    @Transactional
    public Page<RegistryDto> getRegistry(ReportSearchDto reportSearchDto, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var roles = UserAuthorizationHelper.getRoles(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        if (!CheckRoleHelper.checkHaveAllOrganizationsRoleForReport(roles)) {
            reportSearchDto.setOrganizationId(authenticatedEmployee.getDepartment().getOrganization().getId());
        }
        var requestPage = requestService.search(reportSearchDto);
        return new PageImpl<>(
                requestPage.stream()
                        .map(reportMapper::requestToRegistryDto)
                        .toList(),
                requestPage.getPageable(),
                requestPage.getTotalElements());
    }
}
