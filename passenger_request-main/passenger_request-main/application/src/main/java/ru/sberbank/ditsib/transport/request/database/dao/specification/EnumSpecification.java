package ru.sberbank.ditsib.transport.request.database.dao.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import ru.sberbank.ditsib.transport.request.database.model.Request;

import java.io.Serial;
import java.util.EnumSet;

/**
 * Specification class for constructing search requests with criteria Enum case
 */
public class EnumSpecification extends BasicSpecification {

    @Serial
    private static final long serialVersionUID = 6942940467030501156L;
    
    public EnumSpecification(SearchCriteria searchCriteria) {
        super(searchCriteria);
    }
    
    @Override
    public Predicate toPredicate(@NonNull Root<Request> root, @NonNull CriteriaQuery<?> criteriaQuery, @NonNull CriteriaBuilder cb) {
        if (Enum.class.isAssignableFrom(getSearchPath(searchCriteria.key(), root).getJavaType())
            && (searchCriteria.operation() == ComparisonType.IN)) {
                return root.get(searchCriteria.key()).in(((EnumSet<?>) searchCriteria.value()));
            
        }
        return cb.or();
    }
}
    
    

