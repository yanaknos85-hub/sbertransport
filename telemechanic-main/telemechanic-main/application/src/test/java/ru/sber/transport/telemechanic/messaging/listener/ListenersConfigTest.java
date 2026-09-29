package ru.sber.transport.telemechanic.messaging.listener;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.telemechanic.FirstTitleCreateRequestMessage;
import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;
import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;
import ru.sber.transport.telemechanic.messaging.sender.message.TransportMessage;
import ru.sber.transport.telemechanic.provider.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
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
    private TransportProvider transportProvider;
    @Mock
    private DispatcherProvider dispatcherProvider;
    @Mock
    private DriverProvider driverProvider;
    @Mock
    private EwbProvider ewbProvider;
    
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
    void deleteTransport() {
        doNothing().when(transportProvider).delete(any());
        
        var transportMessage = new TransportMessage(UUID.randomUUID(), "", "", "", "", 20, "",
                                                    20, "-", "-", List.of(UUID.randomUUID()), 40, true, null, null);
        listenersConfig.transportInput(transportProvider).accept(new GenericMessage<>(transportMessage));
        listenersConfig.transportInputSsl(transportProvider).accept(new GenericMessage<>(transportMessage));
        
        verify(transportProvider, times(2)).delete(any());
        verify(transportProvider, never()).save(any());
    }
    
    @Test
    void saveTransport() {
        doNothing().when(transportProvider).save(any());
        
        var transportMessage = new TransportMessage(UUID.randomUUID(), "", "", "", "", 20, "",
                                                    20, "-", "-", List.of(UUID.randomUUID()), 40, false, null, null);
        listenersConfig.transportInput(transportProvider).accept(new GenericMessage<>(transportMessage));
        listenersConfig.transportInputSsl(transportProvider).accept(new GenericMessage<>(transportMessage));
        
        verify(transportProvider, times(2)).save(any());
        verify(transportProvider, never()).delete(any());
    }
    
    @Test
    void saveDispatcher() {
        doNothing().when(dispatcherProvider).save(any());
        var dispatcherMessage = Instancio.of(DispatcherMessage.class)
                                         .set(field(DispatcherMessage::id), UUID.randomUUID())
                                         .create();
        listenersConfig.dispatcherInput(dispatcherProvider).accept(new GenericMessage<>(dispatcherMessage));
        listenersConfig.dispatcherInputSsl(dispatcherProvider).accept(new GenericMessage<>(dispatcherMessage));
        
        verify(dispatcherProvider, times(2)).save(any());
    }
    
    @Test
    void saveDriver() {
        doNothing().when(driverProvider).save(any());
        var driverMessage = Instancio.of(DriverMessage.class)
                                         .set(field(DriverMessage::id), UUID.randomUUID())
                                         .create();
        listenersConfig.driverInput(driverProvider).accept(new GenericMessage<>(driverMessage));
        listenersConfig.driverInputSsl(driverProvider).accept(new GenericMessage<>(driverMessage));
        
        verify(driverProvider, times(2)).save(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testAutoparkInput(boolean isDeleted) {
        if (isDeleted) {
            doNothing().when(departmentProvider).deleteAutopark(any());
        } else {
            doNothing().when(departmentProvider).addAutopark(any());
        }

        var autoparkMessage = Instancio.of(AutoparkMessage.class)
                .set(field(AutoparkMessage::deleted), isDeleted)
                .set(field(AutoparkMessage::routingId), UUID.randomUUID())
                .create();
        listenersConfig.autoparkInput(departmentProvider).accept(new GenericMessage<>(autoparkMessage));
        listenersConfig.autoparkInputSsl(departmentProvider).accept(new GenericMessage<>(autoparkMessage));

        if (isDeleted) {
            verify(departmentProvider, times(2)).deleteAutopark(any());
            verify(departmentProvider, never()).addAutopark(any());
        } else {
            verify(departmentProvider, times(2)).addAutopark(any());
            verify(departmentProvider, never()).deleteAutopark(any());
        }
    }

    @ParameterizedTest
    @CsvSource({
        "INTERNAL_AUTO_PARK, true",
        "INTERNAL_AUTO_PARK, false",
        "OTHER_SERVICE_TYPE, true",
        "OTHER_SERVICE_TYPE, false"
    })
    void testContractorInput(String serviceType, boolean deleted) {
        boolean isInternalAutoPark = "INTERNAL_AUTO_PARK".equals(serviceType);
        
        if (isInternalAutoPark && deleted) {
            doNothing().when(organizationProvider).deleteContractor(any());
        } else if (isInternalAutoPark){
            doNothing().when(organizationProvider).addContractor(any());
        }
        
        var contractorMessage = Instancio.of(ContractorMessage.class)
                .set(field(ContractorMessage::deleted), deleted)
                .set(field(ContractorMessage::serviceType), serviceType)
                .create();
        listenersConfig.contractorInput(organizationProvider).accept(new GenericMessage<>(contractorMessage));
        listenersConfig.contractorInputSsl(organizationProvider).accept(new GenericMessage<>(contractorMessage));
        
        if (isInternalAutoPark) {
            if (deleted) {
                verify(organizationProvider, times(2)).deleteContractor(any());
                verify(organizationProvider, never()).addContractor(any());
            } else {
                verify(organizationProvider, times(2)).addContractor(any());
                verify(organizationProvider, never()).deleteContractor(any());
            }
        } else {
            verify(organizationProvider, never()).deleteContractor(any());
            verify(organizationProvider, never()).addContractor(any());
        }
    }
    
    @Test
    void firstTitleCreate() {
        doNothing().when(ewbProvider).save(any());
        listenersConfig.firstTitleCreateInput(ewbProvider).accept(new GenericMessage<>(Instancio.create(FirstTitleCreateRequestMessage.class)));
        listenersConfig.firstTitleCreateInputSsl(ewbProvider).accept(new GenericMessage<>(Instancio.create(FirstTitleCreateRequestMessage.class)));
        verify(ewbProvider, times(2)).save(any());
    }
}