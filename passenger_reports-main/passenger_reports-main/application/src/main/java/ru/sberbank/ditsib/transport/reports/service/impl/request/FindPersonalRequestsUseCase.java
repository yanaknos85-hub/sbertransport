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
import ru.sberbank.ditsib.transport.reports.dto.PositionShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.PersonalCarDTO;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.SharedRideKpiResultSet;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.model.mapping.PersonalResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import java.math.BigInteger;
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
public class FindPersonalRequestsUseCase {
    
    private final DepartmentService departmentService;
    private final EntityManager em;
    private final ObjectMapper objectMapper;

    public Page<PersonalResponseDTO> findPersonalRequests(RequestForPersonalReportDTO requestDTO) {
        RequestServiceStaticHelper.DataAndCountQueries dataAndCountQueries =
                RequestServiceStaticHelper.prepareFindPersonalRequestsQuery(em, departmentService, requestDTO);
        List<PersonalResponseSqlResultSetMapping> sqlResult = dataAndCountQueries.queryData.getResultList()
                .stream()
                .map(it -> objectMapper.convertValue(it, PersonalResponseSqlResultSetMapping.class))
                .toList();
        var total = (long) dataAndCountQueries.queryCount.getSingleResult();

        log.debug("findPersonalRequestsNew(), result: count(*) = {}", sqlResult.size());

        // Подготовим данные по всем тарифам в результате
        List<UUID> tariffIds = sqlResult.stream()
                .map(PersonalResponseSqlResultSetMapping::getTariffId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> mapForTariffs = RequestServiceStaticHelper.prepareMapForTariffsPersonal(em, tariffIds);

        // Подготовим данные по всем точкам маршрута в результате
        List<UUID> requestIds =
                sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getId).filter(Objects::nonNull).toList();
        Map<UUID, List<WaypointDTO>> mapForWaypoints = RequestServiceStaticHelper.prepareMapForWaypoints(em, requestIds);

        // Подготовим данные для всех сотрудников в результате
        var employeesIds =
                Stream.concat(
                                Stream.concat(sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getAuthorId).filter(Objects::nonNull),
                                        sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getPassengerId).filter(Objects::nonNull)),
                                sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getEmployeeDriverId).filter(Objects::nonNull))
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
        var coopTripIds = sqlResult.stream()
                .filter(PersonalResponseSqlResultSetMapping::isCoopTrip)
                .map(PersonalResponseSqlResultSetMapping::getSharedRideId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, EconomyDataDTO> mapForEconomy = RequestServiceStaticHelper.prepareDataForCalculatingEconomyForPersonal(em, coopTripIds);

        // Подготовим данные для всех автомобилей в результате
        var carIds =
                sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getPersonalCar).filter(Objects::nonNull).toList();
        Map<UUID, PersonalCarDTO> mapForCars = RequestServiceStaticHelper.prepareMapForCars(em, carIds);

        // Подготовим данные по подразделениям, встретившимся в результирующем списке
        var departmentIds = sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getPassengerId)
                .filter(Objects::nonNull).map(mapForEmployees::get)
                .filter(Objects::nonNull).map(EmployeeDTO::getDepartmentId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, DepartmentShortDTO> mapForDepartments = RequestServiceStaticHelper.prepareMapForDepartments(em, departmentIds);

        // Подготовим данные по всем целям встретившимся в результате
        var purposeIds = sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getPurposeId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, TripPurposeDTO> mapForPurpose = RequestServiceStaticHelper.prepareMapForPurpose(em, purposeIds);

        // Подготовим данные по совместным поездкам
        var sharedRideIds = sqlResult.stream().map(PersonalResponseSqlResultSetMapping::getSharedRideId)
                .filter(Objects::nonNull).distinct()
                .toList();
        Map<UUID, SharedRideKpiResultSet> mapForSharedRide = RequestServiceStaticHelper.prepareMapForSharedRidePersonal(em, sharedRideIds);

        // Дополним основной результат поиска детальными данными отдельных атрибутов
        var response = sqlResult.stream()
                .map(rec -> RequestServiceStaticHelper.mapSqlResultToResponseForPersonal(
                        rec,
                        mapForEmployees,
                        mapForCars,
                        mapForDepartments,
                        mapForPosition,
                        mapForPurpose,
                        mapForSharedRide,
                        mapForWaypoints,
                        mapForTariffs,
                        mapForEconomy))
                .toList();

        // Дополним готовый результат данными пагинации
        return new PageImpl<>(response, RequestReportDTO.getPageRequest(requestDTO), total);
    }
}
