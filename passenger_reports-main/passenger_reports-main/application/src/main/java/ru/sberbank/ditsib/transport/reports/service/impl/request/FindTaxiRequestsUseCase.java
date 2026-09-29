package ru.sberbank.ditsib.transport.reports.service.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.EconomyDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.PositionShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.taxi.ContractorResultSet;
import ru.sberbank.ditsib.transport.reports.dto.taxi.LimitResultSet;
import ru.sberbank.ditsib.transport.reports.dto.taxi.TaxiTripResultSet;
import ru.sberbank.ditsib.transport.reports.mappers.DriverMapper;
import ru.sberbank.ditsib.transport.reports.mappers.VehicleMapper;
import ru.sberbank.ditsib.transport.reports.model.mapping.PersonalResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.model.mapping.TaxiResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class FindTaxiRequestsUseCase {
    
    private final DepartmentService departmentService;
    private final DriverMapper driverMapper;
    private final VehicleMapper vehicleMapper;
    private final EntityManager em;
    private final ObjectMapper objectMapper;

    public Page<TaxiResponseDTO> findTaxiRequests(RequestForTaxiReportDTO requestDTO) {
        log.debug("findTaxiRequests start: " + LocalDateTime.now());

        RequestServiceStaticHelper.DataAndCountQueries
                dataAndCountQueries = RequestServiceStaticHelper.prepareFindTaxiRequestsQuery(em, departmentService, requestDTO);
        List<TaxiResponseSqlResultSetMapping> sqlResult = dataAndCountQueries.queryData.getResultList()
                                                                                       .stream()
                                                                                       .map(it -> objectMapper.convertValue(it, TaxiResponseSqlResultSetMapping.class))
                                                                                       .toList();
        var total = (long) dataAndCountQueries.queryCount.getSingleResult();

        // Подготовим данные по всем организациям в результате
        List<UUID> organizationIds = sqlResult.stream()
                .map(TaxiResponseSqlResultSetMapping::getOrganizationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, OrganizationShortDTO> mapForOrganizations = RequestServiceStaticHelper.prepareMapForOrganizations(em, organizationIds);

        // Подготовим данные по всем тарифам в результате
        List<UUID> tariffIds = sqlResult.stream()
                .map(TaxiResponseSqlResultSetMapping::getTariffId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, TariffShortDTO> mapForTariffs = RequestServiceStaticHelper.prepareMapForTariffsShort(em, tariffIds);

        // Подготовим данные по всем точкам маршрута в результате
        List<UUID> requestIds =
                sqlResult.stream().map(TaxiResponseSqlResultSetMapping::getId).filter(Objects::nonNull).toList();
        Map<UUID, List<WaypointDTO>> mapForWaypoints = RequestServiceStaticHelper.prepareMapForWaypoints(em, requestIds);

        // Подготовим данные для всех сотрудников в результате
        var employeesIds =
                Stream.concat(sqlResult.stream().map(TaxiResponseSqlResultSetMapping::getApprovedBy).filter(Objects::nonNull),
                                Stream.concat(sqlResult.stream().map(TaxiResponseSqlResultSetMapping::getAuthorId).filter(Objects::nonNull),
                                        sqlResult.stream().map(TaxiResponseSqlResultSetMapping::getPassengerId).filter(Objects::nonNull)))
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
        var departmentIds = sqlResult.stream().map(TaxiResponseSqlResultSetMapping::getPassengerId)
                .filter(Objects::nonNull).map(mapForEmployees::get)
                .filter(Objects::nonNull).map(EmployeeDTO::getDepartmentId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, DepartmentShortDTO> mapForDepartments = RequestServiceStaticHelper.prepareMapForDepartments(em, departmentIds);

        // Подготовим данные по всем целям встретившимся в результате
        var purposeIds = sqlResult.stream().map(TaxiResponseSqlResultSetMapping::getPurposeId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, TripPurposeDTO> mapForPurpose = RequestServiceStaticHelper.prepareMapForPurpose(em, purposeIds);

        // Подготовим данные по совместным поездкам
        var singleTripIds = sqlResult.stream()
                .filter(e -> !e.isCoopTrip())
                .map(TaxiResponseSqlResultSetMapping::getId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, TaxiTripResultSet> mapForTaxiTripSingle = RequestServiceStaticHelper.prepareMapForTaxiTripSingle(em, singleTripIds);

        var coopTripIds = sqlResult.stream()
                .filter(TaxiResponseSqlResultSetMapping::isCoopTrip)
                .map(TaxiResponseSqlResultSetMapping::getRideId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, TaxiTripResultSet> mapForTaxiTripCoop = RequestServiceStaticHelper.prepareMapForTaxiTripCoop(em, coopTripIds);
        Map<UUID, EconomyDataDTO> mapForEconomy = RequestServiceStaticHelper.prepareDataForCalculatingEconomyForTaxi(em, coopTripIds);

        var limitIds = sqlResult.stream()
                .map(TaxiResponseSqlResultSetMapping::getLimitId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, LimitResultSet> mapForLimit = RequestServiceStaticHelper.prepareMapForLimit(em, limitIds);

        var contractorIds = sqlResult.stream()
                .map(TaxiResponseSqlResultSetMapping::getContractorId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, ContractorResultSet> mapForContractor = RequestServiceStaticHelper.prepareMapForContractor(em, contractorIds);
        // Дополним основной результат поиска детальными данными отдельных атрибутов
        var response = sqlResult.stream()
                .map(rec -> RequestServiceStaticHelper.mapSqlResultToResponseForTaxi(objectMapper, driverMapper, vehicleMapper,
                        rec,
                        mapForEmployees,
                        mapForDepartments,
                        mapForPosition,
                        mapForPurpose,
                        mapForTaxiTripSingle,
                        mapForTaxiTripCoop,
                        mapForWaypoints,
                        mapForTariffs,
                        mapForLimit,
                        mapForContractor,
                        mapForOrganizations,
                        mapForEconomy))
                .toList();
        log.debug("findTaxiRequests end: " + LocalDateTime.now());
        return new PageImpl<>(response, RequestReportDTO.getPageRequest(requestDTO), total);
    }
}
