package ru.sber.transport.dispatcher.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.dispatcher.dto.ShiftConflictResponseDTO;
import ru.sber.transport.dispatcher.dto.search.ShiftConflictSearchDTO;

/**
 * Сервис для работы с конфликтами смен.
 */
public interface ShiftConflictService {

    /**
     * Получить страницу конфликтов смен.
     *
     * @param searchDTO параметры поиска и фильтрации
     * @return страница с конфликтами смен
     */
    Page<ShiftConflictResponseDTO> getPage(ShiftConflictSearchDTO searchDTO);

    /**
     * Удалить запись о конфлите по идентификатору маршрута
     * @param routeId идентификтор маршурта
     */
    void delete(String routeId);

}
