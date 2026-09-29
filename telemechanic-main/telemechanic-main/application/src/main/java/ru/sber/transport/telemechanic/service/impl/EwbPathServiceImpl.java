package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.EwbTariffNotFoundException;
import ru.sber.transport.telemechanic.service.EwbPathService;
import ru.sber.transport.telemechanic.service.EwbTariffService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EwbPathServiceImpl implements EwbPathService {
    
    private final EwbTariffService ewbTariffService;
    
    @Override
    public boolean calculatingClientPath(Employee driver) {
        var contractTypes = ewbTariffService.getInspectionTypesByDriverDepartmentId(driver.getDepartment().getId());
        if (contractTypes.isEmpty()) {
            throw new EwbTariffNotFoundException(driver.getOrganization().getId());
        }
        if (contractTypes.contains(InspectionType.TELEMEDIC)) {
            return contractTypes.containsAll(Set.of(InspectionType.TELEMEDIC, InspectionType.TECHNIC));
        }
        return contractTypes.containsAll(Set.of(InspectionType.MEDIC, InspectionType.TECHNIC));
    }
}
