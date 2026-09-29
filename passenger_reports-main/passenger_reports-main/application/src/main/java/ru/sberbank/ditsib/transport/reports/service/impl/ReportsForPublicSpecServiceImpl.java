package ru.sberbank.ditsib.transport.reports.service.impl;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;

import java.util.Collections;
import java.util.Optional;

import static ru.sberbank.ditsib.transport.reports.dao.spec.RequestSearchSpecs.*;

@Component
class ReportsForPublicSpecServiceImpl extends AbstractReportsSpecService<RequestForPublicReportDTO> {
    
    @Override
    public Specification<Request> getReportSpec(RequestForPublicReportDTO requestDTO) {
        return getPublicSpec(super.getReportSpec(requestDTO), requestDTO);
    }
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.PUBLIC;
    }
    
    private Specification<Request> getPublicSpec(Specification<Request> spec, RequestForPublicReportDTO requestDTO) {
        spec = spec.and(withTransportType(transportType().name()));
        
        if (!Optional.ofNullable(requestDTO.getCompensationType()).orElseGet(Collections::emptySet).isEmpty()) {
            spec = spec.and(inTripCompensationType(requestDTO.getCompensationType()));
        }
        if (!Optional.ofNullable(requestDTO.getPublicTransportType()).orElseGet(Collections::emptySet).isEmpty()) {
            spec = spec.and(inPublicTransportType(requestDTO.getPublicTransportType()));
        }
        if (requestDTO.getOrderPaymentFormationFinishingDate() != null) {
            spec = spec.and(orderPaymentFormationFinishingDateBetween(requestDTO.getOrderPaymentFormationFinishingDate().getStart(),
                                                                      requestDTO.getOrderPaymentFormationFinishingDate().getEnd()));
        }
        if (requestDTO.getCreationDate() != null) {
            spec = spec.and(createdBetween(requestDTO.getCreationDate().getStart(), requestDTO.getCreationDate().getEnd()));
        }
        if (requestDTO.getDesiredDateRange() != null) {
            spec = spec.and(desiredBetween(requestDTO.getDesiredDateRange().getStart(), requestDTO.getDesiredDateRange().getEnd()));
        }
        //Дата утверждения
        if (requestDTO.getOrderPaymentFormationStartDate() != null) {
            spec = spec.and(orderPaymentFormationStartDateBetween(requestDTO.getOrderPaymentFormationStartDate().getStart(),
                                                                  requestDTO.getOrderPaymentFormationStartDate().getEnd()));
        }
        if (requestDTO.getApproveDate() != null){
            spec = spec.and(approvedBetween(requestDTO.getApproveDate().getStart(), requestDTO.getApproveDate().getEnd()));
        }
        return spec;
    }
}
