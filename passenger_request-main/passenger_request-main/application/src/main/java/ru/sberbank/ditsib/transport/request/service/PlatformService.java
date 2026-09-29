package ru.sberbank.ditsib.transport.request.service;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с платформой
 */
public interface PlatformService {

    /**
     * Получение группы исполнителей по id пользователя
     *
     * @param employeeId id пользователя
     * @param token токен пользователя
     *
     * @return DTO с данными группы исполнителей
     */
    ExecutorGroupDTO getExecutorGroup(@NotNull UUID employeeId, List<UUID> regionIds, String token);
}
