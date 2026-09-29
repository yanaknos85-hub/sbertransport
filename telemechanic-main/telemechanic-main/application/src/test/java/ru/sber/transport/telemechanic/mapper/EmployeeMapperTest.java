package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sber.transport.telemechanic.database.model.Employee;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тест маппера сотрудника")
class EmployeeMapperTest {
    private final EmployeeMapper mapper = Mappers.getMapper(EmployeeMapper.class);
    
    @Test
    void employeeMessageToEmployee() {
        var message = Instancio.create(EmployeeMessage.class);
        var actual = mapper.employeeMessageToEmployee(message);
        
        assertNotNull(actual);
        assertEquals(message.getId(), actual.getId());
        assertEquals(message.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.getFirstName(), actual.getFirstName());
        assertEquals(message.getLastName(), actual.getLastName());
        assertEquals(message.getPatronymic(), actual.getPatronymic());
        assertEquals(message.getUserId(), actual.getUserId());
        assertEquals(message.getPersonnelNumber(), actual.getPersonnelNumber());
        assertEquals(message.getDepartmentId(), actual.getDepartment().getId());
        assertEquals(message.getPositionId(), actual.getPosition().getId());
        assertEquals(message.getMobilePhone(), actual.getMobilePhone());
        assertTrue(actual.isActive());
        assertEquals(message.getOrganizationId(), actual.getOrganization().getId());
    }
    
    @Test
    void employeeToEmployeeDto() {
        var employee = Instancio.create(Employee.class);
        var actual = mapper.employeeToEmployeeDto(employee);
        
        assertNotNull(actual);
        assertEquals(actual.id(), employee.getId());
        assertEquals(actual.firstName(), employee.getFirstName());
        assertEquals(actual.lastName(), employee.getLastName());
        assertEquals(actual.patronymic(), employee.getPatronymic());
        assertEquals(actual.personnelNumber(), employee.getPersonnelNumber());
    }
}