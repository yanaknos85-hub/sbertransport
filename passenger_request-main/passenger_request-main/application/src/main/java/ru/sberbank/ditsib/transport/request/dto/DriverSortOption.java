package ru.sberbank.ditsib.transport.request.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @deprecated Что использовать вместо этого перечисления?
 */
@Getter
@RequiredArgsConstructor
@Deprecated(since = "18.04")
public enum DriverSortOption {
    
    DRIVER_STATUS("Статус водителя"),
    AUTOPARK_NAME("Название автопарка"),
    FIRST_NAME("Имя"),
    LAST_NAME("Фамилия"),
    PATRONYMIC("Отчество"),
    DRIVER_TAG("Признаки водителя"),
    DRIVER_EXPIRIENCE("Опыт вождения"),
    DRIVER_RAITING("Рейтинг водителя");
    
    private final String description;
    
}
