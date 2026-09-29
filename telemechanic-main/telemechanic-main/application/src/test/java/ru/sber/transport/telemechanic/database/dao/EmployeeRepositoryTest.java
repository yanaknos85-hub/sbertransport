package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.EmployeeRepository;
import ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql(scripts = "/scripts/basic_corp_structure.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("Тест репозитория EmployeeRepository")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;


    @MethodSource
    @ParameterizedTest(name = "Поиск сотрудников подразделения {0} с табельным номером, содержащим {1}")
    void searchByDepartmentAndPersonnelNumber(UUID departmentId, String personnelNumberPattern, List<EmployeeInfoByDepartmentDto> expected) {
        var employeeInfos = employeeRepository.searchByDepartmentAndPersonnelNumber(departmentId, personnelNumberPattern);
        assertThat(employeeInfos).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> searchByDepartmentAndPersonnelNumber() {
        return Stream.of(Arguments.of(UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d9"),
                        "1016497",
                        emptyList()),
                Arguments.of(UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        "2016497",
                        List.of(new EmployeeInfoByDepartmentDto(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                                "2016497",
                                "Петров Петр",
                                "Планктон",
                                null))),
                Arguments.of(UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        "016497",
                        List.of(new EmployeeInfoByDepartmentDto(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                                        "2016497",
                                        "Петров Петр",
                                        "Планктон",
                                        null),
                                new EmployeeInfoByDepartmentDto(UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70"),
                                        "3016497",
                                        "Александров Александр Александрович",
                                        "Планктон",
                                        "123456488880"))));
    }
    
    @Test
    void findDigitIdByUserId() {
        assertThat(employeeRepository.findDigitIdByUserId(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"))).isEqualTo(8L);
        assertThat(employeeRepository.findDigitIdByUserId(UUID.randomUUID())).isNull();
    }
}