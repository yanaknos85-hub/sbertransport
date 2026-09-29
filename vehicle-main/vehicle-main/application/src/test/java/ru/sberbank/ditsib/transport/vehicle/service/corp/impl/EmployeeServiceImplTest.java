package ru.sberbank.ditsib.transport.vehicle.service.corp.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.EmployeeDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.EmployeeMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    
    private final ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
    
    @Test
    void saveOrUpdate() {
        var employee = Instancio.create(Employee.class);
        var returnedEmployee = Instancio.create(Employee.class);
        returnedEmployee = returnedEmployee.setFirstName("Ivan")
                                                   .setLastName("Ivanov")
                                                           .setPatronymic("Ivanovich");
        doReturn(Optional.of(returnedEmployee)).when(employeeRepository).findByUserId(any(UUID.class));
        employeeService.saveOrUpdate(employee);
        verify(employeeRepository, times(1)).save(employeeCaptor.capture());
        var actual = employeeCaptor.getValue();
        
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPatronymic(), actual.getPatronymic());
        assertEquals(returnedEmployee.getFirstName(), actual.getFirstName());
        assertEquals(returnedEmployee.getLastName(), actual.getLastName());
        assertEquals(returnedEmployee.getPatronymic(), actual.getPatronymic());
        
        doReturn(Optional.empty()).when(employeeRepository).findByUserId(any(UUID.class));
        employeeService.saveOrUpdate(employee);
        verify(employeeRepository, times(2)).save(employeeCaptor.capture());
        actual = employeeCaptor.getValue();
        
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPatronymic(), actual.getPatronymic());
    }
    
    @Test
    void getByPersonnelNumber() {
        var expected = Employee.builder()
                               .id(UUID.randomUUID())
                               .personnelNumber("111000111")
                               .firstName("Иван")
                               .lastName("Иванов")
                               .patronymic("Иванович")
                               .organization(Organization.builder()
                                                         .id(UUID.randomUUID())
                                                         .officialName("SOME ORGANIZATION")
                                                         .build())
                               .department(Department.builder()
                                                     .id(UUID.randomUUID())
                                                     .departmentName("SOME DEPARTMENT")
                                                     .build())
                               .build();
        var employeeDto = new EmployeeDto(expected.getId(), expected.getPersonnelNumber(), expected.getFIO(), expected.getOrganization().getId(),
                                          expected.getOrganization().getOfficialName(), expected.getDepartment().getId(),
                                          expected.getDepartment().getDepartmentName());
        var userId = UUID.randomUUID();
        var personnelNumber = "111000111";
    
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(expected));
        when(employeeRepository.findByPersonnelNumber(personnelNumber)).thenReturn(Optional.of(expected));
        when(employeeMapper.employeeToEmployeeDto(expected)).thenReturn(employeeDto);
        
        var actual = employeeService.getByPersonnelNumber(personnelNumber, userId);
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getPersonnelNumber(), actual.personnelNumber());
        assertEquals(expected.getFIO(), actual.fullName());
        assertEquals(expected.getOrganization().getId(), actual.organizationId());
        assertEquals(expected.getOrganization().getOfficialName(), actual.organizationName());
        assertEquals(expected.getDepartment().getId(), actual.departmentId());
        assertEquals(expected.getDepartment().getDepartmentName(), actual.departmentName());
    }
}