package ru.sberbank.ditsib.transport.request.service.impl.search;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.AbstractRequestForTnPnC_;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.dto.RequestTaxiSearchDTO;

@Service
public class RequestSearchSpecForTaxiImpl extends AbstractRequestSearchSpecService<RequestForTaxi> {
    
    public Specification<RequestForTaxi> getSpecForTaxi(RequestTaxiSearchDTO requestSearchDTO) {
        return super.getSpec(requestSearchDTO).and(getAdditionalSpec(requestSearchDTO));
    }
    
    private Specification<RequestForTaxi> getAdditionalSpec(RequestTaxiSearchDTO requestSearchDTO) {
        return (root, query, builder) -> {
            query.distinct(true);
            
            Predicate predicate = builder.equal(root.get(Request_.transportType), TransportTypeEnum.TAXI);
            
            if (requestSearchDTO.getCoopTrip() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(AbstractRequestForTnPnC_.coopTrip),
                                                                 requestSearchDTO.getCoopTrip()));
            }
            if (requestSearchDTO.getRideId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(AbstractRequestForTnPnC_.rideId),
                                                                 requestSearchDTO.getRideId()));
            }
            
            return predicate;
        };
    }
}
