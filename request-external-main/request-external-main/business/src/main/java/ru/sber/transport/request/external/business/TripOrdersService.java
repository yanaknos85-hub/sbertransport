package ru.sber.transport.request.external.business;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import ru.sber.transport.request.external.business.exception.BusinessException;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.FileData;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;

/**
 * Заявки на поездки
 */
public interface TripOrdersService {

    /**
     * Уведомление пользователей о необработанных заявках
     */
    void remind();

    /**
     * Создает новую заявку на поездку
     *
     * @param passengerId идентификатор пассажира
     * @param order      исходные данные для создания заявки на поездку
     * @return новая заявка на поездку
     */
    TripOrderData create(UUID passengerId, TripOrderCreateDTO order);

    /**
     * Удаляет заявку на поездку
     *
     * @param organizationId идентификатор организации
     * @param id             идентификатор заявки на поездку
     */
    void delete(UUID organizationId, UUID id);

    /**
     * Редактирует заявку на поездку
     *
     * @param userId         идентификатор пользователя, изменяющего заявку
     * @param force          принудительное редактирование заявки
     * @param organizationId идентификатор организации
     * @param id             идентификатор заявки на поездку
     * @param newData        новые данные заявки на поездку
     * @param editedFields   список измененных полей
     */
    void edit(UUID userId, boolean force, UUID organizationId, @NonNull UUID id, @NonNull EditTripOrderData newData, @NonNull Set<String> editedFields);

    /**
     * Прикрепляет файл к заявке на поездку
     *
     * @param organizationId идентификатор организации
     * @param requestId      идентификатор заявки на поездку
     * @param fileName       имя файла
     * @param contentType    тип файла
     * @param path           путь к файлу
     * @throws BusinessException в случае ошибки бизнес слоя
     */
    void attachFile(UUID organizationId, UUID requestId, String fileName, String contentType, Path path) throws BusinessException;

    /**
     * Получает файлы заявки на поездку
     *
     * @param organizationId идентификатор организации
     * @param requestId      идентификатор заявки на поездку
     * @param from           индекс байта для начала чтения фрагмента
     * @param to             индекс байта для конца чтения фрагмента или -1 для чтения до конца файла
     * @return ответ с файлами заявки на поездку с признаком конца файла и путем к файлу
     */
    FileData getFile(UUID organizationId, UUID requestId, int from, int to);

    /**
     * Удаляет файлы заявки на поездку
     *
     * @param organizationId идентификатор организации
     * @param requestId      идентификатор заявки на поездку
     */
    void deleteFile(UUID organizationId, UUID requestId);

    /**
     * Отправляет сообщение о фроде для заявки
     *
     * @param order заявка на поездку
     */
    void sendFraud(TripOrderData order);
}
