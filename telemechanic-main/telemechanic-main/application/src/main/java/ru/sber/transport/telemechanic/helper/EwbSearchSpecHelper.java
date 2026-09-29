package ru.sber.transport.telemechanic.helper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.Ewb_;
import ru.sber.transport.telemechanic.database.model.Organization_;
import ru.sber.transport.telemechanic.database.model.Transport_;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@UtilityClass
public class EwbSearchSpecHelper {
    
    public Specification<Ewb> getSpecification(
            String searchText, String stateNumber, UUID organizationId, Set<EwbStatus> requestStatusSet,
            DateRange creationTime, DateRange finishTime, List<UUID> contractorIds, List<UUID> departmentsIds
                                              ) {
        return (root, query, builder) -> {
            var predicate = builder.isTrue(builder.literal(true));
            
            predicate = addSearchTextPredicate(searchText, root, builder, predicate);
            predicate = addStateNumberPredicate(stateNumber, root, builder, predicate);
            predicate = addOrganizationPredicate(organizationId, root, builder, predicate);
            predicate = addStatusesPredicate(requestStatusSet, root, builder, predicate);
            predicate = addCreationTimePredicate(creationTime, root, builder, predicate);
            predicate = addFinishTimePredicate(finishTime, root, builder, predicate);
            predicate = addContractorIds(contractorIds, root, builder, predicate);
            predicate = addDepartmentsIds(departmentsIds, root, builder, predicate);
            return predicate;
        };
    }
    
    private static Predicate addSearchTextPredicate(String searchText, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate) {
        if (StringUtils.hasText(searchText)) {
            predicate = builder.and(predicate, builder.or(
                                            builder.like(builder.upper(root.get(Ewb_.TRANSPORT).get(Transport_.STATE_NUMBER)),
                                                         "%" + searchText.toUpperCase() + "%"),
                                            builder.like(builder.upper(root.get(Ewb_.HUMAN_READABLE_ID)),
                                                         "%" + searchText.toUpperCase() + "%"))
                                   );
        }
        return predicate;
    }
    
    private static Predicate addStateNumberPredicate(String stateNumber, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate) {
        if (StringUtils.hasText(stateNumber)) {
            predicate = builder.and(predicate, builder.like(builder.upper(root.get(Ewb_.TRANSPORT).get(Transport_.STATE_NUMBER)),
                                                            "%" + stateNumber.toUpperCase() + "%")
                                   );
        }
        return predicate;
    }
    
    private static Predicate addOrganizationPredicate(UUID organizationId, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate) {
        if (Objects.nonNull(organizationId)) {
            predicate = builder.and(predicate, builder.equal(root.get(Ewb_.ORGANIZATION).get(Organization_.ID), organizationId));
        }
        return predicate;
    }
    
    private static Predicate addStatusesPredicate(Set<EwbStatus> statusSet, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate) {
        if (CollectionUtils.isNotEmpty(statusSet)) {
            predicate = builder.and(predicate, root.get(Ewb_.STATUS).in(statusSet));
        }
        return predicate;
    }
    
    private static Predicate addCreationTimePredicate(DateRange creationTime, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate) {
        if (Objects.nonNull(creationTime)) {
            predicate = builder.and(predicate, builder.between(root.get(Ewb_.CREATION_TIME), creationTime.start(), creationTime.end()));
        }
        return predicate;
    }
    
    private static Predicate addFinishTimePredicate(DateRange finishTime, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate) {
        if (Objects.nonNull(finishTime)) {
            predicate = builder.and(predicate, builder.between(root.get(Ewb_.FINISH_DATE), finishTime.start(), finishTime.end()));
        }
        return predicate;
    }
    
    private static Predicate addContractorIds(List<UUID> ids, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate){
        if (ids != null && !ids.isEmpty()){
            var organization = root.join(Ewb_.ORGANIZATION);
            predicate = builder.and(predicate, organization.get(Organization_.CONTRACTOR_EXTERNAL_ID).in(ids));
        }
        return predicate;
    }
    
    private static Predicate addDepartmentsIds(List<UUID> ids, Root<Ewb> root, CriteriaBuilder builder, Predicate predicate){
        if (ids != null && !ids.isEmpty()){
            predicate = builder.and(predicate, root.get(Ewb_.TARIFF_DEPARTMENT_ID).in(ids));
        }
        return predicate;
    }
}
