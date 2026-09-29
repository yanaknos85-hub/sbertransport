package ru.sber.transport.etrn.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import ru.sber.transport.etrn.database.dao.EmployeeRepository;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sber.transport.etrn.database.model.Department;
import ru.sber.transport.etrn.exceptions.UserNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID EMP_ID = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEPT_ID = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");

    @Mock
    private EmployeeRepository repository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployeeServiceImpl service;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Department department = Department.builder()
                .id(DEPT_ID)
                .departmentName("Отдел разработки")
                .build();

        employee = Employee.builder()
                .id(EMP_ID)
                .userId(USER_ID)
                .firstName("Иван")
                .lastName("Иванов")
                .patronymic("Иванович")
                .department(department)
                .build();
    }

    @Test
    @DisplayName("save — сохранение employee")
    void save_callsRepositorySave() {
        service.save(employee);
        verify(repository).save(employee);
    }

    @Test
    @DisplayName("delete — удаление employee")
    void delete_callsRepositoryDelete() {
        service.delete(employee);
        verify(repository).delete(employee);
    }

    @Test
    @DisplayName("findById — найден")
    void findById_found_returnsOptional() {
        when(repository.findById(EMP_ID)).thenReturn(Optional.of(employee));

        Optional<Employee> result = service.findById(EMP_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("findById — не найден")
    void findById_notFound_returnsEmpty() {
        when(repository.findById(EMP_ID)).thenReturn(Optional.empty());

        Optional<Employee> result = service.findById(EMP_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAuthenticatedEmployee — сотрудник найден")
    void getAuthenticatedEmployee_found_returnsEmployee() {
        when(authentication.getName()).thenReturn(USER_ID.toString());
        when(repository.findByUserIdWithDepartment(USER_ID)).thenReturn(Optional.of(employee));

        Employee result = service.getAuthenticatedEmployee(authentication);

        assertThat(result).isEqualTo(employee);
        assertThat(result.getFirstName()).isEqualTo("Иван");
        assertThat(result.getLastName()).isEqualTo("Иванов");
        assertThat(result.getPatronymic()).isEqualTo("Иванович");
        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getId()).isEqualTo(DEPT_ID);
    }

    @Test
    @DisplayName("getAuthenticatedEmployee — сотрудник не найден выбрасывает исключение")
    void getAuthenticatedEmployee_notFound_throwsException() {
        when(authentication.getName()).thenReturn(USER_ID.toString());
        when(repository.findByUserIdWithDepartment(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAuthenticatedEmployee(authentication))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(USER_ID.toString());
    }
}
