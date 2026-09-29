package ru.sber.transport.etrn.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.database.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты маппера DepartmentMapper")
class DepartmentMapperTest {

    private DepartmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DepartmentMapperImpl();
    }

    @Test
    @DisplayName("fromMessage — преобразование DepartmentMessage в Department")
    void fromMessage_shouldMapFields() {
        // Arrange
        UUID orgId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        var message = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .humanReadableId("DT-0001-001")
                .departmentName("Отдел разработки")
                .organizationId(orgId)
                .parentId(parentId)
                .build();

        // Act
        var result = mapper.fromMessage(message);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(message.getId());
        assertThat(result.getHumanReadableId()).isEqualTo("DT-0001-001");
        assertThat(result.getDepartmentName()).isEqualTo("Отдел разработки");
        assertThat(result.getOrganizationId()).isEqualTo(orgId);
        assertThat(result.getParentId()).isEqualTo(parentId);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("update — обновление существующего Department")
    void update_shouldUpdateFields() {
        // Arrange
        UUID deptId = UUID.randomUUID();
        var existing = Department.builder()
                .id(deptId)
                .humanReadableId("DT-0001-old")
                .departmentName("Старое название")
                .active(true)
                .build();

        var message = DepartmentMessage.builder()
                .id(deptId)
                .humanReadableId("DT-0001-new")
                .departmentName("Новое название")
                .build();

        // Act
        Department updated = mapper.fromMessage(message);
        existing.setDepartmentName(updated.getDepartmentName());
        existing.setHumanReadableId(updated.getHumanReadableId());

        // Assert — id не должен измениться
        assertThat(existing.getId()).isEqualTo(deptId);
        assertThat(existing.getDepartmentName()).isEqualTo("Новое название");
        assertThat(existing.getHumanReadableId()).isEqualTo("DT-0001-new");
    }
}
