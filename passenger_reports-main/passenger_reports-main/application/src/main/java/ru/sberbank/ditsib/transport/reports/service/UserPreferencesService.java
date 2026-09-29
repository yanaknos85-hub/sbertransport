package ru.sberbank.ditsib.transport.reports.service;

import java.util.UUID;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesResponseDTO;

/**
 * Сервис для работы с настройками пользователя
 */
public interface UserPreferencesService {

    /**
     * Получение пользовательских настроек по названию формы и идентификатору пользователя
     * @param userID идентификатор пользователя
     * @param nameForm наименование формы
     * @return пользовательские настройки
     */
    UserPreferencesResponseDTO getPreferencesByIdAndNameForm(UUID userID, String nameForm);

    /**
     * Обновление пользовательских настроек
     * @param requestDTO запрос на обновление пользовательских настроек
     */
    void updateUserPreferences(UserPreferencesRequestDTO requestDTO);
}
