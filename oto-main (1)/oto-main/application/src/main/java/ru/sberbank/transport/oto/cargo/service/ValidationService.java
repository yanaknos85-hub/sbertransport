package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;

public interface ValidationService {
    String EXECUTOR_GROUP_EXCEPTION_MESSAGE = "Нельзя указывать группы исполнителей и режим поиска без группы одновременно.";

    /**
     * Проверяет корректность области видимости запроса на грузовые заявки
     *
     * @param cargoRequestDto дто-объект запроса
     * throws VisibilityScopeException
     */
    void validateRequestScopeVisibility(CargoRequestDto cargoRequestDto);

    /**
     * Проверяет, что группы исполнителей не указаны, если установлен флаг пустых групп
     *
     * @param dto дто-объект запроса с информацией о группах исполнителей
     * throws BadRequestException если указаны группы исполнителей при установленном флаге пустых групп
     */
    void validateEmptyExecutorGroups(CargoRequestDto dto);
}