package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.FleetOwnerOrganizationRepository;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql({ "/scripts/basic_corp_structure.sql",
       "/scripts/fleet_owner_organization.sql" })
class FleetOwnerOrganizationRepositoryTest {
    
    @Autowired
    private FleetOwnerOrganizationRepository fleetOwnerOrganizationRepository;
    
    @Test
    void findAllActive() {
        var expected1 = new GetAllActiveOrganizationNamesDto(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"), "Тест2");
        var expected2 = new GetAllActiveOrganizationNamesDto(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"), "ЦА");
        var expected3 = new GetAllActiveOrganizationNamesDto(UUID.fromString("11501599-b498-4e9c-8b75-3e5899c445c0"), "Тест3");
        assertThat(fleetOwnerOrganizationRepository.findAllActive()).containsExactlyInAnyOrder(expected1, expected2, expected3);
    }
}
