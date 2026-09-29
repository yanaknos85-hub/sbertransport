package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto;
import ru.sber.transport.telemechanic.dto.department.SearchEmployeesInfoByDepartmentRequest;
import ru.sber.transport.telemechanic.dto.medic.GetMedicDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with employees.
 */
public interface EmployeeService {
    
    /**
     * Get employee.
     *
     * @param id ID of employee.
     *
     * @return employee.
     */
    Optional<Employee> get(UUID id);
    
    /**
     * Delete employee.
     *
     * @param entity employee to delete.
     */
    void delete(Employee entity);
    
    /**
     * Save employee.
     *
     * @param entity employee to save.
     */
    Employee save(Employee entity);
    
    /**
     * Get employee by user ID.
     *
     * @param id ID of user.
     *
     * @return employee.
     */
    Employee getByUserId(UUID id);
    
    List<GetMedicDto> getMedicByFIO(String fio);
    
    /**
     * Search employees info by department id and personnel number
     *
     * @param request with department ID and personnel number
     *
     * @return {@link List<EmployeeInfoByDepartmentDto>}
     */
    List<EmployeeInfoByDepartmentDto> searchEmployeeInfoByDepartment(SearchEmployeesInfoByDepartmentRequest request);
    
    /**
     * Получаем уникальный идентификатор (числовой) по идентификатору записи пользователя с таблицы corporate.user
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return Уникальный идентификатор (числовой)
     */
    Long getDigitIdByUserId(UUID userId);
}
