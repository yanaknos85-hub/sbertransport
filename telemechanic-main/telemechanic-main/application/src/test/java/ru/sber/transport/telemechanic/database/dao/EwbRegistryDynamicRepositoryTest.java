package ru.sber.transport.telemechanic.database.dao;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.EwbSortOption;
import ru.sber.transport.telemechanic.dto.ewb_report.DrivingLicenseRegistryInfo;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryResponse;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.mapper.EwbRegistryMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.sber.transport.telemechanic.enumerate.EwbRegistryField.*;

@SpringBootTest
@EmbeddedPostgres
class EwbRegistryDynamicRepositoryTest {
    
    @Autowired
    private DSLContext dslContext;
    @Autowired
    private EwbRegistryDynamicRepository ewbRegistryDynamicRepository;
    @Autowired
    private DrivingLicenseRepository drivingLicenseRepository;
    @Autowired
    private EwbRepository ewbRepository;
    
    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final String ZONE_ID = "Europe/Moscow";
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void shouldFindEwbsWithDynamicSelect() {
        var request = new EwbRegistryAllOrganizationsRequest(
                Set.of(EWB_HUMAN_READABLE_ID,
                       EWB_STATUS,
                       EWB_CREATION_TIME,
                       EWB_TELEMECH_DECISION_OUT_TIME,
                       EWB_TELEMECH_DECISION_IN_TIME,
                       DRIVER_FULL_NAME,
                       DRIVER_ORGANIZATION_NAME,
                       DRIVER_DEPARTMENT_NAME,
                       TRANSPORT_STATE_NUMBER,
                       TRANSPORT_TYPE_TITLE,
                       TRANSPORT_SUBTYPE_TITLE,
                       DRIVING_LICENSE_SERIES,
                       DRIVING_LICENSE_NUMBER,
                       DRIVING_LICENSE_ISSUE_DATE,
                       DRIVING_LICENSE_EXPIRY_DATE
                      ),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        var actual = ewbRegistryDynamicRepository.findEwbRegistry(request.preparePageRequest(),
                                                                  request.fieldSet(),
                                                                  request.searchText(),
                                                                  request.humanReadableId(),
                                                                  request.organizationId(),
                                                                  request.departmentIds(),
                                                                  request.period());
        assertEquals(7, actual.ewbRegistrySearchResult().size());
        assertEquals(7, actual.totalElements());
        
        var ewbs = ewbRepository.findAll().stream()
                                .sorted(Comparator.comparing(Ewb::getHumanReadableId))
                                .toList();
        for (int i = 0; i < actual.totalElements(); i++) {
            assertResult(actual.ewbRegistrySearchResult().get(i), ewbs.get(i));
        }
        
        request = request.withSearchText("EWB_ID")
                         .withHumanReadableId("EWB_ID")
                         .withOrganizationId(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                         .withDepartmentIds(Set.of())
                         .withPeriod(new DateRange(LocalDateTime.of(2000, 1, 1, 0, 0, 0),
                                                   LocalDateTime.of(2025, 1, 1, 0, 0, 0))
                                    )
                         .withSortSetting(new EwbRegistryAllOrganizationsRequest.SortSetting(
                                 EwbSortOption.HUMAN_READABLE_ID, false
                         ));
        actual = ewbRegistryDynamicRepository.findEwbRegistry(request.preparePageRequest(),
                                                              request.fieldSet(),
                                                              request.searchText(),
                                                              request.humanReadableId(),
                                                              request.organizationId(),
                                                              request.departmentIds(),
                                                              request.period());
        assertEquals(6, actual.ewbRegistrySearchResult().size());
        assertEquals(6, actual.totalElements());
        
        var ewbWithFiltersAndDescSort = ewbs.stream()
                                            .filter(ewb -> ewb.getHumanReadableId().contains("EWB_ID"))
                                            .filter(ewb -> ewb.getDriver().getEmployee().getOrganization().getId()
                                                              .equals(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6")))
                                            .filter(ewb -> ewb.getCreationTime().isAfter(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                                                           && ewb.getCreationTime().isBefore(LocalDateTime.of(2025, 1, 1, 0, 0, 0)))
                                            .sorted(Comparator.comparing(Ewb::getHumanReadableId).reversed())
                                            .toList();
        for (int i = 0; i < actual.totalElements(); i++) {
            assertResult(actual.ewbRegistrySearchResult().get(i), ewbWithFiltersAndDescSort.get(i));
        }
        
        request = request.withDepartmentIds(Set.of(UUID.fromString("20d4a338-e121-4a0b-9d80-a7b6b035484f")));
        actual = ewbRegistryDynamicRepository.findEwbRegistry(request.preparePageRequest(),
                                                              request.fieldSet(),
                                                              request.searchText(),
                                                              request.humanReadableId(),
                                                              request.organizationId(),
                                                              request.departmentIds(),
                                                              request.period());
        assertEquals(6, actual.ewbRegistrySearchResult().size());
        assertEquals(6, actual.totalElements());
        
        var ewbWithFiltersAndDepartmentsAndDescSort = ewbs.stream()
                                                          .filter(ewb -> ewb.getHumanReadableId().contains("EWB_ID"))
                                                          .filter(ewb -> ewb.getDriver().getEmployee().getOrganization().getId()
                                                                            .equals(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6")))
                                                          .filter(ewb -> Objects.equals(UUID.fromString("20d4a338-e121-4a0b-9d80-a7b6b035484f"),
                                                                                        ewb.getDriver().getEmployee().getDepartment().getId()))
                                                          .filter(ewb -> ewb.getCreationTime().isAfter(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                                                                         && ewb.getCreationTime().isBefore(LocalDateTime.of(2025, 1, 1, 0, 0, 0)))
                                                          .sorted(Comparator.comparing(Ewb::getHumanReadableId).reversed())
                                                          .toList();
        
        for (int i = 0; i < actual.totalElements(); i++) {
            assertResult(actual.ewbRegistrySearchResult().get(i), ewbWithFiltersAndDepartmentsAndDescSort.get(i));
        }
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void findEwbRegistryForExcelTest() {
        var request = new EwbRegistryAllOrganizationsRequest(
                Set.of(EWB_HUMAN_READABLE_ID,
                       EWB_STATUS,
                       EWB_CREATION_TIME,
                       EWB_TELEMECH_DECISION_OUT_TIME,
                       EWB_TELEMECH_DECISION_IN_TIME,
                       DRIVER_FULL_NAME,
                       DRIVER_ORGANIZATION_NAME,
                       DRIVER_DEPARTMENT_NAME,
                       TRANSPORT_STATE_NUMBER,
                       DRIVING_LICENSE_SERIES,
                       DRIVING_LICENSE_NUMBER,
                       DRIVING_LICENSE_ISSUE_DATE,
                       DRIVING_LICENSE_EXPIRY_DATE
                      ),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        var actual = ewbRegistryDynamicRepository.findEwbRegistryExcel(request.fieldSet(),
                                                                       request.searchText(),
                                                                       request.humanReadableId(),
                                                                       request.organizationId(),
                                                                       request.departmentIds(),
                                                                       request.period());
        
        var ewbs = ewbRepository.findAll().stream()
                                .sorted(Comparator.comparing(Ewb::getHumanReadableId))
                                .toList();
        
        assertNotNull(actual);
        assertEquals(ewbs.size(), actual.size());
        var resultList = EwbRegistryMapper.recordToEwbRegistryExcelSelfOrganizationDto(actual, request.fieldSet());
        for (int i = 0; i < actual.size(); i++) {
            assertResult(resultList.get(i).getRegistry(), ewbs.get(i));
        }
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getAllFields() {
        var request = new EwbRegistryAllOrganizationsRequest(
                Set.of(EwbRegistryField.values()),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        var actual = ewbRegistryDynamicRepository.findEwbRegistryExcel(request.fieldSet(),
                                                                       request.searchText(),
                                                                       request.humanReadableId(),
                                                                       request.organizationId(),
                                                                       request.departmentIds(),
                                                                       request.period());
        
        var ewbs = ewbRepository.findAll().stream()
                                .sorted(Comparator.comparing(Ewb::getHumanReadableId))
                                .toList();
        
        assertNotNull(actual);
        assertEquals(ewbs.size(), actual.size());
    }
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getRequiredFields() {
        var request = new EwbRegistryAllOrganizationsRequest(
                Set.of(EWB_HUMAN_READABLE_ID, EWB_STATUS, EWB_CREATION_TIME,
                       EWB_TELEMECH_DECISION_OUT_TIME, EWB_TELEMECH_DECISION_IN_TIME,
                       DRIVER_FULL_NAME, DRIVER_ORGANIZATION_NAME, DRIVER_DEPARTMENT_NAME),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        var actual = ewbRegistryDynamicRepository.findEwbRegistryExcel(request.fieldSet(),
                                                                       request.searchText(),
                                                                       request.humanReadableId(),
                                                                       request.organizationId(),
                                                                       request.departmentIds(),
                                                                       request.period());
        
        var ewbs = ewbRepository.findAll().stream()
                                .sorted(Comparator.comparing(Ewb::getHumanReadableId))
                                .toList();
        
        assertNotNull(actual);
        assertEquals(ewbs.size(), actual.size());
    }
    
    private void assertResult(EwbRegistryResponse actual, Ewb expected) {
        assertEquals(expected.getHumanReadableId(), actual.ewb().humanReadableId());
        assertEquals(expected.getStatus(), actual.ewb().status());
        assertEquals(expected.getCreationTime(), actual.ewb().creationTime());
        assertEquals(expected.getTelemechDecisionOut(), actual.ewb().telemechDecisionOutTime());
        assertEquals(expected.getTelemechDecisionIn(), actual.ewb().telemechDecisionInTime());
        assertEquals(expected.getDriver().getEmployee().getFIO(), actual.driver().fullName());
        assertEquals(expected.getDriver().getEmployee().getOrganization().getOfficialName(),
                     actual.driver().organizationName());
        assertEquals(expected.getDriver().getEmployee().getDepartment().getDepartmentName(),
                     actual.driver().departmentName());
        assertEquals(expected.getTransport().getStateNumber(), actual.transport().stateNumber());
        assertEquals(expected.getTransport().getType(), actual.transport().type());
        assertEquals(expected.getTransport().getSubtype(), actual.transport().subtype());
        assertDrivingLicense(actual.drivingLicense(), expected);
    }
    
    private void assertDrivingLicense(DrivingLicenseRegistryInfo actual, Ewb ewb) {
        var expected = drivingLicenseRepository.findById(ewb.getDriverLicenseId())
                                               .orElseThrow(() -> new JUnitException("Driving license not found"));
        assertEquals(expected.getSeries(), actual.series());
        assertEquals(expected.getNumber(), actual.number());
        assertEquals(expected.getIssueDate(), actual.issueDate());
        assertEquals(expected.getExpiryDate(), actual.expiryDate());
    }
    
    private void assertResult(Map<String, String> actual, Ewb expected) {
        assertEquals(expected.getHumanReadableId(), actual.get(EWB_HUMAN_READABLE_ID.getExcelColumnName()));
        assertEquals(expected.getStatus().getRusName(), actual.get(EWB_STATUS.getExcelColumnName()));
        assertEquals(OffsetDateTime.of(expected.getCreationTime(), ZoneOffset.UTC)
                                   .atZoneSameInstant(ZoneId.of(ZONE_ID))
                                   .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                     actual.get(EWB_CREATION_TIME.getExcelColumnName()));
        assertEquals(OffsetDateTime.of(expected.getTelemechDecisionOut(), ZoneOffset.UTC)
                                   .atZoneSameInstant(ZoneId.of(ZONE_ID))
                                   .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                     !actual.get(EWB_TELEMECH_DECISION_OUT_TIME.getExcelColumnName()).isEmpty()
                     ? actual.get(EWB_TELEMECH_DECISION_OUT_TIME.getExcelColumnName())
                     : null);
        assertEquals(OffsetDateTime.of(expected.getTelemechDecisionIn(), ZoneOffset.UTC)
                                   .atZoneSameInstant(ZoneId.of(ZONE_ID))
                                   .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                     !actual.get(EWB_TELEMECH_DECISION_IN_TIME.getExcelColumnName()).isEmpty()
                     ? actual.get(EWB_TELEMECH_DECISION_IN_TIME.getExcelColumnName())
                     : null);
        assertEquals(expected.getTransport().getStateNumber(), actual.get(TRANSPORT_STATE_NUMBER.getExcelColumnName()));
    }
    
}



