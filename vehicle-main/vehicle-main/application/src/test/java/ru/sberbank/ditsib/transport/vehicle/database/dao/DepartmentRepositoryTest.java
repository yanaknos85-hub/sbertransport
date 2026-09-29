package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Sql("/scripts/basic_corp_structure.sql")
@Sql(value = "/scripts/truncate.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository repository;

    @Test
    void getParentDepartments_withNullParent_returnsSelf() {
        var departmentId = UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8");

        var result = repository.getParentDepartments(departmentId);

        assertThat(result).containsExactly(departmentId);
    }

    @Test
    void getParentDepartments_withHierarchicalParents_returnsAllAncestors() {
        var childId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        var parentId = UUID.fromString("a0000000-0000-0000-0000-000000000002");
        var grandparentId = UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8");

        // child -> parent -> grandparent (root)
        repository.save(createDepartment(parentId, grandparentId, UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"), "Parent"));
        repository.save(createDepartment(childId, parentId, UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"), "Child"));

        var result = repository.getParentDepartments(childId);

        assertThat(result)
                .containsExactlyInAnyOrder(childId, parentId, grandparentId);
    }

    @Test
    void findDepOrgPairsByIds_returnsCorrectPairs() {
        var depId1 = UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8");
        var orgId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
        var depId2 = UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4");
        var orgId2 = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");

        var result = repository.findDepOrgPairsByIds(List.of(depId1, depId2));

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new Object[]{depId1, orgId1},
                        new Object[]{depId2, orgId2}
                );
    }

    @Test
    void findDepOrgPairsByIds_emptyList_returnsEmpty() {
        var result = repository.findDepOrgPairsByIds(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void findDepOrgPairsByIds_unknownIds_returnsEmpty() {
        var result = repository.findDepOrgPairsByIds(List.of(UUID.randomUUID()));

        assertThat(result).isEmpty();
    }

    @Test
    void getParentDepartments_unknownId_returnsEmpty() {
        var result = repository.getParentDepartments(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    private Department createDepartment(UUID id, UUID parentId, UUID orgId, String name) {
        var org = createOrganization(orgId);
        var builder = Department.builder()
                .id(id)
                .humanReadableId("HR-" + id)
                .organization(org)
                .departmentName(name)
                .active(true);
        if (parentId != null) {
            var parent = createDepartment(parentId, null, orgId, "ParentDummy");
            builder.parent(parent);
        }
        return builder.build();
    }

    private Organization createOrganization(UUID orgId) {
        var org = new Organization();
        org.setId(orgId);
        return org;
    }
}
