package ru.sberbank.ditsib.transport.vehicle.messaging.listeners;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;
import ru.sberbank.ditsib.transport.vehicle.providers.DepartmentProvider;
import ru.sberbank.ditsib.transport.vehicle.providers.EmployeeProvider;
import ru.sberbank.ditsib.transport.vehicle.providers.OrganizationProvider;
import ru.sberbank.ditsib.transport.vehicle.providers.PositionProvider;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка конфигурации binding listeners")
class ListenersConfigTest {

    @InjectMocks
    private ListenersConfig listenersConfig;

    @Mock
    private OrganizationProvider organizationProvider;

    @Mock
    private DepartmentProvider departmentProvider;

    @Mock
    private PositionProvider positionProvider;

    @Mock
    private EmployeeProvider employeeProvider;
    
    @Mock
    private TransportService transportService;

    @Test
    void deleteOrganization() {
        doNothing().when(organizationProvider).delete(any());

        var organizationMessage = new OrganizationMessage();
        organizationMessage.setDeleted(true);
        listenersConfig.organizationsInput(organizationProvider).accept(new GenericMessage<>(organizationMessage));
        listenersConfig.organizationsInputDlq(organizationProvider).accept(new GenericMessage<>(organizationMessage));
        listenersConfig.organizationsInputSsl(organizationProvider).accept(new GenericMessage<>(organizationMessage));
        listenersConfig.organizationsInputDlqSsl(organizationProvider).accept(new GenericMessage<>(organizationMessage));

        verify(organizationProvider, times(4)).delete(any());
        verify(organizationProvider, never()).save(any());
    }

    @Test
    void saveOrganization() {
        doNothing().when(organizationProvider).save(any());

        var organizationMessage = new OrganizationMessage();
        organizationMessage.setDeleted(false);
        listenersConfig.organizationsInput(organizationProvider).accept(new GenericMessage<>(organizationMessage));
        listenersConfig.organizationsInputDlq(organizationProvider).accept(new GenericMessage<>(organizationMessage));
        listenersConfig.organizationsInputSsl(organizationProvider).accept(new GenericMessage<>(organizationMessage));
        listenersConfig.organizationsInputDlqSsl(organizationProvider).accept(new GenericMessage<>(organizationMessage));

        verify(organizationProvider, times(4)).save(any());
        verify(organizationProvider, never()).delete(any());
    }

    @Test
    void deleteDepartment() {
        doNothing().when(departmentProvider).delete(any());

        var departmentMessage = DepartmentMessage.builder()
                .deleted(true)
                .build();
        listenersConfig.departmentsInput(departmentProvider).accept(new GenericMessage<>(departmentMessage));
        listenersConfig.departmentsInputDlq(departmentProvider).accept(new GenericMessage<>(departmentMessage));
        listenersConfig.departmentsInputSsl(departmentProvider).accept(new GenericMessage<>(departmentMessage));
        listenersConfig.departmentsInputDlqSsl(departmentProvider).accept(new GenericMessage<>(departmentMessage));

        verify(departmentProvider, times(4)).delete(any());
        verify(departmentProvider, never()).save(any());
    }

    @Test
    void saveDepartment() {
        doNothing().when(departmentProvider).save(any());

        var departmentMessage = DepartmentMessage.builder()
                .deleted(false)
                .build();
        listenersConfig.departmentsInput(departmentProvider).accept(new GenericMessage<>(departmentMessage));
        listenersConfig.departmentsInputDlq(departmentProvider).accept(new GenericMessage<>(departmentMessage));
        listenersConfig.departmentsInputSsl(departmentProvider).accept(new GenericMessage<>(departmentMessage));
        listenersConfig.departmentsInputDlqSsl(departmentProvider).accept(new GenericMessage<>(departmentMessage));

        verify(departmentProvider, times(4)).save(any());
        verify(departmentProvider, never()).delete(any());
    }

    @Test
    void deletePosition() {
        doNothing().when(positionProvider).delete(any());

        var positionMessage = PositionMessage.builder()
                .deleted(true)
                .build();
        listenersConfig.positionsInput(positionProvider).accept(new GenericMessage<>(positionMessage));
        listenersConfig.positionsInputDlq(positionProvider).accept(new GenericMessage<>(positionMessage));
        listenersConfig.positionsInputSsl(positionProvider).accept(new GenericMessage<>(positionMessage));
        listenersConfig.positionsInputDlqSsl(positionProvider).accept(new GenericMessage<>(positionMessage));

        verify(positionProvider, times(4)).delete(any());
        verify(positionProvider, never()).save(any());
    }

    @Test
    void savePosition() {
        doNothing().when(positionProvider).save(any());

        var positionMessage = PositionMessage.builder()
                .deleted(false)
                .build();
        listenersConfig.positionsInput(positionProvider).accept(new GenericMessage<>(positionMessage));
        listenersConfig.positionsInputDlq(positionProvider).accept(new GenericMessage<>(positionMessage));
        listenersConfig.positionsInputSsl(positionProvider).accept(new GenericMessage<>(positionMessage));
        listenersConfig.positionsInputDlqSsl(positionProvider).accept(new GenericMessage<>(positionMessage));

        verify(positionProvider, times(4)).save(any());
        verify(positionProvider, never()).delete(any());
    }

    @Test
    void deleteEmployee() {
        doNothing().when(employeeProvider).delete(any());

        var employeeMessage = EmployeeMessage.builder()
                .deleted(true)
                .build();
        listenersConfig.employeesInput(employeeProvider).accept(new GenericMessage<>(employeeMessage));
        listenersConfig.employeesInputDlq(employeeProvider).accept(new GenericMessage<>(employeeMessage));
        listenersConfig.employeesInputSsl(employeeProvider).accept(new GenericMessage<>(employeeMessage));
        listenersConfig.employeesInputDlqSsl(employeeProvider).accept(new GenericMessage<>(employeeMessage));

        verify(employeeProvider, times(4)).delete(any());
        verify(employeeProvider, never()).save(any());
    }

    @Test
    void saveEmployee() {
        doNothing().when(employeeProvider).save(any());

        var employeeMessage = EmployeeMessage.builder()
                .deleted(false)
                .build();
        listenersConfig.employeesInput(employeeProvider).accept(new GenericMessage<>(employeeMessage));
        listenersConfig.employeesInputDlq(employeeProvider).accept(new GenericMessage<>(employeeMessage));
        listenersConfig.employeesInputSsl(employeeProvider).accept(new GenericMessage<>(employeeMessage));
        listenersConfig.employeesInputDlqSsl(employeeProvider).accept(new GenericMessage<>(employeeMessage));

        verify(employeeProvider, times(4)).save(any());
        verify(employeeProvider, never()).delete(any());
    }

    @Test
    void inputOdometerHistoryValue() {
        doNothing().when(transportService).addIndicatorsHistory(any());
        
        var odometerHistoryValueMessage = new OdometerHistoryValueMessage(UUID.randomUUID(), 100, UUID.randomUUID(), LocalDateTime.now(), Map.of("direction",
                                                                                                                                      "OUT"));
        listenersConfig.odometerHistoryValueInput(transportService).accept(new GenericMessage<>(odometerHistoryValueMessage));
        listenersConfig.odometerHistoryValueInputSsl(transportService).accept(new GenericMessage<>(odometerHistoryValueMessage));
        
        verify(transportService, times(2)).addIndicatorsHistory(any());
    }
}
