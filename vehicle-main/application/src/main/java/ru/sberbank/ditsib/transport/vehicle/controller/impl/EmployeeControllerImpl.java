package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.vehicle.controller.EmployeeController;
import ru.sberbank.ditsib.transport.vehicle.dto.EmployeeDto;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;

@RestController
@RequiredArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {

    private final EmployeeService employeeService;

    @Override
    public EmployeeDto getEmployeeByPersonnelNumber(String personnelNumber, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return employeeService.getByPersonnelNumber(personnelNumber, userId);
    }
}
