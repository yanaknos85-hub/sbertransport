package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для работы с Заявками на подключение к корп.каршерингу
 */
public interface CarsharingJoinRequestService {
    
    /**
     * Получение всех подключенных и неподключенных корп.каршерингов для сотрудника корп.клиента
     * @param calculatedList список CalculatedDto для каршеринга, получаемый из расчета по тарифам
     * @param organizationId ID корп.клиента
     * @param employeeId ID сотрудника
     * @return список CarsharingJoinAndCalculatedDTO
     */
    List<CarsharingJoinAndCalculatedDTO> getCarsharingJoinsForEmployee(
            List<CalculatedDto> calculatedList, UUID organizationId, UUID employeeId);
    
    /**
     * Получить созданную заявку или пустой бланк для конкретного сотрудника
     * @param organizationId ID корп.клиента
     * @param employee данные сотрудника
     * @return созданная заявка или бланк для заполнения
     */
    GetCarsharingJoinRequestDTO getCreatedOrBlank(UUID organizationId, Employee employee);
    
    /**
     * Создать заявку на подключение
     * @param joinRequestDto данные нового подключения
     * @param organization данные корп.клиента
     * @param employee данные сотрудника
     * @return созданная заявка
     */
    GetCarsharingJoinRequestDTO create(NewCarsharingJoinRequestDTO joinRequestDto, Organization organization,
                                       Employee employee);
    
    /**
     * Получить заявку на подключение к корп.каршерингам по ID
     * @param joinRequestId ID заявки
     * @return заявка
     */
    CarsharingJoinRequest getById(UUID joinRequestId);
    
    /**
     * Получить DTO заявки на подключение к корп.каршерингам по ID
     * @param joinRequestId ID заявки
     * @return GetCarsharingJoinRequestDTO
     */
    GetCarsharingJoinRequestDTO getDtoById(UUID joinRequestId);
    
    /**
     * Получить все заявки на подключение к корп.каршерингам по статусу заявки
     * @param status статус заявки
     * @return все заявки на подключение
     */
    List<GetCarsharingJoinRequestShortDTO> getByStatus(CarsharingJoinRequestStatus status);
    
    /**
     * Обработать заявку вручную
     * @param processedDtos результат подключений
     * @param joinRequestId ID заявки
     * @param organization корп.клиент
     * @return GetCarsharingJoinRequestDTO
     */
    GetCarsharingJoinRequestDTO process(Set<ProcessedContractorAndJoinStatusDTO> processedDtos, UUID joinRequestId,
                                        Organization organization);
    
    /**
     * Редактировать заявку на подключение сотрудника к корп.каршерингам
     * @param joinRequestDto данные для изменения
     * @param joinRequestFromDb заявка из БД
     */
    void update(UpdateCarsharingJoinRequestDTO joinRequestDto, CarsharingJoinRequest joinRequestFromDb);
    
    /**
     * Отменить заявку на подключение сотрудника к корп.каршерингам
     * @param request заявка на подключение сотрудника к корп.каршерингам
     * @param cancelDTO объект с причиной и кодом отмены
     */
    void cancel(CarsharingJoinRequest request, CancelDTO cancelDTO);
    
    /**
     * Удалить заявку на подключение сотрудника к корп.каршерингам
     * @param joinRequestFromDb заявка из БД
     */
    void delete(CarsharingJoinRequest joinRequestFromDb);
}
