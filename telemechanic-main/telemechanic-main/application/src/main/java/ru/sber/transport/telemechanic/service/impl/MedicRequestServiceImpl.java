package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.MedicRequestRegistryDynamicRepository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.medic_request_report.*;
import ru.sber.transport.telemechanic.enumerate.MedicRequestField;
import ru.sber.transport.telemechanic.exception.DateRangeValidationException;
import ru.sber.transport.telemechanic.exception.DepartmentNotInOrganizationException;
import ru.sber.transport.telemechanic.exception.FieldSetValidationException;
import ru.sber.transport.telemechanic.mapper.MedicRequestMapper;
import ru.sber.transport.telemechanic.mapper.MedicRequestRegistryMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.MedicRequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicRequestServiceImpl implements MedicRequestService {
    
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final MedicRequestRegistryDynamicRepository medicRequestRegistryDynamicRepository;
    private final MedicRequestMapper medicRequestMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> searchRegistryForAllOrganizations(MedicRequestRegistryAllOrganizationsRequest request) {
        validateDateRange(request.period());
        validateDepartmentIdSet(request.organizationId(), request.departmentIdSet());
        
        var pageRequest = request.preparePageRequest();
        var searchDto = medicRequestMapper.medicRequestAllOrganizationsToMedicRequestSearchDto(request);
        var result = medicRequestRegistryDynamicRepository.findMedicRequestRegistry(
                pageRequest,
                searchDto
                                                                                   );
        return new PageImpl<>(result.content(),
                              pageRequest,
                              result.totalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> searchRegistryForSelfOrganization(MedicRequestRegistrySelfOrganizationRequest request, UUID userId) {
        var organizationId = employeeService.getByUserId(userId).getOrganization().getId();
        validateDateRange(request.period());
        validateDepartmentIdSet(organizationId, request.departmentIdSet());
        
        var pageRequest = request.preparePageRequest();
        var searchDto = medicRequestMapper.medicRequestSelfOrganizationToMedicRequestSearchDto(request, organizationId);
        var result = medicRequestRegistryDynamicRepository.findMedicRequestRegistry(
                pageRequest,
                searchDto
                                                                                   );
        return new PageImpl<>(result.content(),
                              pageRequest,
                              result.totalElements());
    }
    
    @Override
    public List<MedicRequestExcelAllOrganizationsDto> searchExcelForAllOrganizations(Map<String, ?> parameters) {
        var fieldSet = createFieldsFromParameters(parameters);
        var organizationId = createOrganizationIdFromParameters(parameters);
        var departmentIdSet = createDepartmentIdSetFromParameters(parameters);
        var period = createPeriodFromParameters(parameters);
        validateDateRange(period);
        validateDepartmentIdSet(organizationId, departmentIdSet);
        var result = medicRequestRegistryDynamicRepository.findMedicRequestExcel(
                new MedicRequestSearchDto(
                        fieldSet,
                        createSearchTextFromParameters(parameters),
                        createPersonnelNumberFromParameters(parameters),
                        createHumanReadableIdFromParameters(parameters),
                        organizationId,
                        departmentIdSet,
                        period
                )
                                                                                );
        return MedicRequestRegistryMapper.recordToMedicRequestExcelAllOrganizationsDto(result, fieldSet);
    }
    
    @Override
    public List<MedicRequestExcelSelfOrganizationDto> searchExcelForSelfOrganization(Map<String, ?> parameters, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var fieldSet = createFieldsFromParameters(parameters);
        var organizationId = employee.getOrganization().getId();
        var departmentIdSet = createDepartmentIdSetFromParameters(parameters);
        var period = createPeriodFromParameters(parameters);
        validateDateRange(period);
        validateDepartmentIdSet(organizationId, departmentIdSet);
        var result = medicRequestRegistryDynamicRepository.findMedicRequestExcel(
                new MedicRequestSearchDto(
                        fieldSet,
                        createSearchTextFromParameters(parameters),
                        createPersonnelNumberFromParameters(parameters),
                        createHumanReadableIdFromParameters(parameters),
                        organizationId,
                        departmentIdSet,
                        period
                )
                                                                                );
        return MedicRequestRegistryMapper.recordToMedicRequestExcelSelfOrganizationDto(result, fieldSet);
    }
    
    private void validateDateRange(DateRange period) {
        if (period != null) {
            var start = period.start();
            var end = period.end();
            
            if (start.isAfter(end)) {
                throw new DateRangeValidationException();
            }
        }
    }
    
    private void validateDepartmentIdSet(UUID organizationId, Set<UUID> departmentIdSet) {
        if (Objects.nonNull(organizationId)) {
            var wrongIds = departmentService.getNotOrganizationIds(organizationId, departmentIdSet);
            if (!wrongIds.isEmpty()) {
                throw new DepartmentNotInOrganizationException(organizationId, wrongIds);
            }
        }
    }
    
    private Set<MedicRequestField> createFieldsFromParameters(Map<String, ?> parameters) {
        var value = String.valueOf(parameters.get("fieldSet"));
        if (value == null) {
            throw new FieldSetValidationException();
        }
        return Arrays.stream(value.split(","))
                     .map(MedicRequestField::valueOf)
                     .collect(Collectors.toSet());
    }
    
    private String createSearchTextFromParameters(Map<String, ?> parameters) {
        return Optional.ofNullable(parameters.get("searchText"))
                       .map(str -> "%" + str + "%")
                       .orElse(null);
    }
    
    private String createHumanReadableIdFromParameters(Map<String, ?> parameters) {
        return Optional.ofNullable(parameters.get("humanReadableId"))
                       .map(str -> "%" + str + "%")
                       .orElse(null);
    }
    
    private String createPersonnelNumberFromParameters(Map<String, ?> parameters) {
        return Optional.ofNullable(parameters.get("personnelNumber"))
                       .map(String::valueOf)
                       .orElse(null);
    }
    
    private UUID createOrganizationIdFromParameters(Map<String, ?> parameters) {
        return Optional.ofNullable(parameters.get("organizationId"))
                       .map(str -> UUID.fromString(String.valueOf(str))).orElse(null);
    }
    
    private Set<UUID> createDepartmentIdSetFromParameters(Map<String, ?> parameters) {
        var departments = Optional.ofNullable(parameters.get("departmentIds"))
                                  .map(str -> String.valueOf(str).split(","))
                                  .orElse(null);
        return departments == null ? null
                                   : Arrays.stream(departments)
                                           .map(UUID::fromString)
                                           .collect(Collectors.toSet());
    }
    
    private DateRange createPeriodFromParameters(Map<String, ?> parameters) {
        var start = Optional.ofNullable(parameters.get("start"))
                            .map(str -> LocalDateTime.parse(String.valueOf(str), DateTimeFormatter.ISO_DATE_TIME))
                            .orElse(null);
        var end = Optional.ofNullable(parameters.get("end"))
                          .map(str -> LocalDateTime.parse(String.valueOf(str), DateTimeFormatter.ISO_DATE_TIME))
                          .orElse(null);
        return start != null && end != null
               ? new DateRange(start, end)
               : null;
    }
}
