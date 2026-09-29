package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.SortDirection;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис работы с заявками.
 */
public interface RequestService {
    
    /**
     * Поиск заявок по фильтрам
     *
     * @param spec
     *
     * @return
     */
    List<Request> findAllBySpec(Specification<Request> spec);
    
    /**
     * Поиск заявок по фильтрам
     *
     * @param spec
     *
     * @return
     */
    Page<Request> findAllBySpec(
            Specification<Request> spec, Integer size, Integer page,
            SortDirection direction, String field
                               );
    
    /**
     * Поиск заявок по фильтрам c пагинацией
     *
     * @return
     */
    Page<TaxiResponseDTO> findTaxiRequests(RequestForTaxiReportDTO requestDTO);
    
    /**
     * Поиск заявок личного транспорта
     *
     * @return
     */
    Page<PersonalResponseDTO> findPersonalRequests(RequestForPersonalReportDTO requestDTO);
    
    /**
     * Поиск заявок общественного транспорта
     *
     * @return
     */
    Page<PublicResponseDTO> findPublicRequests(RequestForPublicReportDTO requestDTO);
    
    /**
     * Поиск заявок каршеринга
     *
     * @return
     */
    Page<CarsharingResponseDTO> findCarsharingRequests(RequestForCarsharingReportDTO requestDTO);
    
    /**
     * Поиск заявок группового трансфера
     *
     * @return
     */
    Page<GroupTransferResponseDTO> findGroupTransferRequests(RequestForGroupTransferReportDTO requestDTO);
    
    /**
     * Поиск заявок личного транспорта (выплата и агрегированного)
     *
     * @return
     */
    List<Request> findPersonalRequestsForPaymentAndAggregation(RequestForPersonalReportDTO requestDTO);
    
    /**
     * Поиск заявок общественного транспорта (выплата и агрегированного)
     *
     * @return
     */
    List<Request> findPublicRequestsForPaymentAndAggregation(RequestForPublicReportDTO requestDTO);
    
    
    /**
     * Поиск заявок по id
     *
     * @param id
     *
     * @return
     */
    Optional<Request> findById(UUID id);
    
    Request save(Request request);
    
    List<Request> save(List<Request> request);
}
