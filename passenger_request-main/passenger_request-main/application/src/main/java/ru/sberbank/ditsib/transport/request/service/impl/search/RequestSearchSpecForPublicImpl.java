package ru.sberbank.ditsib.transport.request.service.impl.search;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic_;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee_;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation_;
import ru.sberbank.ditsib.transport.request.dto.RequestPublicSearchDTO;

@Service
public class RequestSearchSpecForPublicImpl extends AbstractRequestSearchSpecService<RequestForPublic> {

    public Specification<RequestForPublic> getSpeForPublic(RequestPublicSearchDTO requestSearchDTO) {
        return super.getSpec(requestSearchDTO).and(getAdditionalSpec(requestSearchDTO));
    }
    
    private Specification<RequestForPublic> getAdditionalSpec(RequestPublicSearchDTO requestSearchDTO) {
        return (root, query, builder) -> {
            query.distinct(true);
            Predicate predicate = builder.equal(root.get(Request_.transportType), TransportTypeEnum.PUBLIC);

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

            if (!StringUtils.isEmpty(requestSearchDTO.getMvz())) {
                Join<RequestForPublic, Employee> passenger = root.join(Request_.passenger);

                predicate = builder.and(predicate, builder.equal(passenger.get(Employee_.costCenter),
                        requestSearchDTO.getMvz()));
            }

            if (requestSearchDTO.getCompensationType() != null) {
                ListJoin<RequestForPublic, TransportCompensation> transportCompensation = root.join(RequestForPublic_.transportCompensation);
                predicate = builder.and(predicate,
                        builder.equal(transportCompensation.get(TransportCompensation_.COMPENSATION_TYPE), requestSearchDTO.getCompensationType()));
            }
            return predicate;
        };
    }
}
