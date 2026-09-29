package ru.sberbank.ditsib.service;

import ru.sberbank.ditsib.dto.AggregatedMainLeadDto;
import ru.sberbank.ditsib.dto.CreateMainLeadResponseDto;
import ru.sberbank.ditsib.dto.MainLeadRequestModel;

import java.util.List;

/**
 * Интерфейс доменного сервиса для работы с основным лидом
 * Определяет контракт для предсказания маршрутов такси
 */
public interface MainLeadService {

    /**
     * Предсказание маршрута такси на основе входных данных
     * 
     * @param request модель с данными для предсказания
     * @return модель с результатом предсказания маршрута
     */
    CreateMainLeadResponseDto predictRoute(MainLeadRequestModel request);

    /**
     * Получение списка основных заявок
     * @return список заявок
     */
    List<AggregatedMainLeadDto> getAllRequests();
}