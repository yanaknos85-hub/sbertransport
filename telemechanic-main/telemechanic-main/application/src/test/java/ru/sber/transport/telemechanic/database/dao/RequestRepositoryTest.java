package ru.sber.transport.telemechanic.database.dao;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.RegistryExcelDto;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.FileStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sber.transport.telemechanic.TestData.*;
import static ru.sber.transport.telemechanic.enumerate.CheckType.INSTRUMENT_PANEL;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.*;

@Transactional
@SpringBootTest
@EmbeddedPostgres
class RequestRepositoryTest {
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private RequestRepository requestRepository;
    private Employee employee1;
    private List<Request> requests;
    private final Clock fixedClock = Clock.fixed(LocalDateTime.now().toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
    
    @BeforeEach
    void setUp() {
        var organization = organizationRepository.save(createOrganization1());
        var department1 = departmentRepository.save(createDepartment1(organization, null));
        var department2 = departmentRepository.save(createDepartment2(organization, null));
        var position = positionRepository.save(createPosition1(organization));
        employee1 = employeeRepository.saveAndFlush(createEmployee1(department1, position));
        var employee2 = createEmployee2(department2, position);
        employee2.setPatronymic(null);
        employee2 = employeeRepository.saveAndFlush(employee2);
        requests = new ArrayList<>();
        var count = 20;
        for (var i = 0; i < count; i++) {
            var transport = transportRepository.saveAndFlush(
                    new Transport(UUID.randomUUID(),
                                  "А"
                                          .concat(RandomStringUtils.random(3, false, true))
                                          .concat("ЕК")
                                          .concat(RandomStringUtils.random(3, false, true)),
                                  "LADA",
                                  "VESTA",
                                  100000,
                                  TransportStatus.IN_USE,
                                  "-",
                                  "-",
                                  40,
                                  new HashSet<>(Set.of(ORGANIZATION_1)),
                                  null, null, null));
            var request = createRequest(i % 2 == 0 ? employee1 : employee2, transport, ORGANIZATION_1_ID);
            request.setHumanReadableId(String.format("TM-%03d", i));
            switch (i % 10) {
                case 0 -> {
                    request.setStatus(WARNING);
                    request.setChecksFinishedTime(request.getCreationTime().plusMinutes(30));
                    updateChecksForWarningStatus(request);
                }
                case 1 -> {
                    request.setStatus(DONE);
                    request.setChecksFinishedTime(request.getCreationTime().plusMinutes(30));
                    updateChecksForDoneStatus(request);
                }
                case 2 -> {
                    request.setStatus(CANCELED);
                    updateChecksForWarningStatus(request);
                }
                case 3 -> {
                    request.setStatus(EXPIRED);
                    updateChecksForDoneStatus(request);
                }
                case 4 -> {
                    request.setStatus(FINISHED);
                    request.setInspectionTime(LocalDateTime.now(fixedClock).plusDays(9L));
                    request.setInspector(employee2);
                    updateChecksForDoneStatus(request);
                }
                case 5 -> {
                    request.setStatus(ON_THE_LINE);
                    request.setInspectionTime(LocalDateTime.now(fixedClock).plusDays(12L));
                    request.setInspector(employee2);
                    request.setCreationTime(request.getCreationTime().minusYears(5));
                    request.setComment("Все хорошо");
                    updateChecksForDoneStatus(request);
                }
                case 6 -> {
                    request.setStatus(DECLINED);
                    request.setInspectionTime(LocalDateTime.now(fixedClock).plusDays(12L));
                    request.setInspector(employee2);
                    request.setCreationTime(request.getCreationTime().minusYears(5));
                    request.setComment("Все плохо");
                    updateChecksForWarningStatus(request);
                }
                default -> {
                }
            }
            requests.add(request);
        }
        requests = requestRepository.saveAll(requests);
    }
    
    @Test
    void findAllByAuthorAndStatusIn() {
        var statuses = List.of(DONE, WARNING, ON_THE_LINE);
        var expectedList = requests.stream()
                                   .filter(request -> request.getAuthor().equals(employee1) && statuses.contains(request.getStatus()))
                                   .sorted(Comparator.comparing(Request::getHumanReadableId))
                                   .toList();
        var actualList = requestRepository.findAllByAuthorAndStatusIn(employee1, List.of(DONE, WARNING, ON_THE_LINE))
                                          .stream()
                                          .sorted(Comparator.comparing(Request::getHumanReadableId))
                                          .toList();
        assertEquals(expectedList, actualList);
    }
    
    @Test
    void findAllRegistryByOrganizationId() {
        var actualList1 = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_2_ID,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            Collections.emptySet(),
                                                                            false);
        assertTrue(actualList1.isEmpty());
        var actualList2 = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_1_ID,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            Collections.emptySet(),
                                                                            false);
        assertEquals(20, actualList2.size());
        var humanReadableId = "TM-010";
        var expectedList3 = requests.stream()
                                    .filter(request -> request.getHumanReadableId().equals(humanReadableId))
                                    .toList();
        var actualList3 = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_1_ID,
                                                                            humanReadableId,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            Collections.emptySet(),
                                                                            false);
        assertResult(1, expectedList3, actualList3);
        var expectedList4 = requests.stream()
                                    .filter(request -> request.getAuthor().getPersonnelNumber().equals(EMPLOYEE_1_PERSONNEL_NUMBER))
                                    .sorted(Comparator.comparing(Request::getHumanReadableId))
                                    .toList();
        var actualList4 = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_1_ID,
                                                                            null,
                                                                            EMPLOYEE_1_PERSONNEL_NUMBER,
                                                                            null,
                                                                            null,
                                                                            Collections.emptySet(),
                                                                            false).stream()
                                           .sorted(Comparator.comparing(RegistryExcelDto::getHumanReadableId))
                                           .toList();
        assertResult(10, expectedList4, actualList4);
        
        var start = LocalDateTime.now(fixedClock).minusYears(2);
        var end = LocalDateTime.now(fixedClock).plusMinutes(10);
        var expectedList5 = requests.stream()
                                    .filter(request -> request.getAuthor().getPersonnelNumber().equals(EMPLOYEE_1_PERSONNEL_NUMBER)
                                                       && start.isBefore(request.getCreationTime())
                                                       && end.isAfter(request.getCreationTime()))
                                    .sorted(Comparator.comparing(Request::getHumanReadableId))
                                    .toList();
        var actualList5 = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_1_ID,
                                                                            null,
                                                                            EMPLOYEE_1_PERSONNEL_NUMBER,
                                                                            start,
                                                                            end,
                                                                            Collections.emptySet(),
                                                                            false).stream()
                                           .sorted(Comparator.comparing(RegistryExcelDto::getHumanReadableId))
                                           .toList();
        assertResult(8, expectedList5, actualList5);
        var expectedList6 = requests.stream()
                                    .filter(request -> request.getAuthor().getDepartment().getId().equals(DEPARTMENT_2_ID))
                                    .sorted(Comparator.comparing(Request::getHumanReadableId))
                                    .toList();
        var actualList6 = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_1_ID,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            Collections.singleton(DEPARTMENT_2_ID),
                                                                            true).stream()
                                           .sorted(Comparator.comparing(RegistryExcelDto::getHumanReadableId))
                                           .toList();
        assertResult(10, expectedList6, actualList6);
    }
    
    private void assertResult(int count, List<Request> expectedList, List<RegistryExcelDto> actualList) {
        assertEquals(count, actualList.size());
        for (int i = 0; i < count; i++) {
            assertRegistryExcelDtoEquals(expectedList.get(i), actualList.get(i));
        }
    }
    
    private void assertRegistryExcelDtoEquals(Request expected, RegistryExcelDto actual) {
        assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(expected.getAuthor().getOrganization().getOfficialName(), actual.getOfficialName());
        assertLocalDateTime(expected.getCreationTime(), actual.getCreationTime());
        assertLocalDateTime(expected.getChecksStartedTime(), actual.getChecksStartedTime());
        assertLocalDateTime(expected.getChecksFinishedTime(), actual.getChecksFinishedTime());
        assertLocalDateTime(expected.getInspectionTime(), actual.getInspectionTime());
        assertEquals(getInspectionMarkByStatus(expected.getStatus()), actual.getInspectionMark());
        assertEquals(expected.getTransport().getStateNumber(), actual.getStateNumber());
        assertEquals(expected.getTransport().getBrand(), actual.getBrand());
        assertEquals(expected.getTransport().getModel(), actual.getModel());
        assertEquals(expected.getAuthor().getPersonnelNumber(), actual.getPersonnelNumber());
        assertEquals(expected.getAuthor().getFIO(), actual.getFullName());
        if (Objects.isNull(expected.getInspector())) {
            assertNull(actual.getInspectorFullName());
            assertNull(actual.getInspectorPersonnelNumber());
        } else {
            assertEquals(expected.getInspector().getPersonnelNumber(), actual.getInspectorPersonnelNumber());
            assertEquals(expected.getInspector().getFIO(), actual.getInspectorFullName());
        }
        assertEquals(expected.getComment(), actual.getComment());
    }
    
    private void assertLocalDateTime(LocalDateTime expected, LocalDateTime actual) {
        if (Objects.isNull(expected)) {
            assertNull(actual);
        } else {
            assertEquals(expected.truncatedTo(ChronoUnit.SECONDS), actual.truncatedTo(ChronoUnit.SECONDS));
        }
    }
    
    private String getInspectionMarkByStatus(RequestStatus status) {
        return switch (status) {
            case ON_THE_LINE, FINISHED -> "Пройден";
            case DECLINED -> "Не пройден";
            default -> "";
        };
    }
    
    private void updateChecksForDoneStatus(Request request) {
        request.getChecks().forEach(check -> {
            check.setCheckStatus(CheckStatus.DONE);
            check.getPhotos().add(new CheckPhoto(null, check.getId(), LocalDateTime.now(fixedClock), FileStatus.UPLOADED));
        });
    }
    
    private void updateChecksForWarningStatus(Request request) {
        request.getChecks().forEach(check -> {
            check.setCheckStatus(CheckStatus.DONE);
            check.setAttempt(1);
            check.getPhotos().add(new CheckPhoto(null, check.getId(), LocalDateTime.now(fixedClock), FileStatus.UPLOADED));
            if (check.getCheckType().equals(INSTRUMENT_PANEL)) {
                check.setAttempt(2);
                check.setCheckStatus(CheckStatus.DECLINE);
                check.getPhotos().add(new CheckPhoto(null, check.getId(), LocalDateTime.now(fixedClock), FileStatus.UPLOADED));
                check.getPhotos().add(new CheckPhoto(null, check.getId(), LocalDateTime.now(fixedClock), FileStatus.UPLOADED));
            }
        });
    }
}