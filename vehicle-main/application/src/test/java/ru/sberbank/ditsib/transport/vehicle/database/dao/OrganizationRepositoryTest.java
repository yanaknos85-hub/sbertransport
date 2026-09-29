package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationNameWithDepartmentInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Sql("/scripts/basic_corp_structure.sql")
@Sql(value = "/scripts/truncate.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrganizationRepositoryTest {
    
    @Autowired
    private OrganizationRepository repository;
    
    @Test
    void findAllActiveOrganizations() {
        var expectedList = repository.findAll().stream()
                .filter(Organization::isActive)
                .sorted(Comparator.comparing(Organization::getOfficialName))
                .toList();
        
        var actualList = repository.findAllByActiveIsTrueOrderByOfficialName();
        
        assertThat(actualList).isEqualTo(expectedList);
    }
    
    @Test
    void findAllActiveOrganizationsById() {
        var ids = Set.of(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"));
        var expectedList = List.of(
                new OrganizationNameWithDepartmentInfo(
                        UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                        "Тест2",
                        UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                        "Test2",
                        null
                )
                                  );
        var actualList = repository.findByIdsWithActiveDepartments(ids);
        
        assertThat(actualList).isEqualTo(expectedList);
    }
}
