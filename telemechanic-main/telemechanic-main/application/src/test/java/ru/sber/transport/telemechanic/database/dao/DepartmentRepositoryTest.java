package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.DepartmentWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@EmbeddedPostgres
@SpringBootTest
@DisplayName("Тест репозитория DepartmentRepository")
class DepartmentRepositoryTest {
    
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    private Department savedDepartment1;
    private Department savedDepartment2;
    private Department savedDepartment3;
    private UUID organizationId;
    
    @BeforeEach
    void init() {
        var organization = organizationRepository.save(Organization.builder()
                                                                   .active(true)
                                                                   .id(UUID.randomUUID())
                                                                   .officialName("officialName")
                                                                   .msrn("msrn")
                                                                   .tin("tin")
                                                                   .build());
        var department1 = Department.builder()
                                    .id(UUID.randomUUID())
                                    .departmentName("departmentParentName1")
                                    .parentId(null)
                                    .active(true)
                                    .organization(organization)
                                    .humanReadableId("humanReadableId")
                                    .build();
        department1 = departmentRepository.save(department1);
        var department2 = Department.builder()
                                    .id(UUID.randomUUID())
                                    .departmentName("departmentParentName2")
                                    .parentId(null)
                                    .active(true)
                                    .organization(organization)
                                    .humanReadableId("humanReadableId")
                                    .build();
        department2 = departmentRepository.save(department2);
        savedDepartment1 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .departmentName("departmentName1")
                                     .parentId(department1.getId())
                                     .active(true)
                                     .organization(organization)
                                     .humanReadableId("humanReadableId")
                                     .build();
        savedDepartment1 = departmentRepository.save(savedDepartment1);
        savedDepartment2 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .departmentName("departmentName2")
                                     .parentId(department2.getId())
                                     .active(true)
                                     .organization(organization)
                                     .humanReadableId("humanReadableId")
                                     .build();
        savedDepartment2 = departmentRepository.save(savedDepartment2);
        
        var organization2 = organizationRepository.save(Organization.builder()
                                                                    .active(true)
                                                                    .id(UUID.randomUUID())
                                                                    .officialName("officialName")
                                                                    .msrn("msrn")
                                                                    .tin("tin")
                                                                    .build());
        
        organizationId = organization2.getId();
        savedDepartment3 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .departmentName("departmentName3")
                                     .parentId(null)
                                     .active(true)
                                     .organization(organization2)
                                     .humanReadableId("humanReadableId")
                                     .build();
    }
    
    @Test
    void findDepartmentChains() {
        var departmentIds = Set.of(savedDepartment1.getId(), savedDepartment2.getId());
        var actualList = departmentRepository.findDepartmentChains(departmentIds);
        assertEquals(2, actualList.size());
        var actual1 = actualList.stream()
                                .filter(departmentWithChain -> departmentWithChain.getId().equals(savedDepartment1.getId()))
                                .findFirst()
                                .orElseThrow();
        var actual2 = actualList.stream()
                                .filter(departmentWithChain -> departmentWithChain.getId().equals(savedDepartment2.getId()))
                                .findFirst()
                                .orElseThrow();
        assertEquals(savedDepartment1.getId(), actual1.getId());
        assertEquals("officialName;departmentParentName1;departmentName1", actual1.getChain());
        assertEquals(savedDepartment2.getId(), actual2.getId());
        assertEquals("officialName;departmentParentName2;departmentName2", actual2.getChain());
    }
    
    @Test
    void findNotOrganizationIds() {
        var actual = departmentRepository.findNotOrganizationIds(organizationId, Set.of(savedDepartment1.getId(), savedDepartment3.getId()));
        assertThat(actual)
                .hasSize(1)
                .containsAnyElementsOf(Set.of(savedDepartment1.getId()));
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql({ "/scripts/basic_corp_structure.sql",
           "/scripts/organization_medical_license.sql",
           "/scripts/ewb_contract.sql",
           "/scripts/fleet_owner_organization.sql",
           "/scripts/ewb_tariff.sql"
    })
    void findAllByIdWithDepartmentWithTariff() {
        var departments = departmentRepository.findAllByOrganizationIdWithActiveTariffWithChildren(
                UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396")
                                                                                                  );
        assertThat(departments).isNotEmpty()
                               .hasSize(2)
                               .containsExactlyInAnyOrder(
                new TariffDepartmentResponse(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                                             "Test2",
                                             null),
                new TariffDepartmentResponse(UUID.fromString("c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d"),
                                             "Test_Child_1",
                                             UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4")
                                                         ));
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql({ "/scripts/basic_corp_structure.sql"})
    void findOrganiztionWithAutoparkTest() {
        var departments = departmentRepository.findOrganiztionWithAutopark(
                UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6")
        );
        assertThat(departments).isNotEmpty()
                               .hasSize(1)
                               .containsExactlyInAnyOrder(
                                       new DepartmentWithAutoparkDto(UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"), "Fortest"
                                    )
                                );
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql({ "/scripts/basic_corp_structure.sql"})
    void nofindOrganiztionWithAutoparkTest() {
        var departments = departmentRepository.findOrganiztionWithAutopark(
                UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad010")
        );
        
        assertThat(departments).isEmpty();
    }
    
}
