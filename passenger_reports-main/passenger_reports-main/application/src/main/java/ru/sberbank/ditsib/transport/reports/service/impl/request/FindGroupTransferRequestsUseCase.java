package ru.sberbank.ditsib.transport.reports.service.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.PositionShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.taxi.ContractorResultSet;
import ru.sberbank.ditsib.transport.reports.dto.taxi.LimitResultSet;
import ru.sberbank.ditsib.transport.reports.mappers.DriverMapper;
import ru.sberbank.ditsib.transport.reports.mappers.VehicleMapper;
import ru.sberbank.ditsib.transport.reports.model.mapping.GroupTransferResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class FindGroupTransferRequestsUseCase {
    
    private final DepartmentService departmentService;
    private final DriverMapper driverMapper;
    private final VehicleMapper vehicleMapper;
    private final EntityManager em;
    private final ObjectMapper objectMapper;

    public Page<GroupTransferResponseDTO> findGroupTransferRequests(RequestForGroupTransferReportDTO requestDTO) {
        log.debug("findGroupTransferRequests start: " + LocalDateTime.now());

        RequestServiceStaticHelper.DataAndCountQueries
                dataAndCountQueries = RequestServiceStaticHelper.prepareFindGroupTransferRequestsQuery(em, departmentService, requestDTO);
        List<GroupTransferResponseSqlResultSetMapping> sqlResult = dataAndCountQueries.queryData.getResultList()
                .stream()
                .map(it -> objectMapper.convertValue(it, GroupTransferResponseSqlResultSetMapping.class))
                .toList();

        var total = (long) dataAndCountQueries.queryCount.getSingleResult();

        // Подготовим данные по всем организациям в результате
        List<UUID> organizationIds = sqlResult.stream()
                .map(GroupTransferResponseSqlResultSetMapping::getOrganizationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, OrganizationShortDTO> mapForOrganizations = RequestServiceStaticHelper.prepareMapForOrganizations(em, organizationIds);

        // Подготовим данные по всем тарифам в результате
        List<UUID> tariffIds = sqlResult.stream()
                .map(GroupTransferResponseSqlResultSetMapping::getTariffId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, TariffShortDTO> mapForTariffs = RequestServiceStaticHelper.prepareMapForTariffsShort(em, tariffIds);
        // Подготовим данные по всем точкам маршрута в результате
        List<UUID> requestIds =
                sqlResult.stream().map(GroupTransferResponseSqlResultSetMapping::getId).filter(Objects::nonNull).toList();
        Map<UUID, List<WaypointDTO>> mapForWaypoints = RequestServiceStaticHelper.prepareMapForWaypoints(em, requestIds);

        // Подготовим данные для всех сотрудников в результате
        var employeesIds =
                Stream.concat(sqlResult.stream().map(GroupTransferResponseSqlResultSetMapping::getApprovedBy).filter(Objects::nonNull),
                                Stream.concat(sqlResult.stream().map(GroupTransferResponseSqlResultSetMapping::getAuthorId).filter(Objects::nonNull),
                                        sqlResult.stream().map(GroupTransferResponseSqlResultSetMapping::getPassengerId)
                                                .filter(Objects::nonNull)))
                        .distinct()
                        .toList();
        Map<UUID, EmployeeDTO> mapForEmployees = RequestServiceStaticHelper.prepareMapForEmployees(em, employeesIds);

        // Подготовим данные по должностям всех сотрудников в результате
        Map<UUID, PositionShortDTO> mapForPosition = new HashMap<>();
        mapForEmployees.values().forEach(
                employeeDTO -> {
                    if (employeeDTO != null &&
                            employeeDTO.getPositionId() != null &&
                            mapForPosition.get(employeeDTO.getPositionId()) == null) {
                        PositionShortDTO positionShortDTO = new PositionShortDTO();
                        positionShortDTO.setId(employeeDTO.getPositionId());
                        positionShortDTO.setPositionName(employeeDTO.getPositionName());
                        mapForPosition.put(employeeDTO.getPositionId(), positionShortDTO);
                    }
                });

        // Подготовим данные по подразделениям, встретившимся в результирующем списке
        var departmentIds = sqlResult.stream().map(GroupTransferResponseSqlResultSetMapping::getPassengerId)
                .filter(Objects::nonNull).map(mapForEmployees::get)
                .filter(Objects::nonNull).map(EmployeeDTO::getDepartmentId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, DepartmentShortDTO> mapForDepartments = RequestServiceStaticHelper.prepareMapForDepartments(em, departmentIds);

        // Подготовим данные по всем целям встретившимся в результате
        var purposeIds = sqlResult.stream().map(GroupTransferResponseSqlResultSetMapping::getPurposeId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, TripPurposeDTO> mapForPurpose = RequestServiceStaticHelper.prepareMapForPurpose(em, purposeIds);

        var limitIds = sqlResult.stream()
                .map(GroupTransferResponseSqlResultSetMapping::getLimitId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, LimitResultSet> mapForLimit = RequestServiceStaticHelper.prepareMapForLimit(em, limitIds);

        var contractorIds = sqlResult.stream()
                .map(GroupTransferResponseSqlResultSetMapping::getContractorId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, ContractorResultSet> mapForContractor = RequestServiceStaticHelper.prepareMapForContractor(em, contractorIds);

        // Дополним основной результат поиска детальными данными отдельных атрибутов
        var response = sqlResult.stream()
                .map(rec -> RequestServiceStaticHelper.mapSqlResultToResponseForGroupTransfer(objectMapper, driverMapper,
                        vehicleMapper,
                        rec,
                        mapForEmployees,
                        mapForDepartments,
                        mapForPosition,
                        mapForPurpose,
                        mapForWaypoints,
                        mapForTariffs,
                        mapForLimit,
                        mapForContractor,
                        mapForOrganizations))
                .toList();

        log.debug("findGroupTransferRequests end: " + LocalDateTime.now());
        return new PageImpl<>(response, RequestReportDTO.getPageRequest(requestDTO), total);
    }
}
