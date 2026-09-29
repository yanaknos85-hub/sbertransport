package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.vehicle.database.model.Attorney;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка мэппера МЧД")
class AttorneyMapperTest {

    private final AttorneyMapper mapper = new AttorneyMapperImpl(Mappers.getMapper(EmployeeMapper.class));

    @Test
    void testMapping() {
        var entity = Attorney.builder()
                .attorneyId(UUID.randomUUID())
                .expiryDate(LocalDateTime.now())
                .issueDate(LocalDateTime.now())
                .creationSystem("Система")
                .telemechanic(Employee.builder().id(UUID.randomUUID())
                        .firstName("Тест")
                        .lastName("Мэппер")
                        .patronymic(null)
                        .personnelNumber("2016498")
                        .organization(Organization.builder()
                                .id(UUID.randomUUID())
                                .officialName("ЦА")
                                .build())
                        .department(Department.builder()
                                .id(UUID.randomUUID())
                                .departmentName("Департамент ЦА")
                                .build())
                        .build())
                .build();
        var actual = mapper.mapAttorneyToAttorneyDto(entity);

        assertThat(entity.getId()).isEqualTo(actual.id());
        assertThat(entity.getAttorneyId()).isEqualTo(actual.attorneyId());
        assertThat(entity.getExpiryDate()).isEqualTo(actual.expiryDate());
        assertThat(entity.getIssueDate()).isEqualTo(actual.issueDate());
        assertThat(entity.getCreationSystem()).isEqualTo(actual.creationSystem());
        assertThat(entity.getTelemechanic().getId()).isEqualTo(actual.telemechanic().id());

    }
}
