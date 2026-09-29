package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.OrganizationMedicalLicenseRepository;
import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
class OrganizationMedicalLicenseRepositoryTest {
    
    @Autowired
    private OrganizationMedicalLicenseRepository organizationMedicalLicenseRepository;
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql"
    })
    void existsAndActive() {
        assertThat(organizationMedicalLicenseRepository.existsAndActive(UUID.fromString("d8586e9a-a50c-4c6f-a581-e01ef501e983"))).isTrue();
        assertThat(organizationMedicalLicenseRepository.existsAndActive(UUID.randomUUID())).isFalse();
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql"
    })
    void findByIdAndActiveIsTrue() {
        var id = UUID.fromString("d8586e9a-a50c-4c6f-a581-e01ef501e983");
        assertThat(organizationMedicalLicenseRepository.findByIdAndActiveIsTrue(id))
                .isEqualTo(Optional.of(new OrganizationMedicalLicense(id,
                                                                      "55555",
                                                                      "666666",
                                                                      LocalDate.now(),
                                                                      LocalDate.now().plusYears(1),
                                                                      true)));
        assertThat(organizationMedicalLicenseRepository.findByIdAndActiveIsTrue(UUID.randomUUID())).isNotPresent();
    }
}