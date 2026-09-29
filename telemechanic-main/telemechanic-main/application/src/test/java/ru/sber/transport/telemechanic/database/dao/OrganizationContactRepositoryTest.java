package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.OrganizationContactRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql"
})
class OrganizationContactRepositoryTest {
    
    @Autowired
    private OrganizationContactRepository organizationContactRepository;
    
    @Test
    @Transactional
    void deleteAllByOrganizationContactKeyOrganizationId() {
        assertThat(organizationContactRepository.findAll()).hasSize(2);
        organizationContactRepository.deleteAllByOrganizationContactKeyOrganizationId(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"));
        assertThat(organizationContactRepository.findAll()).isEmpty();
    }
}