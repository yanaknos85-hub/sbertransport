package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchDto;
import ru.sber.transport.telemechanic.enumerate.MedicRequestSortOption;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.telemechanic.enumerate.MedicRequestField.*;

@SpringBootTest
@EmbeddedPostgres
class MedicRequestDynamicRepositoryTest {
    
    @Autowired
    private MedicRequestRegistryDynamicRepository medicRequestRegistryDynamicRepository;
    
    @Test
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void findMedicRequestRegistry() {
        var pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID.getSqlValue()));
        var fieldSet = Set.of(EWB_ID, EWB_UUID, EWB_HUMAN_READABLE_ID, EWB_MEDIC_DECISION_TIME, MEDIC_REQUEST_HUMAN_READABLE_ID,
                              MEDIC_REQUEST_SYSTOLIC_PRESSURE, MEDIC_REQUEST_DIASTOLIC_PRESSURE, MEDIC_REQUEST_PULSE, MEDIC_REQUEST_TEMPERATURE,
                              MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT, MEDIC_REQUEST_STATUS, MEDIC_FULL_NAME, MEDIC_PERSONNEL_NUMBER,
                              MEDIC_ORGANIZATION_NAME, MEDIC_DEPARTMENT_NAME, MEDIC_LICENSE_SERIES, MEDIC_LICENSE_NUMBER, MEDIC_LICENSE_ISSUE_DATE,
                              MEDIC_LICENSE_EXPIRY_DATE, DRIVER_FULL_NAME, DRIVER_PERSONNEL_NUMBER, DRIVER_ORGANIZATION_NAME, DRIVER_DEPARTMENT_NAME,
                              DRIVER_TIN, DRIVER_LICENSE_SERIES, DRIVER_LICENSE_NUMBER, DRIVER_LICENSE_ISSUE_DATE, DRIVER_LICENSE_EXPIRY_DATE);
        var actual = medicRequestRegistryDynamicRepository.findMedicRequestRegistry(pageRequest,
                                                                                    new MedicRequestSearchDto(
                                                                                            fieldSet,
                                                                                            "0000",
                                                                                            "1913586",
                                                                                            "0002",
                                                                                            UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                                                                            Set.of(UUID.fromString(
                                                                                                    "d640b449-4482-4a48-bc05-2bbcfa66b465")),
                                                                                            new DateRange(
                                                                                                    LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                                                                                                    LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                                                                                            )));
        
        assertThat(actual.totalElements()).isEqualTo(1);
        assertThat(actual.content().getFirst()).containsEntry(MEDIC_REQUEST_HUMAN_READABLE_ID.getAlias(), "TL-0000-00000002");
        assertThat(actual.content().getFirst()).containsEntry(DRIVER_TIN.getAlias(), "0000003");
    }
}
