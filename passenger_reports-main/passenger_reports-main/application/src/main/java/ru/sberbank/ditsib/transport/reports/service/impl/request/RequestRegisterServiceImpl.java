package ru.sberbank.ditsib.transport.reports.service.impl.request;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.service.RequestRegisterService;

@Slf4j
@Transactional
@Service
@AllArgsConstructor
class RequestRegisterServiceImpl implements RequestRegisterService {
    
    private final FindTaxiRequestsUseCase findTaxiRequestsUseCase;
    private final FindPersonalRequestsUseCase findPersonalRequestsUseCase;
    private final FindCarsharingRequestsUseCase findCarsharingRequestsUseCase;
    private final FindPublicRequestsUseCase findPublicRequestsUseCase;
    private final FindGroupTransferRequestsUseCase findGroupTransferRequestsUseCase;
    
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
}
