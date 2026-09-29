package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.data.domain.Page;
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

import java.util.List;

/**
 * Сервис работы с заявками по реестрам
 */
public interface RequestRegisterService {
    
    /**
     * Поиск заявок по фильтрам c пагинацией
     */
    Page<TaxiResponseDTO> findTaxiRequests(RequestForTaxiReportDTO requestDTO);
    
    /**
     * Поиск заявок личного транспорта
     */
    Page<PersonalResponseDTO> findPersonalRequests(RequestForPersonalReportDTO requestDTO);
    
    /**
     * Поиск заявок общественного транспорта
     */
    Page<PublicResponseDTO> findPublicRequests(RequestForPublicReportDTO requestDTO);
    
    /**
     * Поиск заявок каршеринга
     */
    Page<CarsharingResponseDTO> findCarsharingRequests(RequestForCarsharingReportDTO requestDTO);
    
    /**
     * Поиск заявок группового трансфера
     */
    Page<GroupTransferResponseDTO> findGroupTransferRequests(RequestForGroupTransferReportDTO requestDTO);
}
