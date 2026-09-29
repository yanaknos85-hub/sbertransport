package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.projection.EwbContractDetailsProjection;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@EmbeddedPostgres
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql",
        "/scripts/organization_medical_license.sql",
        "/scripts/ewb_contract.sql",
        "/scripts/fleet_owner_organization.sql",
        "/scripts/ewb_tariff.sql"
})
class EwbTariffRepositoryTest {
    
    @Autowired
    private EwbTariffRepository ewbTariffRepository;
    
    @Test
    void existsByOrganizationIdAndActiveTrue() {
        var randomOrganizationId = UUID.randomUUID();
        assertThat(ewbTariffRepository.existsByOrganizationIdAndActiveTrue(randomOrganizationId)).isFalse();
        var activeOrganizationId = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
        assertThat(ewbTariffRepository.existsByOrganizationIdAndActiveTrue(activeOrganizationId)).isTrue();
    }
    
    @Test
    void findContractOrganizationIdByTariffDepartmentIdWithRecursiveHierarchy() {
        var departmentId = UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8");
        var contractOrganizationId = ewbTariffRepository.findContractOrganizationIdByTariffDepartmentIdWithRecursiveHierarchy(departmentId);
        assertThat(contractOrganizationId).isEqualTo(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"));
    }
    
    @Test
    void findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy() {
        var departmentId = UUID.fromString("e4cdf1e8-45d6-4ecf-8330-6b10760d256d");
        var ewbContractDetails = ewbTariffRepository.findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(
                departmentId,
                Set.of(InspectionType.MEDIC.name(), InspectionType.TECHNIC.name())
                                                                                                                 );
        assertThat(ewbContractDetails)
                .extracting(EwbContractDetailsProjection::getInspectionType)
                .containsExactlyInAnyOrder(InspectionType.TECHNIC.name());
        
    }

    @Test
    void findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy() {
        var departmentId = UUID.fromString("e4cdf1e8-45d6-4ecf-8330-6b10760d256d");
        var actual = ewbTariffRepository.findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId);
        assertThat(actual)
                .hasSize(1)
                .extracting(
                        EwbContract::getContractId,
                        EwbContract::getOrganizationId,
                        EwbContract::getInspectionType,
                        EwbContract::getEdfOperatorId,
                        EwbContract::getEdfCode,
                        EwbContract::getOrganizationMedicalLicenseId,
                        EwbContract::isActive
                )
                .containsExactly(
                        tuple(
                                UUID.fromString("671eee2e-47a5-4f90-9311-06c82a1f1dd6"),
                                UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                                InspectionType.TECHNIC,
                                "2AE",
                                "77777",
                                null,
                                true
                        )
                );

    }
}
