package ru.sber.transport.dispatcher.service;

import ru.sber.transport.dispatcher.dto.AttributeDTO;
import ru.sber.transport.dispatcher.dto.NewAttributeDTO;

import java.util.List;
import java.util.UUID;

public interface AttributeService {

    /**
     * Добавление признака водителя
     * @param tag тэг
     * @return сохраненный тэг
     */
    AttributeDTO add(UUID contractorId, NewAttributeDTO tag);

    /**
     * Добавление всех признаков водителей для контрагента
     * @param contractorId id контрагента
     * @param tags список признаков
     * @return список добавленных прихнаков
     */
    List<AttributeDTO> addAll(UUID contractorId, List<NewAttributeDTO> tags);

    /**
     * Обновление признака водителей контрагента
     * @param contractorId id контрагента
     * @param attributeId id признака для обновления
     * @param attributeDTO признак для обновления
     * @return обновленный признак
     */
    void edit(UUID contractorId, UUID attributeId, NewAttributeDTO attributeDTO);

    /**
     * Удаление признака водителей
     * @param attributeId id признака для удаления
     * @param contractorId id контрагента
     */
    void delete(UUID attributeId, UUID contractorId);

    /**
     * Получение признака водителя по id с проверкой принадлежности к контрагенту
     * @param attributeId id признака водителя
     * @param contractorId id контрагента
     * @return признак водителя
     */
    AttributeDTO findByIdAndContractorId(UUID attributeId, UUID contractorId);

    /**
     * Получить все признаки водителей контрагента
     * @param contractorId id контрагента
     * @return Список признаков водителей
     */
    List<AttributeDTO> findAllByContractorId(UUID contractorId);
}
