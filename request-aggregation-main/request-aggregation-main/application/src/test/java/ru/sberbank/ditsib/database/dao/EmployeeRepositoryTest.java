package ru.sberbank.ditsib.database.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.database.model.Employee;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@EmbeddedPostgres
@SpringBootTest
@Sql(scripts = "/scripts/basic_corp_structure.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("Тест репозитория EmployeeRepository")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void findByPersonnelNumber() {
        assertThat(employeeRepository.findByPersonnelNumber("3016497")).isPresent();
        assertThat(employeeRepository.findByPersonnelNumber("3016499")).isEmpty();
    }
}