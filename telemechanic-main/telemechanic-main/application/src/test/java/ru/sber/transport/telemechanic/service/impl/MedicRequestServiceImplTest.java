package ru.sber.transport.telemechanic.service.impl;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.database.dao.MedicRequestRegistryDynamicRepository;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchDto;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchResponse;
import ru.sber.transport.telemechanic.enumerate.MedicRequestSortOption;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.exception.DateRangeValidationException;
import ru.sber.transport.telemechanic.exception.DepartmentNotInOrganizationException;
import ru.sber.transport.telemechanic.mapper.MedicRequestMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static ru.sber.transport.telemechanic.enumerate.MedicRequestField.*;
import static ru.sber.transport.telemechanic.exception.DepartmentNotInOrganizationException.MSG_FORMAT;


@ExtendWith(MockitoExtension.class)
class MedicRequestServiceImplTest {
    
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private MedicRequestRegistryDynamicRepository repository;
    @Mock
    private MedicRequestMapper mapper;
    @InjectMocks
    private MedicRequestServiceImpl service;
    
    @Test
    void searchRegistryForAllOrganizations() {
        var fieldSet = Set.of(EWB_ID, EWB_UUID, EWB_HUMAN_READABLE_ID, EWB_MEDIC_DECISION_TIME, MEDIC_REQUEST_HUMAN_READABLE_ID,
                              MEDIC_REQUEST_SYSTOLIC_PRESSURE, MEDIC_REQUEST_DIASTOLIC_PRESSURE, MEDIC_REQUEST_PULSE, MEDIC_REQUEST_TEMPERATURE,
                              MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT, MEDIC_REQUEST_STATUS, MEDIC_FULL_NAME, MEDIC_PERSONNEL_NUMBER,
                              MEDIC_ORGANIZATION_NAME, MEDIC_DEPARTMENT_NAME, MEDIC_LICENSE_SERIES, MEDIC_LICENSE_NUMBER, MEDIC_LICENSE_ISSUE_DATE,
                              MEDIC_LICENSE_EXPIRY_DATE, DRIVER_FULL_NAME, DRIVER_PERSONNEL_NUMBER, DRIVER_ORGANIZATION_NAME, DRIVER_DEPARTMENT_NAME,
                              DRIVER_LICENSE_SERIES, DRIVER_LICENSE_NUMBER, DRIVER_LICENSE_ISSUE_DATE, DRIVER_LICENSE_EXPIRY_DATE);
        var map = createExpectedMap();
        var request1 = new MedicRequestRegistryAllOrganizationsRequest(fieldSet,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       UUID.randomUUID(),
                                                                       Set.of(UUID.randomUUID()),
                                                                       new DateRange(LocalDateTime.now().minusYears(1), LocalDateTime.now()),
                                                                       null,
                                                                       null);
        
        doReturn(Set.of()).when(departmentService).getNotOrganizationIds(request1.organizationId(), request1.departmentIdSet());
        doReturn(Instancio.create(MedicRequestSearchDto.class)).when(mapper).medicRequestAllOrganizationsToMedicRequestSearchDto(any());
        doReturn(new MedicRequestSearchResponse(List.of(map), 1)).when(repository).findMedicRequestRegistry(
                any(PageRequest.class),
                any(MedicRequestSearchDto.class)
                                                                                                           );
        
        var actual = service.searchRegistryForAllOrganizations(request1);
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID.getSqlValue()));
        assertThat(actual.getContent())
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .build())
                .isEqualTo(List.of(map));
    }
    
    @Test
    void searchRegistryForSelfOrganization() {
        var fieldSet = Set.of(EWB_ID, EWB_UUID, EWB_HUMAN_READABLE_ID, EWB_MEDIC_DECISION_TIME, MEDIC_REQUEST_HUMAN_READABLE_ID,
                              MEDIC_REQUEST_SYSTOLIC_PRESSURE, MEDIC_REQUEST_DIASTOLIC_PRESSURE, MEDIC_REQUEST_PULSE, MEDIC_REQUEST_TEMPERATURE,
                              MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT, MEDIC_REQUEST_STATUS, MEDIC_FULL_NAME, MEDIC_PERSONNEL_NUMBER,
                              MEDIC_ORGANIZATION_NAME, MEDIC_DEPARTMENT_NAME, MEDIC_LICENSE_SERIES, MEDIC_LICENSE_NUMBER, MEDIC_LICENSE_ISSUE_DATE,
                              MEDIC_LICENSE_EXPIRY_DATE, DRIVER_FULL_NAME, DRIVER_PERSONNEL_NUMBER, DRIVER_ORGANIZATION_NAME, DRIVER_DEPARTMENT_NAME,
                              DRIVER_LICENSE_SERIES, DRIVER_LICENSE_NUMBER, DRIVER_LICENSE_ISSUE_DATE, DRIVER_LICENSE_EXPIRY_DATE);
        var map = createExpectedMap();
        var employee = Instancio.create(Employee.class);
        var request1 = new MedicRequestRegistrySelfOrganizationRequest(fieldSet,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       Set.of(UUID.randomUUID()),
                                                                       new DateRange(LocalDateTime.now().minusYears(1), LocalDateTime.now()),
                                                                       null,
                                                                       null);
        
        doReturn(employee).when(employeeService).getByUserId(employee.getUserId());
        doReturn(Set.of()).when(departmentService).getNotOrganizationIds(employee.getOrganization().getId(), request1.departmentIdSet());
        doReturn(Instancio.create(MedicRequestSearchDto.class)).when(mapper).medicRequestSelfOrganizationToMedicRequestSearchDto(any(), any());
        doReturn(new MedicRequestSearchResponse(List.of(map), 1)).when(repository).findMedicRequestRegistry(
                any(PageRequest.class),
                any(MedicRequestSearchDto.class)
                                                                                                           );
        
        var actual = service.searchRegistryForSelfOrganization(request1, employee.getUserId());
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID.getSqlValue()));
        assertThat(actual.getContent())
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .build())
                .isEqualTo(List.of(map));
    }
    
    @Test
    void searchRegistryDateValidationException() {
        var request1 = new MedicRequestRegistryAllOrganizationsRequest(null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       new DateRange(LocalDateTime.now(), LocalDateTime.now().minusYears(1)),
                                                                       null,
                                                                       null);
        
        assertThatExceptionOfType(DateRangeValidationException.class)
                .isThrownBy(() -> service.searchRegistryForAllOrganizations(request1))
                .withMessage("Некорректно задан период создания заявки");
    }
    
    @Test
    void searchRegistryDepartmentValidationException() {
        var request1 = new MedicRequestRegistryAllOrganizationsRequest(null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       UUID.randomUUID(),
                                                                       Set.of(UUID.randomUUID()),
                                                                       new DateRange(LocalDateTime.now().minusYears(1), LocalDateTime.now()),
                                                                       null,
                                                                       null);
        
        doReturn(request1.departmentIdSet()).when(departmentService).getNotOrganizationIds(request1.organizationId(), request1.departmentIdSet());
        
        assertThatExceptionOfType(DepartmentNotInOrganizationException.class)
                .isThrownBy(() -> service.searchRegistryForAllOrganizations(request1))
                .withMessage(String.format(MSG_FORMAT,
                                           request1.organizationId().toString(),
                                           request1.departmentIdSet().stream()
                                                   .map(UUID::toString)
                                                   .collect(Collectors.joining(", "))));
    }
    
    private Map<String, Object> createExpectedMap() {
        var map = new HashMap<String, Object>();
        map.put(EWB_ID.getAlias(), "2d497a93-f1f0-47f7-a0e4-fcaf1b9f7801");
        map.put(EWB_UUID.getAlias(), "218447c3-4b34-441e-ae98-9f73b7c27f65");
        map.put(EWB_HUMAN_READABLE_ID.getAlias(), "PL-0000-00000000");
        map.put(EWB_MEDIC_DECISION_TIME.getAlias(), 946684800000L);
        
        map.put(MEDIC_REQUEST_HUMAN_READABLE_ID.getAlias(), "TL-0000-000000");
        map.put(MEDIC_REQUEST_SYSTOLIC_PRESSURE.getAlias(), 120);
        map.put(MEDIC_REQUEST_DIASTOLIC_PRESSURE.getAlias(), 80);
        map.put(MEDIC_REQUEST_PULSE.getAlias(), 60);
        map.put(MEDIC_REQUEST_TEMPERATURE.getAlias(), 36.6);
        map.put(MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT.getAlias(), 0.0);
        map.put(MEDIC_REQUEST_STATUS.getAlias(), TelemedicineStatus.DONE.getDescription());
        
        map.put(MEDIC_FULL_NAME.getAlias(), "Петров Петр Петрович");
        map.put(MEDIC_PERSONNEL_NUMBER.getAlias(), "00000000");
        map.put(MEDIC_ORGANIZATION_NAME.getAlias(), "ООО Компания");
        map.put(MEDIC_DEPARTMENT_NAME.getAlias(), "Бригада");
        
        map.put(MEDIC_LICENSE_SERIES.getAlias(), "11111");
        map.put(MEDIC_LICENSE_NUMBER.getAlias(), "123456789");
        map.put(MEDIC_LICENSE_ISSUE_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        map.put(MEDIC_LICENSE_EXPIRY_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        
        map.put(DRIVER_FULL_NAME.getAlias(), "Иванов Иван Иванович");
        map.put(DRIVER_PERSONNEL_NUMBER.getAlias(), "00000001");
        map.put(DRIVER_ORGANIZATION_NAME.getAlias(), "ООО Компания");
        map.put(DRIVER_DEPARTMENT_NAME.getAlias(), "Бригада");
        
        map.put(DRIVER_LICENSE_SERIES.getAlias(), "22222");
        map.put(DRIVER_LICENSE_NUMBER.getAlias(), 123456789);
        map.put(DRIVER_LICENSE_ISSUE_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        map.put(DRIVER_LICENSE_EXPIRY_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        
        return map;
    }
}
