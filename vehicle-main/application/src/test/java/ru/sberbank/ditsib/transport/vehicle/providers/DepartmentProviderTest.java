package ru.sberbank.ditsib.transport.vehicle.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.mapper.DepartmentMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.impl.DepartmentProviderImpl;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера подразделений")
class DepartmentProviderTest {
    
    @InjectMocks
    private DepartmentProviderImpl provider;
    
    @Mock
    private DepartmentService service;
    
    @Mock
    private DepartmentMapper mapper;
    
    @Mock
    private OrganizationService organizationService;
    
    private Organization organization;
    private Department department;
    private DepartmentMessage message;
    
    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                                   .id(UUID.randomUUID())
                                   .digitId(1L)
                                   .officialName("officialName")
                                   .build();
        department = Department.builder()
                               .id(UUID.randomUUID())
                               .organization(organization)
                               .departmentName("departmentName")
                               .humanReadableId("humanReadableId")
                               .build();
        message = DepartmentMessage.builder()
                                   .id(department.getId())
                                   .departmentName(department.getDepartmentName())
                                   .organizationId(organization.getId())
                                   .location("location")
                                   .code("code")
                                   .humanReadableId(department.getHumanReadableId())
                                   .easupId("10278656")
                                   .build();
    }
    
    @Test
    void delete() {
        when(mapper.departmentMessageToDepartment(message)).thenReturn(department);
        doNothing().when(service).delete(department);
        provider.delete(message);
        verify(mapper).departmentMessageToDepartment(message);
        verify(service).delete(department);
    }
    
    @Test
    void save() {
        when(mapper.departmentMessageToDepartment(message)).thenReturn(department);
        when(service.save(department)).thenReturn(department);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        provider.save(message);
        verify(mapper, times(1)).departmentMessageToDepartment(message);
        verify(service, times(1)).save(department);
        verify(organizationService, times(1)).get(message.getOrganizationId());
    }
    
    @Test
    void saveNoOrganization() {
        var messageNoOrganization = DepartmentMessage.builder()
                                   .id(department.getId())
                                   .departmentName(department.getDepartmentName())
                                   .organizationId(null)
                                   .location("location")
                                   .code("code")
                                   .humanReadableId(department.getHumanReadableId())
                                   .build();
        when(mapper.departmentMessageToDepartment(messageNoOrganization)).thenReturn(department.toBuilder().organization(null).build());
        provider.save(messageNoOrganization);
        verify(mapper, times(1)).departmentMessageToDepartment(messageNoOrganization);
        verify(service, times(0)).save(department);
        verify(organizationService, times(0)).get(messageNoOrganization.getOrganizationId());
    }
    
    @Test
    void saveNoOrganizationInDatabase() {
        when(mapper.departmentMessageToDepartment(message)).thenReturn(department);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> provider.save(message))
                .isInstanceOf(EntityNotFoundException.class);
        verify(mapper, times(1)).departmentMessageToDepartment(message);
        verify(service, times(0)).save(department);
        verify(organizationService, times(1)).get(message.getOrganizationId());
    }
}