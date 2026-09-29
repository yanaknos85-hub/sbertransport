package ru.sberbank.ditsib.transport.request.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ProductionCalendar {
    /**
     * @param date Дата, на которую надо вернуть количество рабочих часов
     *
     * @return Количество рабочих часов
     */
    Integer getWorkingMinutes(LocalDate date);
    
    /**
     * @param dateTime Местное дата/время, от которого нужно найти смещение в рабочих минутах
     * @param workingMinutes Смещение в рабочих минутах
     *
     * @return Время смещенное на указанное количество рабочих минут
     */
    LocalDateTime addWorkingMinutes(LocalDateTime dateTime, long workingMinutes);
    
    LocalDateTime addWorkingMinutes(LocalDateTime dateTime, long workingMinutes, String timeZone);
}
