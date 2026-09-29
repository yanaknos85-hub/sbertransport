package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;

import static ru.sberbank.ditsib.transport.reports.dao.spec.RequestSearchSpecs.*;

@Component
@RequiredArgsConstructor
class ReportsForPersonalSpecServiceImpl extends AbstractReportsSpecService<RequestForPersonalReportDTO> {
    
    @Override
    public Specification<Request> getReportSpec(RequestForPersonalReportDTO requestDTO) {
        return getPersonalSpec(super.getReportSpec(requestDTO), requestDTO);
    }
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.PERSONAL;
    }
    
    private Specification<Request> getPersonalSpec(Specification<Request> spec, RequestForPersonalReportDTO requestDTO) {
        spec = spec.and(withTransportType(transportType().name()));
        
        if (requestDTO.getOrderPaymentFormationFinishingDate() != null) {
            spec = spec.and(orderPaymentFormationFinishingDateBetween(requestDTO.getOrderPaymentFormationFinishingDate().getStart(),
                                                                      requestDTO.getOrderPaymentFormationFinishingDate().getEnd()));
        }
        if (requestDTO.getCoopTrip() != null) {
            spec = spec.and(withCoopTrip(requestDTO.getCoopTrip()));
        }
        if (requestDTO.getSharedRideId() != null) {
            spec = spec.and(withSharedRideId(requestDTO.getSharedRideId()));
        }
        if (requestDTO.getCreationDate() != null) {
            spec = spec.and(createdBetween(requestDTO.getCreationDate().getStart(), requestDTO.getCreationDate().getEnd()));
        }
        if (requestDTO.getDesiredDateRange() != null) {
            spec = spec.and(desiredBetween(requestDTO.getDesiredDateRange().getStart(), requestDTO.getDesiredDateRange().getEnd()));
        }
        if (requestDTO.getOrderPaymentFormationStartRange() != null) {
            spec = spec.and(orderPaymentFormationStartDateBetween(requestDTO.getOrderPaymentFormationStartRange().getStart(),
                                                                  requestDTO.getOrderPaymentFormationStartRange().getEnd()));
        }
        if (requestDTO.getApproveDate() != null){
            spec = spec.and(approvedBetween(requestDTO.getApproveDate().getStart(), requestDTO.getApproveDate().getEnd()));
        }
        return spec;
    }
}
