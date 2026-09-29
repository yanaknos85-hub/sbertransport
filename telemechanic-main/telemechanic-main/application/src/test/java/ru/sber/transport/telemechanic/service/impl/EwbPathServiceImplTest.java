package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.EwbTariffNotFoundException;
import ru.sber.transport.telemechanic.service.EwbTariffService;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EwbPathServiceImplTest {
    
    @Mock
    private EwbTariffService ewbTariffService;
    @InjectMocks
    private EwbPathServiceImpl ewbPathService;
    
    @Test
    void calculatingClientPath() {
        var driver = Instancio.create(Employee.class);
        
        doReturn(Set.of(InspectionType.MEDIC, InspectionType.TECHNIC)).when(ewbTariffService).getInspectionTypesByDriverDepartmentId(any(UUID.class));
        
        assertThat(ewbPathService.calculatingClientPath(driver)).isTrue();
        
        doReturn(Set.of(InspectionType.TECHNIC)).when(ewbTariffService).getInspectionTypesByDriverDepartmentId(any(UUID.class));
        
        assertThat(ewbPathService.calculatingClientPath(driver)).isFalse();
        
        doReturn(Collections.emptySet()).when(ewbTariffService).getInspectionTypesByDriverDepartmentId(any(UUID.class));
        assertThatExceptionOfType(EwbTariffNotFoundException.class)
                  .isThrownBy(() -> ewbPathService.calculatingClientPath(driver))
                  .withMessage("Не найден активный тариф для организации, id:%s".formatted(driver.getOrganization().getId()));
    }
    
    @Test
    void calculatingTelemedicClientPath() {
        var driver = Instancio.create(Employee.class);
        
        doReturn(Set.of(InspectionType.TELEMEDIC, InspectionType.TECHNIC)).when(ewbTariffService).getInspectionTypesByDriverDepartmentId(any(UUID.class));
        
        assertThat(ewbPathService.calculatingClientPath(driver)).isTrue();
    }
}