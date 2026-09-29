package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;

import java.util.UUID;

/**
 * Сервис для работы с текстовыми полями заявки на подключение к корп.каршерингу.
 * Для всей системы - единая форма заявки. Нельзя вручную создавать / удалять, только редактировать.
 * Добавляются по умолчанию перед первым обращением к БД.
 */
public interface CarsharingJoinRequestTextService {
    
    /**
     * Сохранить текстовые поля в БД
     * @param text CarsharingJoinRequestText заполненные тестовые поля
     * @return сохраненные текстовые поля
     */
    CarsharingJoinRequestText save(CarsharingJoinRequestText text);
    
    /**
     * Проверить наличие формы заявки в БД, если нет - создать по умолчанию
     * @param organizationId ID корп.клиента
     * @return CarsharingJoinRequestText
     */
    CarsharingJoinRequestText getOrCreateDefaults(UUID organizationId);
    
    /**
     * Проверить наличие формы заявки в БД, если нет - бросить исключение
     * @param organizationId ID корп.клиента
     * @return CarsharingJoinRequestText
     * @throws EntityNotFoundException
     */
    CarsharingJoinRequestText getOrThrowException(UUID organizationId) throws EntityNotFoundException;
    
    /**
     * Заменить созданные текстовые поля на умолчательные
     * @param organizationId ID корп.клиента
     * @return CarsharingJoinRequestText
     */
    CarsharingJoinRequestText setToDefaults(UUID organizationId);
}
