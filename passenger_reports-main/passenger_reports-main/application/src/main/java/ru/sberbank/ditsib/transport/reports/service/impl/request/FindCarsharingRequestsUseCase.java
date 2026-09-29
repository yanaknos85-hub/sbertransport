package ru.sberbank.ditsib.transport.reports.service.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.CarsharingTripRepository;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.taxi.ContractorResultSet;
import ru.sberbank.ditsib.transport.reports.model.CarsharingTrip;
import ru.sberbank.ditsib.transport.reports.model.mapping.CarsharingResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FindCarsharingRequestsUseCase {
    
    private final DepartmentService departmentService;
    private final EntityManager em;
    private final CarsharingTripRepository carsharingTripRepository;
    private final ObjectMapper objectMapper;
    
    public Page<CarsharingResponseDTO> findCarsharingRequests(RequestForCarsharingReportDTO requestDTO) {
        RequestServiceStaticHelper.DataAndCountQueries dataAndCountQueries =
                RequestServiceStaticHelper.prepareFindCarsharingRequestsQuery(em, departmentService, requestDTO);
        List<CarsharingResponseSqlResultSetMapping> sqlResult = dataAndCountQueries.queryData.getResultList()
                .stream()
                .map(it -> objectMapper.convertValue(it, CarsharingResponseSqlResultSetMapping.class))
                .toList();
        var total = (long) dataAndCountQueries.queryCount.getSingleResult();
        
        log.debug("findCarsharingRequestsNew(), result: count(*) = {}", sqlResult.size());
        
        // Подготовим данные по всем тарифам в результате
        List<UUID> tariffIds = sqlResult.stream()
                .map(CarsharingResponseSqlResultSetMapping::getTariffId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, TariffShortDTO> mapForTariffs = RequestServiceStaticHelper.prepareMapForTariffsShort(em, tariffIds);

        // Подготовим данные для всех сотрудников в результате
        var employeesIds =
                sqlResult.stream().map(CarsharingResponseSqlResultSetMapping::getPassengerId).filter(Objects::nonNull)
                         .distinct()
                         .toList();
        Map<UUID, EmployeeDTO> mapForEmployees = RequestServiceStaticHelper.prepareMapForEmployees(em, employeesIds);
        
        
        // Подготовим данные по подразделениям, встретившимся в результирующем списке
        var departmentIds = sqlResult.stream().map(CarsharingResponseSqlResultSetMapping::getPassengerId)
                                     .filter(Objects::nonNull).map(mapForEmployees::get)
                                     .filter(Objects::nonNull).map(EmployeeDTO::getDepartmentId)
                                     .filter(Objects::nonNull).distinct()
                                     .toList();
        Map<UUID, DepartmentShortDTO> mapForDepartments = RequestServiceStaticHelper.prepareMapForDepartments(em, departmentIds);
        
        var orgIds = sqlResult.stream().map(CarsharingResponseSqlResultSetMapping::getPassengerId)
                              .filter(Objects::nonNull).map(mapForEmployees::get)
                              .filter(Objects::nonNull).map(EmployeeDTO::getOrganizationId)
                              .filter(Objects::nonNull).distinct()
                              .toList();
        var mapForOrgs = RequestServiceStaticHelper.prepareMapForOrganization(em, orgIds);
        
        // Подготовим данные по всем целям встретившимся в результате
        var purposeIds = sqlResult.stream().map(CarsharingResponseSqlResultSetMapping::getPurposeId)
                                  .filter(Objects::nonNull).distinct()
                                  .toList();
        Map<UUID, TripPurposeDTO> mapForPurpose = RequestServiceStaticHelper.prepareMapForPurpose(em, purposeIds);
        
        var rentIds = sqlResult.stream().map(CarsharingResponseSqlResultSetMapping::getRentId)
                               .filter(Objects::nonNull).distinct().toList();
        var carsharingTrips = carsharingTripRepository.findAllByRentId(rentIds);
        Map<Integer, CarsharingTrip> transportCompensationMap = carsharingTrips.stream()
                                                                               .collect(Collectors.toMap(CarsharingTrip::getRentId, dto -> dto));
        
        var contractorIds = sqlResult.stream()
                                     .map(CarsharingResponseSqlResultSetMapping::getContractorId)
                                     .filter(Objects::nonNull).distinct()
                                     .toList();
        Map<UUID, ContractorResultSet> mapForContractor = RequestServiceStaticHelper.prepareMapForContractor(em, contractorIds);
        
        
        // Дополним основной результат поиска детальными данными отдельных атрибутов
        var response = sqlResult.stream()
                                .map(rec -> RequestServiceStaticHelper.mapSqlResultToResponseForCarsharing(
                                        rec,
                                        mapForEmployees,
                                        mapForDepartments,
                                        mapForPurpose,
                                        mapForTariffs,
                                        transportCompensationMap.get(rec.getRentId()),
                                        mapForOrgs,
                                        mapForContractor))
                                .toList();
        
        // Дополним готовый результат данными пагинации
        return new PageImpl<>(response, RequestReportDTO.getPageRequest(requestDTO), total);
    }
}
