package ru.sberbank.ditsib.transport.request.service.impl.search;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal_;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee_;
import ru.sberbank.ditsib.transport.request.dto.RequestPersonalSearchDTO;

import java.util.Collections;
import java.util.Optional;

@Service
public class RequestSearchSpecForPersonalImpl extends AbstractRequestSearchSpecService<RequestForPersonal> {

    public Specification<RequestForPersonal> getSpecForPersonal(RequestPersonalSearchDTO requestSearchDTO) {
        return super.getSpec(requestSearchDTO).and(getAdditionalSpec(requestSearchDTO));
    }
    
    private Specification<RequestForPersonal> getAdditionalSpec(RequestPersonalSearchDTO requestSearchDTO) {
        return (root, query, builder) -> {
            
            Predicate predicate = builder.equal(root.get(Request_.transportType), TransportTypeEnum.PERSONAL);
            
            if (requestSearchDTO.getCoopTrip() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(RequestForPersonal_.coopTrip),
                                                                 requestSearchDTO.getCoopTrip()));
            }

            var approvalDate = requestSearchDTO.getApprovalDate();
            if (approvalDate != null) {
                if (approvalDate.getEnd() != null) {
                    predicate = builder.and(predicate,
                            builder.between(root.get(Request_.approvalDate),
                                    approvalDate.getStart(),
                                    approvalDate.getEnd()));
                } else {
                    predicate = builder.and(predicate,
                            builder.greaterThan(root.get(Request_.approvalDate),
                                    approvalDate.getStart()));
                }
            }
            
            /*if (requestSearchDTO.getKpiSaving() != null) {   // not used by Sasha Kuzmin
                Join<RequestForPersonal, MagentaSharedRequest>
                        magentaSharedRequest = root.join(RequestForPersonal_.magentaSharedRequest);
                Join<MagentaSharedRequest, SharedRideKPI> kpi = magentaSharedRequest.join(MagentaSharedRequest_.kpi);
                Join<SharedRideKPI, OrderKpi> ordersKpi = kpi.join(SharedRideKPI_.ordersKpi);
                
                requestSearchDTO.getKpiSaving().prepareQuery();
                predicate = builder.and(predicate,
                                        builder.between(ordersKpi.get(OrderKpi_.savings),
                                                        requestSearchDTO.getKpiSaving().getStart(),
                                                        requestSearchDTO.getKpiSaving().getEnd()));
            }*/

            if (!StringUtils.isEmpty(requestSearchDTO.getMvz())) {
                Join<RequestForPersonal, Employee> passenger = root.join(Request_.passenger);

                predicate = builder.and(predicate, builder.equal(passenger.get(Employee_.costCenter),
                        requestSearchDTO.getMvz()));
            }
            
            if (requestSearchDTO.getRideId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(RequestForPersonal_.rideId),
                                                                 requestSearchDTO.getRideId()));
            }

            if (!Optional.ofNullable(requestSearchDTO.getEmployeeItinerantTypeSet()).orElseGet(Collections::emptySet).isEmpty()) {
                Join<RequestForPersonal, Employee> passenger = root.join(Request_.passenger);

                predicate = builder.and(predicate, builder.and(passenger.get(Employee_.itinerantType)
                        .in(requestSearchDTO.getEmployeeItinerantTypeSet())));
            }
            return predicate;
        };
    }
}
