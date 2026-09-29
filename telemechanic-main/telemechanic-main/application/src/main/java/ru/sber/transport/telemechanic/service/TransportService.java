package ru.sber.transport.telemechanic.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.CreateRequestDto;
import ru.sber.transport.telemechanic.dto.transport.GetTransportRequest;
import ru.sber.transport.telemechanic.dto.transport.GetTransportResponse;
import ru.sber.transport.telemechanic.dto.transport.TransportSearchDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с транспортными средставами
 */
@Validated
public interface TransportService {
    
    /**
     * Получаем транспортное средство по идентификатору
     *
     * @param transportId Идентификатор транспортного средства
     *
     * @return {@link Transport}
     */
    Transport getTransportById(UUID transportId);
    
    /**
     * Получаем транспортное средство по идентификатору c загруженными организациями
     *
     * @param transportId Идентификатор транспортного средства
     *
     * @return {@link Transport}
     */
    Transport getTransportByIdWithOrganizations(UUID transportId);
    
    /**
     * Получаем транспортное средство по идентификатору
     *
     * @param id Идентификатор записи о транспортном средстве
     *
     * @return {@link Optional<Transport>}
     */
    Optional<Transport> get(UUID id);
    
    /**
     * Удаление транспортного средства
     *
     * @param entity {@link Transport}
     */
    void deactivate(Transport entity);
    
    /**
     * Сохраняем или обновляем транспортное средство
     *
     * @param entity {@link Transport}
     */
    void saveOrUpdate(@Valid Transport entity);
    
    /**
     * Поиск списка транспортных средств при помощи гос.номера в рамках организации
     *
     * @param request {@link GetTransportRequest}
     * @param employeeId идентификатор сотрудника
     *
     * @return {@link GetTransportResponse}
     */
    Page<GetTransportResponse> getTransportByStateNumberSelfOrganization(GetTransportRequest request, UUID employeeId);
    
    /**
     * Поиск списка транспортных средств при помощи гос.номера
     *
     * @param request {@link GetTransportRequest}
     *
     * @return {@link GetTransportResponse}
     */
    Page<GetTransportResponse> getTransportByStateNumberAllOrganizations(GetTransportRequest request);
    
    Transport getTransportInUseById(UUID transportId);
    
    /**
     * Поиск ТС по фильтрам
     * @param searchDto фильтры
     * @param authentication данные об авторизованом пользователе
     * @return страница тс
     */
    Page<GetTransportResponse> getTransportByFilters(TransportSearchDto searchDto, Authentication authentication);
}