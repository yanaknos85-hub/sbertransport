package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.OrganizationDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
class OrganizationRepositoryTest {
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql("/scripts/basic_corp_structure.sql")
    void getFirstContactPhone() {
        var organizationId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
        var organizationId2 = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
        assertThat(organizationRepository.getFirstContactPhone(organizationId1))
                .isEqualTo("+79163313365");
        assertThat(organizationRepository.getFirstContactPhone(organizationId2))
                .isNull();
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql("/scripts/basic_corp_structure.sql")
    void findAllByActiveTrueOrderByOfficialName() {
        organizationRepository.save(createOrganization(false, null));
        var organizationId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
        var organizationId2 = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
        var organizationId3 = UUID.fromString("11501599-b498-4e9c-8b75-3e5899c445c0");
        assertThat(organizationRepository.findAll()).hasSize(4);
        assertThat(organizationRepository.findAllActiveOrderByOfficialName())
                .hasSize(3)
                .extracting(OrganizationDto::id)
                .contains(organizationId2, organizationId3, organizationId1);
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllWithInternalContractorsByActiveTrueOrderByOfficialName() {
        var organizationFail1 = createOrganization(true,  null);
        var organizationFail2 = createOrganization(false, UUID.randomUUID());
        var organizationSuccess = createOrganization(true, UUID.randomUUID());
        organizationSuccess.setId(UUID.randomUUID());
        
        organizationRepository.saveAll(List.of(organizationFail1, organizationFail2, organizationSuccess));
        
        assertThat(organizationRepository.findAll()).hasSize(3);
        assertThat(organizationRepository.findAllActiveWithInternalContractorOrderByOfficialName())
                .hasSize(1)
                .extracting(OrganizationDto::id)
                .contains(organizationSuccess.getId());
    }
    
    private static Organization createOrganization(boolean active, UUID contractorId) {
        return Organization.builder()
                .active(active)
                .id(UUID.randomUUID())
                .officialName("officialName")
                .msrn("msrn")
                .tin("tin")
                .contractorExternalId(contractorId)
                .build();
    }
}