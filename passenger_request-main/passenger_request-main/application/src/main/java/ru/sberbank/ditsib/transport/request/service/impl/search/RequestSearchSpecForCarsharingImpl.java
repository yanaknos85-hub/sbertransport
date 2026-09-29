package ru.sberbank.ditsib.transport.request.service.impl.search;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing_;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.dto.RequestCarsharingSearchDTO;

@Service
public class RequestSearchSpecForCarsharingImpl extends AbstractRequestSearchSpecService<RequestForCarsharing> {

    public Specification<RequestForCarsharing> getSpecForCarsharing(RequestCarsharingSearchDTO requestSearchDTO) {
        return super.getSpec(requestSearchDTO).and(getAdditionalSpec(requestSearchDTO));
    }
    
    private Specification<RequestForCarsharing> getAdditionalSpec(RequestCarsharingSearchDTO requestSearchDTO) {
        return (root, query, builder) -> {
            query.distinct(true);
            
            Predicate predicate = builder.equal(root.get(Request_.transportType), TransportTypeEnum.CARSHARING);
            
            if (requestSearchDTO.getCoopTrip() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(RequestForCarsharing_.coopTrip),
                                                                 requestSearchDTO.getCoopTrip()));
            }

            //TODO вернуть при реализации совместных поездок на каршеринге
//            if (requestSearchDTO.getSharedRideId() != null) {
//                Join<RequestForCarsharing, MagentaSharedRequest> magentaSharedRequest =
//                        root.join(RequestForCarsharing_.magentaSharedRequest);
//
//                predicate = builder.and(predicate, builder.equal(magentaSharedRequest.get(MagentaSharedRequest_.magentaId),
//                        requestSearchDTO.getSharedRideId()));
//            }
//            if (requestSearchDTO.getKpiSaving() != null) {
//                Join<RequestForTaxi, MagentaSharedRequest>
//                        magentaSharedRequest = root.join(RequestForTaxi_.magentaSharedRequest);
//                Join<MagentaSharedRequest, SharedRideKPI> kpi = magentaSharedRequest.join(MagentaSharedRequest_.kpi);
//                Join<SharedRideKPI, OrderKpi> ordersKpi = kpi.join(SharedRideKPI_.ordersKpi);
//
//                requestSearchDTO.getKpiSaving().prepareQuery();
//                predicate = builder.and(predicate,
//                                        builder.between(ordersKpi.get(OrderKpi_.savings),
//                                                        requestSearchDTO.getKpiSaving().getStart(),
//                                                        requestSearchDTO.getKpiSaving().getEnd()));
//            }
            
            return predicate;
        };
    }
}
