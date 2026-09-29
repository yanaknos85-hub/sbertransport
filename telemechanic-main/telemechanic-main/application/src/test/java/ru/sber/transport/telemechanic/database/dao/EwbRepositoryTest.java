package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.*;

@Transactional
@SpringBootTest
@EmbeddedPostgres
class EwbRepositoryTest {
    
    @Autowired
    private EwbRepository ewbRepository;
    
    @Test
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void shouldReturnExistsByTransportIdAndStartDateAndStatusIn() {
        var transportId = UUID.fromString("6a897664-9a0f-4e58-b3b7-d666579b37cb");
        var startDate = LocalDate.of(2023, 2, 10);
        var finishStatus = Set.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS, ON_THE_LINE);
        var driverId = UUID.fromString("167a0b4c-8324-44ba-9619-c0cd583fb1ca");
        
        assertTrue(ewbRepository.existsByTransportIdAndStartDateAndStatusIn(transportId, startDate, finishStatus));
        assertTrue(ewbRepository.existsByTransportIdAndStatusIn(transportId, finishStatus));
        assertTrue(ewbRepository.existsByDriverIdAndStartDateAndStatusIn(driverId, startDate, finishStatus));
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
    })
    void findCurrentOnTheLineByDriverId() {
        var driverId = UUID.fromString("167a0b4c-8324-44ba-9619-c0cd583fb1ca");
        var now = LocalDate.now();
        var actualOptional = ewbRepository.findCurrentOnTheLineByDriverId(driverId, now);
        assertTrue(actualOptional.isPresent());
        var actual = actualOptional.get();
        assertThat(actual.getStartDate())
                .isBeforeOrEqualTo(now);
        assertThat(actual.getFinishDate())
                .isAfterOrEqualTo(now);
        assertThat(actual.getStatus())
                .isEqualTo(ON_THE_LINE);
        assertThat(actual.getDriver().getId())
                .isEqualTo(driverId);
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
    })
    void findAllByStatusInAndFinishDate() {
        var statuses = Set.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS);
        var finishDate = LocalDate.of(2024, 7, 31);
        var result = ewbRepository.findAllByStatusInAndFinishDate(statuses, finishDate);
        
        assertThat(result).hasSize(2);
        var ewb = result.get(0);
        assertThat(ewb.getStatus()).isEqualTo(TELEMECH_IN_PROGRESS);
        assertThat(ewb.getFinishDate()).isEqualTo(finishDate);
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/have_active_ewb.sql"
    })
    void ewbExistsByTariffDepartmentsAndStatuses() {
        var startDate = LocalDate.of(2023, 2, 10);
        assertTrue(ewbRepository.ewbExistsByTariffDepartmentsAndStatuses(List.of(UUID.fromString("20d4a338-e121-4a0b-9d80-a7b6b035484f")),
                                                                         List.of(EWB_CREATED),
                                                                         startDate));
        assertFalse(ewbRepository.ewbExistsByTariffDepartmentsAndStatuses(List.of(UUID.fromString("d640b449-4482-4a48-bc05-2bbcfa66b46")),
                                                                          List.of(EWB_CREATED),
                                                                          startDate));
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
    })
    void setEwbListExpired() {
        var id1 = UUID.fromString("8800a83d-e9a1-4fc9-b85d-be1221fd294e");
        var id2 = UUID.fromString("29d4ef98-51fa-43b3-b3ed-a9870ad4b659");
        var expiredIds = List.of(id1, id2);
        
        ewbRepository.setEwbListExpired(expiredIds);
        
        var allEwbs = ewbRepository.findAll();
        var updatedEwbs = allEwbs.stream()
                                 .filter(ewb -> expiredIds.contains(ewb.getId()))
                                 .toList();
        var notUpdatedEwbs = allEwbs.stream()
                                    .filter(ewb -> !expiredIds.contains(ewb.getId()))
                                    .toList();
        
        assertThat(updatedEwbs)
                .hasSize(2)
                .allSatisfy(ewb -> {
                    assertThat(ewb.getStatus()).isEqualTo(EXPIRED);
                    assertThat(ewb.getEwbUuid()).isNotNull();
                    assertThat(ewb.getEwbUuid().toString())
                            .isNotIn("a60c8143-feb4-490d-b459-8ba7e0cd388f", "a4797f06-a46e-49a3-bf28-6a79b188216f");
                });
        
        assertThat(notUpdatedEwbs)
                .hasSize(5)
                .allSatisfy(ewb -> assertThat(ewb.getStatus()).isNotEqualTo(EXPIRED));
    }
}
