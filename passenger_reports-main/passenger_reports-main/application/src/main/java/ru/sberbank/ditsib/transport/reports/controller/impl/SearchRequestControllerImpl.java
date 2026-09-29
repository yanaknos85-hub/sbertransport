package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.reports.controller.SearchRequestController;
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
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import jakarta.validation.Valid;

@RestController
@E2EController
@RequiredArgsConstructor
@Slf4j
public class SearchRequestControllerImpl implements SearchRequestController {
    private final RequestService requestServiceImpl;
    
    @Override
    public Page<TaxiResponseDTO> getTaxi(@Valid RequestForTaxiReportDTO requestDTO) {
        return requestServiceImpl.findTaxiRequests(requestDTO);
    }
    
    @Override
    public Page<PersonalResponseDTO> getPersonal(@Valid RequestForPersonalReportDTO requestSearchDTO) {
        return requestServiceImpl.findPersonalRequests(requestSearchDTO);
    }
    
    @Override
    public Page<PublicResponseDTO> getPublic(@Valid RequestForPublicReportDTO requestSearchDTO) {
        return requestServiceImpl.findPublicRequests(requestSearchDTO);
    }
    
    @Override
    public Page<CarsharingResponseDTO> getСarsharing(@Valid RequestForCarsharingReportDTO requestSearchDTO) {
        return requestServiceImpl.findCarsharingRequests(requestSearchDTO);
    }

    @Override
    public Page<GroupTransferResponseDTO> getGroupTransferReport(@Valid RequestForGroupTransferReportDTO requestSearchDTO) {
        return requestServiceImpl.findGroupTransferRequests(requestSearchDTO);
    }
    
}