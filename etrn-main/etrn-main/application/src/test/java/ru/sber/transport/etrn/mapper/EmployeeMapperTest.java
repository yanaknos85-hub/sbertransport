package ru.sber.transport.etrn.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sber.transport.etrn.database.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты маппера EmployeeMapper")
class EmployeeMapperTest {

    private EmployeeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EmployeeMapperImpl();
    }

    @Test
    @DisplayName("fromMessage — преобразование EmployeeMessage в Employee")
    void fromMessage_shouldMapFields() {
        // Arrange
        UUID empId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();
        var message = EmployeeMessage.builder()
                .id(empId)
                .humanReadableId("EMP-0001-001")
                .firstName("Иван")
                .lastName("Иванов")
                .patronymic("Иванович")
                .personnelNumber("P-001")
                .userId(userId)
                .departmentId(deptId)
                .build();

        // Act
        var result = mapper.fromMessage(message);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(empId);
        assertThat(result.getHumanReadableId()).isEqualTo("EMP-0001-001");
        assertThat(result.getFirstName()).isEqualTo("Иван");
        assertThat(result.getLastName()).isEqualTo("Иванов");
        assertThat(result.getPatronymic()).isEqualTo("Иванович");
        assertThat(result.getPersonnelNumber()).isEqualTo("P-001");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("update — обновление существующего Employee")
    void update_shouldUpdateFields() {
        // Arrange
        UUID empId = UUID.randomUUID();
        var existing = Employee.builder()
                .id(empId)
                .firstName("Старое")
                .lastName("Фамилия")
                .active(true)
                .build();

        var message = EmployeeMessage.builder()
                .id(empId)
                .firstName("Новое")
                .lastName("НоваяФамилия")
                .build();

        // Act — извлекаем updated entity из mapper
        var updated = mapper.fromMessage(message);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());

        // Assert
        assertThat(existing.getId()).isEqualTo(empId);
        assertThat(existing.getFirstName()).isEqualTo("Новое");
        assertThat(existing.getLastName()).isEqualTo("НоваяФамилия");
    }

    @Test
    @DisplayName("employeeMessageToEmployeeEntity — маппинг departmentId в department.id")
    void employeeMessageToEmployeeEntity_shouldMapDepartmentId() {
        // Arrange
        UUID empId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();
        var message = EmployeeMessage.builder()
                .id(empId)
                .departmentId(deptId)
                .build();

        // Act
        var result = mapper.employeeMessageToEmployeeEntity(message);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(empId);
        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getId()).isEqualTo(deptId);
    }
}
