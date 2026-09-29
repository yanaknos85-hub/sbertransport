package ru.sberbank.ditsib.transport.reports.service.impl;

import java.util.Collections;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;


import static ru.sberbank.ditsib.transport.reports.dao.spec.RequestSearchSpecs.*;

@Component
@Slf4j
@RequiredArgsConstructor
class ReportsForTaxiSpecServiceImpl extends AbstractReportsSpecService<RequestForTaxiReportDTO> {
    
    @Override
    public Specification<Request> getReportSpec(
            RequestForTaxiReportDTO requestDTO) {
        return getTaxiSpec(super.getReportSpec(requestDTO), requestDTO);
    }
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.TAXI;
    }
    
    private Specification<Request> getTaxiSpec(Specification<Request> spec, RequestForTaxiReportDTO requestDTO) {
        spec = spec.and(withTransportType(transportType().name()));
        if (requestDTO.getFactCost() != null) {
            spec = andSpec(spec, factCostBetween(requestDTO.getFactCost().getStart(), requestDTO.getFactCost().getEnd()));
        }
        if (requestDTO.getFactDistance() != null) {
            spec = andSpec(spec, factDistanceBetween(requestDTO.getFactDistance().getStart(),
                    requestDTO.getFactDistance().getEnd()));
        }
        if (requestDTO.getActualDepartureDate() != null) {
            spec = andSpec(spec, departureDateBetween(requestDTO.getActualDepartureDate().getStart(),
                    requestDTO.getActualDepartureDate().getEnd()));
        }
        if (requestDTO.getDesiredDateRange() != null) {
            spec = spec.and(desiredBetween(requestDTO.getDesiredDateRange().getStart(), requestDTO.getDesiredDateRange().getEnd()));
        }
        if (requestDTO.getWaypointWaitTime() != null) {
            spec = spec.and(withWaypointWaitTime(requestDTO.getWaypointWaitTime().getStart(),
                    requestDTO.getWaypointWaitTime().getEnd()));
        }
        if (requestDTO.getCreationDate() != null) {
            spec = spec.and(createdBetween(requestDTO.getCreationDate().getStart(), requestDTO.getCreationDate().getEnd()));
        }
        if (requestDTO.getCoopTrip() != null) {
            spec = spec.and(withCoopTrip(requestDTO.getCoopTrip()));
        }
        if (requestDTO.getSharedRideId() != null) {
            spec = spec.and(withSharedRideId(requestDTO.getSharedRideId()));
        }
        if (!Optional.ofNullable(requestDTO.getRatingMarkSet()).orElseGet(Collections::emptySet).isEmpty()) {
            spec = andSpec(spec, inRatingMarkSet(requestDTO.getRatingMarkSet()));
        }
        return spec;
    }
}