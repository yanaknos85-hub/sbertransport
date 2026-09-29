package ru.sberbank.ditsib.transport.request.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис поиска по запросам
 */
public interface RequestSearchService {

    /**
     * Get all requests by search dto object
     * @param requestSearchDTO requests container
     *
     * @return list of requests.
     */
    Page<RequestForTaxi> taxiSearch(RequestTaxiSearchDTO requestSearchDTO);
    
    
    /**
     * Поиск заявок
     * @param requestSearchDTO поисковый фильтр
     *
     * @return список заявок
     */
    Page<Request> generalSearch(RequestSearchDTO requestSearchDTO, Pageable pageable);
    
    
    /**
     * * Поиск заявок с пейджингом
     * @param requestSearchDTO поисковый фильтр
     *
     * @return список заявок
     */
    Page<RequestForTaxi> taxiSearch(RequestTaxiSearchDTO requestSearchDTO, Pageable pageable);
    
    
    /**
     * * Get all requests by personal search dto
     * @param requestSearchDTO requests container
     *
     * @return list of requests.
     */
    Page<RequestForPersonal> personalSearch(RequestPersonalSearchDTO requestSearchDTO);
    
    
    
    /**
     * * Get all requests by public search dto
     * @param requestSearchDTO requests container
     *
     * @return list of requests.
     */
    Page<RequestForPublic> publicSearch(RequestPublicSearchDTO requestSearchDTO);
    
    /**
     * * Get all requests by carsharing search dto
     * @param requestSearchDTO requests container
     *
     * @return list of requests.
     */
    Page<RequestForCarsharing> carsharingSearch(RequestCarsharingSearchDTO requestSearchDTO);

    List<Request> getBySearchParam(Optional<UUID> authorId, Optional<UUID> passengerId, Optional<UUID> approvedById,
                                                         Optional<Boolean> approvedFlag, Optional<Boolean> coopTrip, Optional<String> comment,
                                                         Optional<UUID> requestId, Optional<Set<Integer>> statuses, Optional<LocalDateTime> dateFrom,
                                                         Optional<LocalDateTime> dateTo, Optional<Double> minRideCost, Optional<Double> maxRideCost,
                                                         Optional<Set<UUID>> tripPurposes, Optional<String> fio,
                                                         Employee authenticated);
}
