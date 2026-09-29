package ru.sberbank.ditsib.transport.reports.service.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.reports.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.TransportCompensationDtoMapper;
import ru.sberbank.ditsib.transport.reports.model.TransportCompensation;
import ru.sberbank.ditsib.transport.reports.model.mapping.PublicResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@Slf4j
@AllArgsConstructor
public class FindPublicRequestsUseCase {
    
    private final DepartmentService departmentService;
    private final EntityManager em;
    private final ObjectMapper objectMapper;
    private TransportCompensationRepository transportCompensationRepository;
    private TransportCompensationDtoMapper transportCompensationDtoMapper;
    
    public Page<PublicResponseDTO> findPublicRequests(RequestForPublicReportDTO requestDTO) {
        
        RequestServiceStaticHelper.DataAndCountQueries dataAndCountQueries
                = RequestServiceStaticHelper.prepareFindPublicRequestsQuery(em, departmentService, requestDTO);
        
        List<PublicResponseSqlResultSetMapping> sqlResult = dataAndCountQueries.queryData.getResultList()
                .stream()
                .map(it -> objectMapper.convertValue(it, PublicResponseSqlResultSetMapping.class))
                .toList();
        var total = (long) dataAndCountQueries.queryCount.getSingleResult();
        
        log.debug("findPublicRequestsNew(), result: count(*) = {}", sqlResult.size());
        
        // Подготовим данные по всем тарифам в результате
        List<UUID> tariffIds = sqlResult.stream()
                                        .map(PublicResponseSqlResultSetMapping::getTariffId)
                                        .filter(Objects::nonNull)
                                        .distinct()
                                        .toList();
        Map<UUID, TariffShortDTO> mapForTariffs = RequestServiceStaticHelper.prepareMapForTariffsShort(em, tariffIds);
        
        // Подготовим данные по всем точкам маршрута в результате
        List<UUID> requestIds = sqlResult.stream().map(PublicResponseSqlResultSetMapping::getId).filter(Objects::nonNull).toList();
        
        // Подготовим данные для всех сотрудников в результате
        var employeesIds =
                Stream.concat(sqlResult.stream().map(PublicResponseSqlResultSetMapping::getAuthorId).filter(Objects::nonNull),
                              sqlResult.stream().map(PublicResponseSqlResultSetMapping::getPassengerId).filter(Objects::nonNull))
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
        
        // Подготовим данные для компенсации
        List<TransportCompensation> compensationList = transportCompensationRepository.findAllByRequestId(requestIds).stream().distinct().toList();
        var transportCompensationDtoList = transportCompensationDtoMapper.mapToTransportCompensationDtoList(compensationList);
        Map<UUID, TransportCompensation> transportCompensationMap = compensationList.stream().collect(Collectors.toMap(
                dto -> dto.getRequest().getId(),
                dto -> dto,
                (existing ,replacement) -> existing));
        
        // Подготовим данные по подразделениям, встретившимся в результирующем списке
        var departmentIds = sqlResult.stream().map(PublicResponseSqlResultSetMapping::getPassengerId)
                                     .filter(Objects::nonNull).map(mapForEmployees::get)
                                     .filter(Objects::nonNull).map(EmployeeDTO::getDepartmentId)
                                     .filter(Objects::nonNull).distinct()
                                     .toList();
        Map<UUID, DepartmentShortDTO> mapForDepartments = RequestServiceStaticHelper.prepareMapForDepartments(em, departmentIds);
        
        // Подготовим данные по всем целям встретившимся в результате
        var purposeIds = sqlResult.stream().map(PublicResponseSqlResultSetMapping::getPurposeId)
                                  .filter(Objects::nonNull).distinct()
                                  .toList();
        Map<UUID, TripPurposeDTO> mapForPurpose = RequestServiceStaticHelper.prepareMapForPurpose(em, purposeIds);
        Map<UUID, List<WaypointDTO>> mapForWaypoints = RequestServiceStaticHelper.prepareMapForWaypoints(em, requestIds);
        var filterByTransportType = requestDTO.getPublicTransportType();
        var filterByCompensationType = requestDTO.getCompensationType();
        
        // Дополним основной результат поиска детальными данными отдельных атрибутов
        var response = sqlResult.stream()
                                .filter(rec -> filterByCompensation(transportCompensationMap, rec.getId(), filterByTransportType,
                                                                    filterByCompensationType))
                                .map(rec -> RequestServiceStaticHelper.mapSqlResultToResponse(
                                        rec,
                                        mapForEmployees,
                                        mapForDepartments,
                                        mapForPosition,
                                        mapForPurpose,
                                        mapForTariffs,
                                        transportCompensationMap,
                                        transportCompensationDtoList,
                                        mapForWaypoints))
                                .toList();
        
        // Дополним готовый результат данными пагинации
        return new PageImpl<>(response, RequestReportDTO.getPageRequest(requestDTO), total);
    }
    
    private boolean filterByCompensation(
            Map<UUID, TransportCompensation> transportCompensationMap, UUID id,
            Set<PublicTransportType> filterByTransportType,
            Set<PublicCompensationType> filterByCompensationType) {
        
        boolean resultCompensationType = Boolean.TRUE;
        boolean resultTransportType = Boolean.TRUE;
        
        if (!transportCompensationMap.isEmpty()) {
            TransportCompensation transportCompensation = transportCompensationMap.get(id);
           if (transportCompensation != null) {
               if (isNotEmpty(filterByTransportType)) {
                   var transportType = transportCompensation.getTransportType();
                   resultTransportType = filterByTransportType.contains(transportType);
               }
               if (isNotEmpty(filterByCompensationType)) {
                   var compensationType = transportCompensation.getCompensationType();
                   resultCompensationType = filterByCompensationType.contains(compensationType);
               }
           }
        } else {
            if (isEmpty(filterByTransportType) && isEmpty(filterByCompensationType)) {
                return Boolean.TRUE;
            }
           return Boolean.FALSE;
        }
        
        return resultCompensationType && resultTransportType;
    }
}
