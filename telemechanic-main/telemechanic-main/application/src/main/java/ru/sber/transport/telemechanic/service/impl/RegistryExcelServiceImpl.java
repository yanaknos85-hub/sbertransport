package ru.sber.transport.telemechanic.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.DepartmentRepository;
import ru.sber.transport.telemechanic.database.dao.RequestRepository;
import ru.sber.transport.telemechanic.database.model.DepartmentWithChain;
import ru.sber.transport.telemechanic.dto.RegistryExcelDto;
import ru.sber.transport.telemechanic.dto.ReportQueryParamDto;
import ru.sber.transport.telemechanic.service.RegistryExcelService;

/**
 * Implementation of service for working with registry excels.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistryExcelServiceImpl implements RegistryExcelService {

    private final RequestRepository repository;

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public List<RegistryExcelDto> getAllRegistryByOrganizationId(UUID organizationId, ReportQueryParamDto parameters) {
        var foundRequests = repository.findAllRegistryByOrganizationId(
            getOrganizationId(organizationId, parameters.getOrganizationId()),
            parameters.getHumanReadableId(), parameters.getPersonnelNumber(),
            parameters.getStartCreationTime(), parameters.getEndCreationTime(), parameters.getDepartmentIds(),
            hasDepartments(parameters.getDepartmentIds()));
        setTimeZone(foundRequests);
        return enhanceWithDepartmentChain(foundRequests);
    }

    private List<RegistryExcelDto> enhanceWithDepartmentChain(List<RegistryExcelDto> foundRequests) {
        var departmentIds = foundRequests.stream()
            .map(RegistryExcelDto::getDepartmentId)
            .collect(Collectors.toSet());
        var departmentChains = departmentRepository.findDepartmentChains(departmentIds).stream()
            .collect(Collectors.toMap(DepartmentWithChain::getId, DepartmentWithChain::getChain));

        return foundRequests.stream()
            .map(registryExcelDto -> {
                var chain = departmentChains.getOrDefault(registryExcelDto.getDepartmentId(), "");
                registryExcelDto.setOrgStructureChain(chain);
                return registryExcelDto;
            })
            .toList();
    }

    private void setTimeZone(List<RegistryExcelDto> requests) {
        requests.forEach(request -> {
            request.setCreationTime(setTimeZone(request.getCreationTime()));
            request.setChecksStartedTime(setTimeZone(request.getChecksStartedTime()));
            request.setChecksFinishedTime(setTimeZone(request.getChecksFinishedTime()));
            request.setInspectionTime(setTimeZone(request.getInspectionTime()));
        });
    }

    private LocalDateTime setTimeZone(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        var offsetDateTime = OffsetDateTime.of(utcDateTime, ZoneOffset.UTC);
        var moscowZoneDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("Europe/Moscow"));
        return moscowZoneDateTime.toLocalDateTime();
    }

    private UUID getOrganizationId(UUID organizationId, String parameterOrganizationId) {
        if (Objects.nonNull(organizationId)) {
            return organizationId;
        } else if (Objects.nonNull(parameterOrganizationId)) {
            return UUID.fromString(parameterOrganizationId);
        } else {
            return null;
        }
    }

    private boolean hasDepartments(Set<UUID> departmentIds) {
        return !IterableUtils.isEmpty(departmentIds);
    }

}
