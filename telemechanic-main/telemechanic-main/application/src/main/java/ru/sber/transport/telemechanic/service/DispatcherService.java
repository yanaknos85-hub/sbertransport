package ru.sber.transport.telemechanic.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.telemechanic.database.model.Dispatcher;
import ru.sber.transport.telemechanic.dto.dispatcher.*;
import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;

import java.util.UUID;

public interface DispatcherService {
    
    void addDispatcher(AddDispatcherRequest addDispatcherRequest);
    
    void editDispatcher(UUID dispatcherId, EditDispatcherRequest editDispatcherRequest);
    
    GetDispatcherResponse getDispatcher(UUID dispatcherId);
    
    void deactivateDispatcher(UUID dispatcherId);
    
    Page<GetDispatcherResponse> search(SearchDispatcherRequest searchDispatcherRequest);
    
    void deactivateDispatchers();
    
    /**
     * Получение данных по организации диспетчера (с проверкой возможности создавать ЭПЛ)
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link GetOrganizationDispatcherResponse}
     */
    GetOrganizationDispatcherResponse getSelfOrganizationInfo(UUID userId);
    
    Dispatcher getByEmployeeIdAndActive(UUID employeeId);
    
    void saveMessage(DispatcherMessage message);
}
