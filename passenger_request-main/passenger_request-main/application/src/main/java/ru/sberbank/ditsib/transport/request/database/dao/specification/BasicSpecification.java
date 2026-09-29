package ru.sberbank.ditsib.transport.request.database.dao.specification;

import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.request.database.model.Request;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Specification class for constructing search requests with criteria
 */
@RequiredArgsConstructor
public class BasicSpecification implements Specification<Request> {

    @Serial
    private static final long serialVersionUID = 5013862522781703973L;
    
    protected final SearchCriteria searchCriteria;
    
    public static <T> @NonNull Path<T> getSearchPath(@NotBlank String key, Root<Request> root) {
        Path<T> objectPath = null;
        if (!key.contains(".")) {
            objectPath = root.get(key);
        } else {
            for (String splitKey : key.split("\\.")) {
                if (objectPath == null) {
                    objectPath = root.get(splitKey);
                } else {
                    objectPath = objectPath.get(splitKey);
                }
            }
        }
        assert objectPath != null;
        return objectPath;
    }
    
    @Override
    public Predicate toPredicate(@NonNull Root<Request> root, @NonNull CriteriaQuery<?> criteriaQuery, @NonNull CriteriaBuilder cb) {
        if (searchCriteria == null || searchCriteria.key == null) {
            return cb.isTrue(cb.literal(true)); // always true = no filtering
        }
        
        var javaType = getSearchPath(searchCriteria.key(), root).getJavaType();
        
        if (searchCriteria.operation() == ComparisonType.EQUALS) {
            if (javaType == String.class) {
                return cb.like(cb.upper(getSearchPath(searchCriteria.key, root)),
                               "%" + searchCriteria.value().toString().toUpperCase(Locale.ROOT) + "%");
            }
            return cb.equal(getSearchPath(searchCriteria.key, root), searchCriteria.value());
        } else if (searchCriteria.operation() == ComparisonType.GREATER_THAN_OR_EQUALS) {
            if (javaType == LocalDateTime.class) {
                return cb.greaterThanOrEqualTo(getSearchPath(searchCriteria.key, root),
                                               (LocalDateTime) searchCriteria.value());
            }
            if (javaType == Integer.class || javaType == int.class) {
                return cb.greaterThanOrEqualTo(getSearchPath(searchCriteria.key, root),
                                               (Integer) searchCriteria.value());
            }
            if (javaType == Double.class || javaType == double.class) {
                return cb.greaterThanOrEqualTo(getSearchPath(searchCriteria.key, root),
                                               (Double) searchCriteria.value());
            }
            return cb.or();
        } else if (searchCriteria.operation() == ComparisonType.LESS_THAN_OR_EQUALS) {
            if (javaType == LocalDateTime.class) {
                return cb.lessThanOrEqualTo(getSearchPath(searchCriteria.key, root),
                                               (LocalDateTime) searchCriteria.value());
            }
            if (javaType == Integer.class || javaType == int.class) {
                return cb.lessThanOrEqualTo(getSearchPath(searchCriteria.key, root),
                                               (Integer) searchCriteria.value());
            }
            if (javaType == Double.class || javaType == double.class) {
                return cb.lessThanOrEqualTo(getSearchPath(searchCriteria.key, root),
                                            (Double) searchCriteria.value());
            }
            return cb.or();
        }
        return cb.equal(getSearchPath(searchCriteria.key, root), searchCriteria.value);
    }
    
    /**
     * Class with data to determine correct request type and construct criteria query
     */
    public record SearchCriteria(String key,
                                 ComparisonType operation,
                                 Object value) implements Serializable {
    }
}

