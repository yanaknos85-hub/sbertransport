package ru.sber.transport.telemechanic.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.dto.driver.*;
import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;

import java.util.UUID;

public interface DriverService {
    
    void addDriver(AddDriverRequest addDriverRequest);
    
    void editDriver(UUID id, EditDriverRequest editDriverRequest, UUID userId);
    
    void deactivateDriver(UUID id);
    
    GetDriverResponse getDriverById(UUID id);
    
    /**
     * Поиск водителей по ФИО с пагинацией
     *
     * @param request ФИО водителя и пагинация {@link DriverByFioRequest}
     * @param userId идентификатор пользователя {@link UUID}
     *
     * @return список водителей {@link DriverByFioResponse}
     */
    Page<DriverByFioResponse> getDriversByFio(DriverByFioRequest request, UUID userId);
    
    /**
     * Поиск водителей по фильтру с сортировкой и пагинацией
     *
     * @param request фильтры и сортировка {@link DriverSearchRequest}
     *
     * @return список водителей {@link DriverSearchResponse}
     */
    Page<DriverSearchResponse> search(DriverSearchRequest request);
    
    void deactivateDrivers();
    
    Driver getActiveDriverById(UUID id);
    
    /**
     * Получить водителя по идентификатору сотрудника
     *
     * @param employeeId идентификатор сотрудника
     *
     * @return водитель {@link Driver}
     */
    Driver getByEmployeeId(UUID employeeId);
    
    void saveMessage(DriverMessage driverMessage);
    
    /**
     * Поиск водителей по фильтрам
     * @param filters фильтры
     * @return страница водителей
     */
    Page<DriverByFioResponse> getDrivers(DriverFilters filters);
}
