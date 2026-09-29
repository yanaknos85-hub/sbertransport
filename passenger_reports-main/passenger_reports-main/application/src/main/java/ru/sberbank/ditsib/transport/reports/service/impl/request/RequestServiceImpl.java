package ru.sberbank.ditsib.transport.reports.service.impl.request;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.dto.SortDirection;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.exception.ConditionalForExportReportError;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Request_;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.RequestService;
import ru.sberbank.ditsib.transport.reports.service.RequestServiceStaticHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Transactional
@Service
@AllArgsConstructor
class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final DepartmentService departmentService;
    private final EntityManager em;
    private final FindTaxiRequestsUseCase findTaxiRequestsUseCase;
    private final FindPersonalRequestsUseCase findPersonalRequestsUseCase;
    private final FindCarsharingRequestsUseCase findCarsharingRequestsUseCase;
    private final FindPublicRequestsUseCase findPublicRequestsUseCase;
    private final FindGroupTransferRequestsUseCase findGroupTransferRequestsUseCase;

    @Override
    public List<Request> findAllBySpec(Specification<Request> spec) {
        return findAllBySpec(spec, null, null, null, null).toList();
    }

    @Override
    public Page<Request> findAllBySpec(
            Specification<Request> spec, Integer size, Integer page,
            SortDirection direction, String field
    ) {
        if (size == null) {
            size = Integer.MAX_VALUE;
        }
        if (page == null) {
            page = 0;
        }
        var sortField = Optional.ofNullable(field).orElse(Request_.CREATION_TIME);
        var sort = Sort.by(Sort.Direction.valueOf(Optional.ofNullable(direction).orElse(SortDirection.DESC).name()), sortField);
        var pageRequest = PageRequest.of(page, size, sort);
        return requestRepository.findAll(spec, pageRequest);
    }


    @Override
    public Page<TaxiResponseDTO> findTaxiRequests(RequestForTaxiReportDTO requestDTO) {
        return findTaxiRequestsUseCase.findTaxiRequests(requestDTO);
    }

    @Override
    public Page<PersonalResponseDTO> findPersonalRequests(RequestForPersonalReportDTO requestDTO) {
        return findPersonalRequestsUseCase.findPersonalRequests(requestDTO);
    }

    @Override
    public Page<CarsharingResponseDTO> findCarsharingRequests(RequestForCarsharingReportDTO requestDTO) {
        return findCarsharingRequestsUseCase.findCarsharingRequests(requestDTO);
    }

    @Override
    public Page<PublicResponseDTO> findPublicRequests(RequestForPublicReportDTO requestDTO) {
        return findPublicRequestsUseCase.findPublicRequests(requestDTO);
    }

    @Override
    public Page<GroupTransferResponseDTO> findGroupTransferRequests(RequestForGroupTransferReportDTO requestDTO) {
        return findGroupTransferRequestsUseCase.findGroupTransferRequests(requestDTO);
    }

    @Override
    public List<Request> findPersonalRequestsForPaymentAndAggregation(RequestForPersonalReportDTO requestDTO) {
        List<String> objList =
                RequestServiceStaticHelper.prepareFindPersonalPaymentRequestsQuery(em, departmentService, requestDTO).getResultList();
        var ids = objList.stream().map(UUID::fromString).toList();
        return requestRepository.findAllByIdIn(ids);
    }

    @Override
    public List<Request> findPublicRequestsForPaymentAndAggregation(RequestForPublicReportDTO requestDTO) {
        List<String> objList =
                RequestServiceStaticHelper.prepareFindPublicPaymentRequestsQuery(em, departmentService, requestDTO).getResultList();
        var ids = objList.stream().map(UUID::fromString).toList();
        return requestRepository.findAllByIdIn(ids);
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
    public List<Request> save(List<Request> request) {
        return requestRepository.saveAll(request);
    }
    
}
