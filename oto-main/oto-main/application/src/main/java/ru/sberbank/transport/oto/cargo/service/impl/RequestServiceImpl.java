package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.RequestRepository;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.database.model.Request_;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;
import ru.sberbank.transport.oto.cargo.service.RequestService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис работы с заявками.
 */
@Service
@AllArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
  
  private final RequestRepository requestRepository;
  
  @PersistenceContext
  private final EntityManager entityManager;
  
  @Override
  public List<Request> findAllBySpec(Specification<Request> spec) {
    return findAllBySpec(spec, 100, 0, SortDirection.DESC, Request_.CREATION_TIME).getContent();
  }
  
  @Override
  public Page<Request> findAllBySpec(
          Specification<Request> spec, Integer size, Integer page,
          SortDirection direction, String field
                                    ) {
    var sort = Sort.by(Sort.Direction.valueOf(Optional.of(direction).orElse(SortDirection.DESC).name()), field);
    var pageRequest = PageRequest.of(page, size, sort);
    return requestRepository.findAll(spec, pageRequest);
  }
  
  @Override
  public Optional<Request> findById(UUID id) {
    return requestRepository.findById(id);
  }
  
  @Override
  public Request save(Request request) {
    return requestRepository.save(request);
  }
  
  @Override
  public Page<Request> findAllForCargo(
          Specification<Request> spec, Integer size, Integer page,
          SortDirection direction, String field
                                      ) {
    
    CriteriaBuilder b = entityManager.getCriteriaBuilder();
    CriteriaQuery<UUID> q = b.createQuery(UUID.class);
    Root<Request> root = q.from(Request.class);
    
    size = Optional.ofNullable(size).orElse(Integer.MAX_VALUE);
    page = Optional.ofNullable(page).orElse(0);
    var sortField = Optional.ofNullable(field).orElse(Request_.CREATION_TIME);
    var sort = Sort.by(Sort.Direction.valueOf(Optional.ofNullable(direction).orElse(SortDirection.DESC).name()),
                       sortField);
    
    q.select(root.get(Request_.id));
    q.where(spec.toPredicate(root, q, b));
    
    List<UUID> ids = entityManager.createQuery(q)
                                  .setMaxResults(size)
                                  .setFirstResult(page * size)
                                  .getResultList();
    
    CriteriaQuery<Long> countQuery = b.createQuery(Long.class);
    Root<Request> countRoot = countQuery.from(Request.class);
    countQuery.select(b.count(countRoot))
              .where(spec.toPredicate(countRoot, q, b));
    
    var total = entityManager.createQuery(countQuery)
                             .getSingleResult();
    
    List<Request> requests = Optional.of(ids)
                                     .filter(CollectionUtils::isNotEmpty)
                                     .map(i -> requestRepository.findAllByIdForCargo(i, sort))
                                     .orElseGet(List::of);
    
    return new PageImpl<>(requests,
                          PageRequest.of(page, size),
                          total);
  }
  
}
