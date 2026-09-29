package ru.sberbank.ditsib.transport.reports.service.impl;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Request_;
import ru.sberbank.ditsib.transport.reports.service.ReportsSpecService;
import ru.sberbank.ditsib.transport.reports.utils.ReportsXlsxUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.reports.dao.spec.RequestSearchSpecs.*;

public abstract class AbstractReportsSpecService<T extends RequestReportDTO> implements ReportsSpecService<T> {
    
    public Specification<Request> getReportSpec(T requestDTO) {
        if (requestDTO.isEmpty()) {
            var args = ReportsXlsxUtils.getCreateDateArgs(requestDTO);
            var defaultSpec = createdBetween(args.getCreationDateFrom(), args.getCreationDateTo());
            Set<String> employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
            if (!Optional.ofNullable(employeeOrganizationSet).orElseGet(Collections::emptySet).isEmpty()) {
                defaultSpec = defaultSpec.and(inPassengerOrganizationSet(employeeOrganizationSet));
            }
            return defaultSpec;
        }
        
        Specification<Request> spec = (root, query, cb) -> {
            var predicate = cb.isNotNull(root.get(Request_.id));
            query.distinct(true);
            return predicate;
        };
        if (StringUtils.hasText(requestDTO.getRequestHumanId())) {
            spec = andSpec(spec, withHumanId(requestDTO.getRequestHumanId()));
        }
        if (!requestDTO.getRequestStatusSet().isEmpty()) {
            spec = andSpec(spec, inTripRequestStatus(requestDTO.getRequestStatusSet()));
        }
        if (!requestDTO.getPurposeSet().isEmpty()) {
            spec = andSpec(spec, inTripPurposeSet(requestDTO.getPurposeSet()));
        }
        if (!requestDTO.getContractorSet().isEmpty()) {
            spec = andSpec(spec, inContractorSet(requestDTO.getContractorSet()));
        }
        
        if (requestDTO.getDepartureAddress() != null) {
            spec = spec.and(withDepartureAddress(requestDTO.getDepartureAddress()));
        }
        if (requestDTO.getDestinationAddress() != null) {
            spec = spec.and(withDestinationAddress(requestDTO.getDestinationAddress()));
        }
        
        if (requestDTO.getExpectedCost() != null) {
            spec = spec.and(expectedCostBetween(
                    requestDTO.getExpectedCost().getStart(),
                    requestDTO.getExpectedCost().getEnd()));
        }
        if (requestDTO.getExpectedDistance() != null) {
            spec = spec.and(expectedDistanceBetween(
                    requestDTO.getExpectedDistance().getStart(),
                    requestDTO.getExpectedDistance().getEnd()));
        }
        
        //join(Request_.passenger
        if (StringUtils.hasText(requestDTO.getCostCenter())) {
            spec = andSpec(spec, withPassengerCostCenter(requestDTO.getCostCenter()));
        }
        if (!requestDTO.getEmployeePositionSet().isEmpty()) {
            spec = spec.and(inPassengerPositionSet(requestDTO.getEmployeePositionSet()));
        }
        if (!requestDTO.getEmployeeDepartmentSet().isEmpty()) {
            spec = spec.and(inPassengerDepartmentSet(requestDTO.getEmployeeDepartmentSet()));
        }
        
        var employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
        if (!Optional.ofNullable(employeeOrganizationSet).orElseGet(Collections::emptySet).isEmpty()) {
            spec = spec.and(inPassengerOrganizationSet(employeeOrganizationSet));
        }
        
        if (!Optional.ofNullable(requestDTO.getEmployeeItinerantTypeSet()).orElseGet(Collections::emptySet).isEmpty()) {
            spec = andSpec(spec, inEmployeeItinerantTypeSet(requestDTO.getEmployeeItinerantTypeSet()));
        }
        if (StringUtils.hasText(requestDTO.getEmployeeFIO())) {
            spec = andSpec(spec, withPassengerFio(requestDTO.getEmployeeFIO()));
        }
        
        if (!CollectionUtils.isEmpty(requestDTO.getRatingMarkSet())) {
            spec = andSpec(spec, inRatingMarkSet(requestDTO.getRatingMarkSet()));
        }
        
        return spec;
    }
    
}
