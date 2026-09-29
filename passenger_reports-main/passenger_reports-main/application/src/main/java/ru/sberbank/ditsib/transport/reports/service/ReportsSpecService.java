package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;

public interface ReportsSpecService<T extends RequestReportDTO> {
    /**
     * Получение фильтров спецификаций для поиска заявок в БД
     * @param requestDTO входной параметр относящийся к выбранному типу транспорта
     * @return спецификации заявок для типа транспорта transportType
     */
    Specification<Request> getReportSpec(T requestDTO);
    
    TransportTypeEnum transportType();
}
