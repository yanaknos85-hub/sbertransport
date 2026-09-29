package ru.sber.transport.business.providers;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.Page;
import ru.sber.transport.request.external.model.PriceData;
import ru.sber.transport.request.external.model.RequestFilter;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.request.external.providers.exceptions.DatabaseLayerException;

/**
 * Провайдер данных о заявках
 */
public interface TripOrdersProvider {

    /**
     * Создает заявку на поездку
     *
     * @param passengerId идентификатор пассажира
     * @param source      исходные данные для создания заявки на поездку
     * @param price       цена поездки
     * @param timeZone    тайм зона поездки
     * @return новая заявка на поездку
     */
    TripOrderData create(UUID passengerId, TripOrderCreateDTO source, PriceData price, String timeZone);

    /**
     * Получает отфильтрованный список заявок на поездку
     *
     * @param filter фильтр
     * @param page   страница
     * @param size   количество заявок на странице
     * @param sort   свойство по которому сортируется список заявок на поездку
     * @param asc    сортировка по возрастанию
     * @return список заявок на поездку
     */
    default Page<TripOrderData> get(RequestFilter filter, int page, int size, String sort, boolean asc) {
        return get(filter, Set.of(), page, size, sort, asc);
    }

    /**
     * Получает отфильтрованный список заявок на поездку
     *
     * @param filter     фильтр
     * @param exclusions список идентификаторов заявок на поездку, которые исключаются из списка
     * @param page       страница
     * @param size       количество заявок на странице
     * @param sort       свойство по которому сортируется список заявок на поездку
     * @param asc        сортировка по возрастанию
     * @return список заявок на поездку
     */
    Page<TripOrderData> get(RequestFilter filter, Set<UUID> exclusions, int page, int size, String sort, boolean asc);

    /**
     * Получает отфильтрованный список заявок на поездку
     *
     * @param filter фильтр
     * @param page   страница
     * @param size   количество заявок на странице
     * @param sort   свойство по которому сортируется список заявок на поездку
     * @param asc    сортировка по возрастанию
     * @return список заявок на поездку
     */
    Page<TripOrderData> getFull(RequestFilter filter, int page, int size, String sort, boolean asc);

    /**
     * Получает отфильтрованный список заявок на поездку
     *
     * @param filter фильтр
     * @param page   страница
     * @param size   количество заявок на странице
     * @param sort   свойство по которому сортируется список заявок на поездку
     * @param asc    сортировка по возрастанию
     * @return список заявок на поездку
     */
    Page<TripOrderData> getRegistry(RequestFilter filter, int page, int size, String sort, boolean asc);

    /**
     * Получает заявку на поездку
     *
     * @param organizationId идентификатор организации
     * @param id             идентификатор заявки на поездку
     * @return заявка на поездку
     */
    Optional<TripOrderData> get(UUID organizationId, UUID id);

    /**
     * Получает заявку на поездку по идентификатору (без идентификатора организации)
     *
     * @param id идентификатор заявки на поездку
     * @return заявка на поездку
     */
    Optional<TripOrderData> get(UUID id);

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
     * @param userId       идентификатор пользователя, который редактирует заявку
     * @param id           идентификатор заявки на поездку
     * @param newData      новые данные заявки на поездку
     * @param editedFields список измененных полей
     */
    void edit(UUID userId, UUID id, EditTripOrderData newData, Set<String> editedFields);

    /**
     * Проверка существования заявки на поездку
     *
     * @param organizationId идентификатор организации
     * @param requestId      идентификатор заявки на поездку
     * @return true если заявка на поездку существует, иначе false
     */
    boolean exists(UUID organizationId, UUID requestId);

    /**
     * Проверка существования заявки на поездку
     *
     * @param requestId идентификатор заявки на поездку
     * @return true если заявка на поездку существует, иначе false
     */
    boolean exists(UUID requestId);

    /**
     * Прикрепляет файл к заявке на поездку
     *
     * @param requestId идентификатор заявки на поездку
     * @param fileName  имя файла
     * @throws DatabaseLayerException в случае ошибки взаимодействия со слоем данных
     */
    void attachFile(UUID requestId, String fileName) throws DatabaseLayerException;

    /**
     * Получает файлы заявки на поездку
     *
     * @param requestId идентификатор заявки на поездку
     * @return ответ с названием файла
     */
    String getFile(UUID requestId);

    /**
     * Получает детальную информацию по чеку из заявки
     *
     * @param requestId идентификатор заявки на поездку
     * @return информация по чеку
     * @throws DatabaseLayerException в случае ошибки взаимодействия со слоем данных
     */
    TripOrderData getFileDetailed(UUID requestId) throws DatabaseLayerException;

    /**
     * Открепляет файл из заявки на поездку
     *
     * @param requestId идентификатор заявки на поездку
     */
    void detachFile(UUID requestId);
}
