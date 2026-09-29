package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.EmployeeRepository;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.dto.department.SearchEmployeesInfoByDepartmentRequest;
import ru.sber.transport.telemechanic.dto.medic.GetMedicDto;
import ru.sber.transport.telemechanic.exception.DepartmentNotActiveException;
import ru.sber.transport.telemechanic.exception.DepartmentNotFoundException;
import ru.sber.transport.telemechanic.mapper.EmployeeMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeMapper employeeMapper;
    
    @Test
    void getMedicByFIO() {
        doReturn(List.of(Instancio.create(Employee.class))).when(employeeRepository).findEmployeesByFullNameIndexContainingIgnoreCase(anyString());
        doReturn(Instancio.create(GetMedicDto.class)).when(employeeMapper).employeeToGetMedicDto(any(Employee.class));
        var actual = employeeService.getMedicByFIO("fio");
        assertNotNull(actual);
    }
    
    @Test
    void searchEmployeeInfoByDepartment() {
        var request = Instancio.create(SearchEmployeesInfoByDepartmentRequest.class);
        
        doReturn(Optional.empty()).when(departmentService).get(any());
        assertThrows(DepartmentNotFoundException.class, () -> employeeService.searchEmployeeInfoByDepartment(request));
        
        var department = Instancio.create(Department.class);
        department.setActive(false);
        doReturn(Optional.of(department)).when(departmentService).get(any());
        assertThrows(DepartmentNotActiveException.class, () -> employeeService.searchEmployeeInfoByDepartment(request));
    }
}
