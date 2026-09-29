package ru.sberbank.ditsib.transport.request.service.impl.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.dao.specification.BasicSpecification;
import ru.sberbank.ditsib.transport.request.database.dao.specification.ComparisonType;
import ru.sberbank.ditsib.transport.request.database.dao.specification.EnumSpecification;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Delegate;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.service.DelegateService;
import ru.sberbank.ditsib.transport.request.service.search.RequestSearchService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of request search service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class RequestSearchServiceImpl implements RequestSearchService {
    
    private static final String STATUS = "status";
    
    private final DelegateService delegateService;

    private final RequestGeneralSearchServiceImpl requestGeneralSearchService;
    private final RequestSearchSpecForTaxiImpl requestSearchSpecForTaxi;
    private final RequestSearchSpecForPersonalImpl requestSearchSpecForPersonal;
    private final RequestSearchSpecForPublicImpl requestSearchSpecForPublic;
    private final RequestSearchSpecForCarsharingImpl requestSearchSpecForCarsharing;
    
    private final RequestRepository repository;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final RequestForPersonalRepository requestForPersonalRepository;
    private final RequestForPublicRepository requestForPublicRepository;
    private final RequestForCarsharingRepository requestForCarsharingRepository;

    @Override
    public Page<RequestForPublic> publicSearch(RequestPublicSearchDTO requestSearchDTO) {
        Specification<RequestForPublic> spec = requestSearchSpecForPublic.getSpeForPublic(requestSearchDTO);
        return requestForPublicRepository.findAll(spec, RequestSearchDTO.getPageRequest(requestSearchDTO));
    }
    
    @Override
    public Page<RequestForCarsharing> carsharingSearch(RequestCarsharingSearchDTO requestSearchDTO) {
        Specification<RequestForCarsharing> spec = requestSearchSpecForCarsharing.getSpecForCarsharing(requestSearchDTO);
        return requestForCarsharingRepository.findAll(spec, RequestSearchDTO.getPageRequest(requestSearchDTO));
    }
    
    @Override
    public Page<RequestForPersonal> personalSearch(RequestPersonalSearchDTO requestSearchDTO) {
        Specification<RequestForPersonal> spec = requestSearchSpecForPersonal.getSpecForPersonal(requestSearchDTO);
        return requestForPersonalRepository.findAll(spec, RequestSearchDTO.getPageRequest(requestSearchDTO));
    }
    
    @Override
    public Page<RequestForTaxi> taxiSearch(RequestTaxiSearchDTO requestSearchDTO) {
        return taxiSearch(requestSearchDTO, RequestSearchDTO.getPageRequest(requestSearchDTO));
    }
    
    @Override
    public Page<Request> generalSearch(RequestSearchDTO requestSearchDTO, Pageable pageable) {
        var spec = requestGeneralSearchService.getSpec(requestSearchDTO);
        return repository.findAll(spec,pageable);
    }
    
    @Override
    public Page<RequestForTaxi> taxiSearch(RequestTaxiSearchDTO requestSearchDTO, Pageable pageable) {
        Specification<RequestForTaxi> spec = requestSearchSpecForTaxi.getSpecForTaxi(requestSearchDTO);
    
        return requestForTaxiRepository.findAll(spec, pageable);
    }
    
    

    /**
     * Поиск запросов по поисковым параметрам.
     *
     * @return список запросов.
     */
    @Override
    public List<Request> getBySearchParam(
            Optional<UUID> authorId, Optional<UUID> passengerId, Optional<UUID> approvedById,
            Optional<Boolean> approvedFlag, Optional<Boolean> coopTrip, Optional<String> comment,
            Optional<UUID> requestId, Optional<Set<Integer>> statuses, Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo, Optional<Double> minRideCost, Optional<Double> maxRideCost,
            Optional<Set<UUID>> tripPurposes, Optional<String> fio, Employee authenticated
                                         ) {
        
        var searchCriteriaList = new ArrayList<BasicSpecification.SearchCriteria>();
        approvedById.ifPresent(
                uuid -> searchCriteriaList
                        .add(new BasicSpecification.SearchCriteria("approvedBy.id", ComparisonType.EQUALS, uuid)));
        coopTrip.ifPresent(
                value -> searchCriteriaList
                        .add(new BasicSpecification.SearchCriteria("coopTrip", ComparisonType.EQUALS, value)));
        
        addAdditionalCriteria(searchCriteriaList, requestId, fio, statuses, approvedFlag, comment, authorId,
                              passengerId, dateFrom, dateTo, minRideCost, maxRideCost, tripPurposes);
        
        //Получение заявок на основании делегирования
        
        List<Request> result = new LinkedList<>();
        var delegateRecordDTOS = delegateService.getEmployeeDelegateRecords(authenticated);
        if (delegateRecordDTOS != null && !delegateRecordDTOS.isEmpty()) {
            var recordDTO = delegateRecordDTOS.get(0);
            var searchCriteriaDelegatedList = new ArrayList<BasicSpecification.SearchCriteria>();
            searchCriteriaDelegatedList
                    .add(new BasicSpecification.SearchCriteria("approvedBy.id", ComparisonType.EQUALS,
                                                               recordDTO.getSupervisorId()));
            
            searchCriteriaDelegatedList.add(new BasicSpecification.SearchCriteria("transportType", ComparisonType.IN,
                                                                                  EnumSet.copyOf(
                                                                                          (delegateRecordDTOS.stream()
                                                                                                             .map(
                                                                                                                     Delegate::getTransportType)
                                                                                                             .collect(
                                                                                                                     Collectors
                                                                                                                             .toSet())))));
            addAdditionalCriteria(searchCriteriaDelegatedList, requestId, fio, statuses, approvedFlag, comment,
                                  authorId, passengerId, dateFrom, dateTo, minRideCost, maxRideCost, tripPurposes);
            if (!searchCriteriaDelegatedList.isEmpty()) {
                result.addAll(search(searchCriteriaDelegatedList));
            }
        }
        if (!searchCriteriaList.isEmpty()) {
            result.addAll(search(searchCriteriaList));
        }
        return result;
    }

    
    /**
     * Поиск данных.
     *
     * @param searchCriteriaList список критериев для поиска.
     * @return коллекция заявок.
     */
    private Collection<? extends Request> search(List<BasicSpecification.SearchCriteria> searchCriteriaList) {
        Specification<Request> all = new BasicSpecification(null);
        for (BasicSpecification.SearchCriteria searchCriteria : searchCriteriaList) {
            if (searchCriteria.value() instanceof String
                || searchCriteria.value() instanceof UUID
                || searchCriteria.value() instanceof Boolean
                || searchCriteria.value() instanceof LocalDateTime
                || searchCriteria.value() instanceof Double
                || searchCriteria.value() instanceof Integer) {
                all = all.and(new BasicSpecification(searchCriteria));
            } else if (searchCriteria.value() instanceof EnumSet) {
                all = all.and(new EnumSpecification(searchCriteria));
            }
        }
        return repository.findAll(all, Sort.by(Sort.Direction.DESC, "desiredDate"));
    }
    
    /**
     * Дополнительные ограничения по поиску заявок. Эти ограничения общие для обычных заявок и делегированных.
     */
    private void addAdditionalCriteria(
            List<BasicSpecification.SearchCriteria> searchCriteriaList, Optional<UUID> requestId, Optional<String> fio,
            Optional<Set<Integer>> statuses, Optional<Boolean> approvedFlag, Optional<String> comment,
            Optional<UUID> authorId, Optional<UUID> passengerId, Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo, Optional<Double> minRideCost, Optional<Double> maxRideCost,
            Optional<Set<UUID>> tripPurposes
                                      ) {
        requestId.ifPresent(
                uuid -> searchCriteriaList
                        .add(new BasicSpecification.SearchCriteria("id", ComparisonType.EQUALS, uuid)));
        statuses.ifPresent(
                statusSet -> searchCriteriaList.add(new BasicSpecification.SearchCriteria(STATUS, ComparisonType.IN,
                                                                                          TripRequestStatus
                                                                                                  .getSetOfIntegerCollection(
                                                                                                          statusSet))));
        comment.ifPresent(searchString -> searchCriteriaList
                .add(new BasicSpecification.SearchCriteria("commentForDriver", ComparisonType.EQUALS, searchString)));
        authorId.ifPresent(
                uuid -> searchCriteriaList
                        .add(new BasicSpecification.SearchCriteria("author.id", ComparisonType.EQUALS, uuid)));
        passengerId.ifPresent(
                uuid -> searchCriteriaList
                        .add(new BasicSpecification.SearchCriteria("passenger.id", ComparisonType.EQUALS, uuid)));
        if (approvedFlag.isPresent()) {
            if (Boolean.TRUE.equals(approvedFlag.get())) {
                searchCriteriaList.add(new BasicSpecification.SearchCriteria(STATUS, ComparisonType.IN,
                                                                             TripRequestStatus
                                                                                     .getSetOfGreaterOrEqualToStatus(
                                                                                             TripRequestStatus.TAXI_APPROVED)));
            } else {
                searchCriteriaList.add(new BasicSpecification.SearchCriteria(STATUS, ComparisonType.IN, TripRequestStatus
                        .getSetOfLesserWithoutDraft(TripRequestStatus.TAXI_APPROVED)));
            }
        }
        dateFrom.ifPresent(
                date -> searchCriteriaList.add(
                        new BasicSpecification.SearchCriteria("creationTime", ComparisonType.GREATER_THAN_OR_EQUALS,
                                                              date)));
        dateTo.ifPresent(
                date -> searchCriteriaList.add(
                        new BasicSpecification.SearchCriteria("creationTime", ComparisonType.LESS_THAN_OR_EQUALS,
                                                              date)));
        minRideCost.ifPresent(
                price -> searchCriteriaList.add(
                        new BasicSpecification.SearchCriteria("expected.cost", ComparisonType.GREATER_THAN_OR_EQUALS,
                                                              price)));
        maxRideCost.ifPresent(
                price -> searchCriteriaList.add(
                        new BasicSpecification.SearchCriteria("expected.cost", ComparisonType.LESS_THAN_OR_EQUALS,
                                                              price)));
        tripPurposes.ifPresent(
                purpose -> searchCriteriaList.add(
                        new BasicSpecification.SearchCriteria("purpose.id", ComparisonType.IN, purpose)));
        if (fio.isPresent()) {
            var splittedFio = fio.get().split(" ");
            var lastNameField = "passenger.lastName";
            var firstNameField = "passenger.firstName";
            var patronymicField = "passenger.patronymic";
            
            switch (splittedFio.length) {
                case 1 -> searchCriteriaList
                            .add(new BasicSpecification.SearchCriteria(lastNameField, ComparisonType.EQUALS,
                                                                       splittedFio[0]));
                case 2 -> {
                    searchCriteriaList
                            .add(new BasicSpecification.SearchCriteria(lastNameField, ComparisonType.EQUALS,
                                                                       splittedFio[0]));
                    searchCriteriaList
                            .add(new BasicSpecification.SearchCriteria(firstNameField, ComparisonType.EQUALS,
                                                                       splittedFio[1]));
                }
                default -> {
                    searchCriteriaList
                            .add(new BasicSpecification.SearchCriteria(lastNameField, ComparisonType.EQUALS,
                                                                       splittedFio[0]));
                    searchCriteriaList
                            .add(new BasicSpecification.SearchCriteria(firstNameField, ComparisonType.EQUALS,
                                                                       splittedFio[1]));
                    searchCriteriaList
                            .add(new BasicSpecification.SearchCriteria(patronymicField, ComparisonType.EQUALS,
                                                                       splittedFio[2]));
                }
            }
        }
    }
}
