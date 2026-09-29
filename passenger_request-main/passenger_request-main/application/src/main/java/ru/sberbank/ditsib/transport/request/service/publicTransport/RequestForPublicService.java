package ru.sberbank.ditsib.transport.request.service.publicTransport;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.RequestProjection;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.NewRequestForCompensationDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.NewRequestForPublicDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.RequestForCompensationDTO;

import java.util.List;
import java.util.UUID;

/**
 * Методы для Запросов на компенсацию за общественный транспорт
 */
public interface RequestForPublicService {

    /**
     * Создание Заявки на компенсацию за проезд на общественном транспорте
     * @param authentication данные об инициаторе заявки
     * @param data данные создаваемой заявки
     * @return
     */
    RequestForCompensationDTO addCompensationRequest(JwtAuthenticationToken authentication, NewRequestForCompensationDTO data);

    /**
     * Получение заявки на компенсацию за проезд на общественном транспорте
     * @param requestId идентификатор заявки
     * @return
     */
    RequestForCompensationDTO getRequestForPublicCompensation(UUID requestId, RequestProjection projection);

    /**
     * Редактирование еще не согласованной Заявки на компенсацию для общественного транспорта
     * @param requestId ID заявки
     * @param initiator инициатор заявки
     * @param data изменяемые данные
     */
    void edit(UUID requestId, Employee initiator, NewRequestForPublicDTO data);
    
    /**
     * Редактирование еще не согласованной Заявки на компенсацию для общественного транспорта
     * @param requestId ID заявки
     * @param initiator инициатор заявки
     * @param newDocuments документы, подтверждающие оплату
     */
    void confirm(UUID requestId, Employee initiator,  List<CompensationDocumentDTO> newDocuments);
}
