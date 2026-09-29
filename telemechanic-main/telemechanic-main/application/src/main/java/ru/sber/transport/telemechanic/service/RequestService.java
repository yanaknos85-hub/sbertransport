package ru.sber.transport.telemechanic.service;


import org.springframework.data.domain.Page;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.RequestHistory;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.List;
import java.util.UUID;

public interface RequestService {
    Request createEmpty(CreateRequestDto createRequestDto, Employee authenticatedEmployee, boolean newPath);
    
    Request get(UUID requestId);
    
    ChecksTreeDto getChecksTreeByRequestId(UUID requestId);
    
    Request changeStatus(UUID requestId, RequestStatus newStatus, Employee authenticatedEmployee);
    
    void changeStatusForCallTelemechanic(UUID requestId, UUID userId);
    
    void setChecksStartedTime(UUID requestId);
    
    List<Request> getInStatusByAuthor(Employee employee, List<RequestStatus> statuses);
    
    RequestOnTheLineDto getOnTheLineRequest(Employee employee);
    
    ActiveResponse getInProgressRequest(UUID userId);
    
    void cancel(UUID requestId, Employee authenticatedEmployee);
    
    Page<MonitoringRequestListDto> search(RequestSearchDto requestSearchDTO, UUID userId);
    
    PatchMonitoringResponse update(UUID requestId, MonitoringRequestDto newData, UUID userId);
    
    List<RequestHistory> getStatusHistory(UUID requestId);
    
    /**
     * * Поиск заявок для реестра
     *
     * @param reportSearchDto поисковый фильтр
     *
     * @return список заявок
     */
    Page<Request> search(ReportSearchDto reportSearchDto);
    
    MonitoringRequestDto getForMonitoring(UUID requestId, UUID userId);
    
    /**
     * Закрытие заявки
     * @param requestId идентификатор заявки
     * @param userId идентификатор пользователя
     */
    void close(UUID requestId, UUID userId);
    
    /**
     * Метод для автоматического обновления статуса Тех заявки шедулером.
     */
    void statusAutoUpdate();
    
    /**
     * Метод для обновления Тех заявки при отправке третьего титула.
     *
     * @param request тех заявка
     * @param userId идентификатор инспектора
     */
    void updateRequestThirdTitleSent(Request request, UUID userId);
}
