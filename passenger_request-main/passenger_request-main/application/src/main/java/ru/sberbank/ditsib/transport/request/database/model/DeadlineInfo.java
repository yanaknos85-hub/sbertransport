package ru.sberbank.ditsib.transport.request.database.model;

import ru.sberbank.ditsib.transport.constants.DeadlineState;

import java.time.LocalDateTime;

/**
 * Интерфейс для объектов, которые имеют дедлайн
 */
public interface DeadlineInfo {

    /**
     * Возвращает время наступления дедлайна
     * @return время наступления дедлайна
     */
    LocalDateTime getDriverArrivedDeadline();

    /**
     * Возвращает состояние дедлайна
     * @return состояние дедлайна
     */
    DeadlineState getDeadlineState();

    /**
     * Устанавливает состояние дедлайна
     * @param deadlineState состояние дедлайна
     */
    void setDeadlineState(DeadlineState deadlineState);

}
