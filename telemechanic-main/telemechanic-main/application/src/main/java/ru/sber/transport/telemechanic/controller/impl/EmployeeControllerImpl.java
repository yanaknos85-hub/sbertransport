package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.EmployeeController;
import ru.sber.transport.telemechanic.dto.medic.GetMedicDto;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.List;

@RestController
@E2EController
@RequiredArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {
    
    private final EmployeeService employeeService;
    
    @Override
    public List<GetMedicDto> getMedicByFIO(String fio) {
        return employeeService.getMedicByFIO(fio);
    }
}
