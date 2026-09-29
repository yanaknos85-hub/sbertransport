package ru.sberbank.ditsib.transport.request.service.impl;

import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.service.ProductionCalendar;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductionCalendarImpl implements ProductionCalendar {
    
    private static final LocalTime BEGINNING_OF_WORKING_DAY = LocalTime.of(9, 0, 0, 0);
    private static final Integer MIN_PER_HOUR = 60;
    private static final Integer LUNCH_BREAK_DURATION = 45;
    private static final LocalTime BEGINNING_OF_LUNCH_BREAK = LocalTime.of(13, 0, 0, 0);
    private static final Integer MIN_PER_COMMON_WORKDAY = 495;
    private static final Integer MIN_PER_FRIDAY_WORKDAY = MIN_PER_COMMON_WORKDAY - 5 * (MIN_PER_HOUR - LUNCH_BREAK_DURATION);
    
    private List<LocalDate> holidaysAndWeekend;
    private List<LocalDate> preHolidays;
    private List<LocalDate> workDays;
    
    ProductionCalendarImpl() {
        
        // Праздничные дни, в которые вне зависимости от дня недели не работаем
        holidaysAndWeekend = new ArrayList<>();
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 1));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 2));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 3));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 4));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 5));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 6));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 7));
        holidaysAndWeekend.add(LocalDate.of(2024, 1, 8));
        holidaysAndWeekend.add(LocalDate.of(2024, 2, 23));
        holidaysAndWeekend.add(LocalDate.of(2024, 3, 8));
        holidaysAndWeekend.add(LocalDate.of(2024, 4, 29));
        holidaysAndWeekend.add(LocalDate.of(2024, 4, 30));
        holidaysAndWeekend.add(LocalDate.of(2024, 5, 1));
        holidaysAndWeekend.add(LocalDate.of(2024, 5, 9));
        holidaysAndWeekend.add(LocalDate.of(2024, 5, 10));
        holidaysAndWeekend.add(LocalDate.of(2024, 6, 12));
        holidaysAndWeekend.add(LocalDate.of(2024, 11, 4));
        holidaysAndWeekend.add(LocalDate.of(2024, 12, 30));
        holidaysAndWeekend.add(LocalDate.of(2024, 12, 31));
        
        // Предпраздничные дни, в которые работаем на 1 час меньше
        preHolidays = new ArrayList<>();
        preHolidays.add(LocalDate.of(2024, 2, 22));
        preHolidays.add(LocalDate.of(2024, 3, 7));
        preHolidays.add(LocalDate.of(2024, 5, 8));
        preHolidays.add(LocalDate.of(2024, 6, 11));
        preHolidays.add(LocalDate.of(2024, 11, 2));
        
        // Выходные дни, которые стали рабочими из-за переноса
        workDays = new ArrayList<>();
        workDays.add(LocalDate.of(2024, 4, 27));
        workDays.add(LocalDate.of(2024, 12, 28));
        
        //2025
        // Праздничные дни, в которые вне зависимости от дня недели не работаем
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 1));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 2));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 3));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 4));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 5));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 6));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 7));
        holidaysAndWeekend.add(LocalDate.of(2025, 1, 8));
        holidaysAndWeekend.add(LocalDate.of(2025, 2, 23));
        holidaysAndWeekend.add(LocalDate.of(2025, 3, 8));
        holidaysAndWeekend.add(LocalDate.of(2025, 5, 1));
        holidaysAndWeekend.add(LocalDate.of(2025, 5, 2));
        holidaysAndWeekend.add(LocalDate.of(2025, 5, 9));
        holidaysAndWeekend.add(LocalDate.of(2025, 5, 10));
        holidaysAndWeekend.add(LocalDate.of(2025, 6, 12));
        holidaysAndWeekend.add(LocalDate.of(2025, 6, 13));
        holidaysAndWeekend.add(LocalDate.of(2025, 11, 3));
        holidaysAndWeekend.add(LocalDate.of(2025, 11, 4));
        holidaysAndWeekend.add(LocalDate.of(2025, 12, 31));

        // Предпраздничные дни, в которые работаем на 1 час меньше
        preHolidays.add(LocalDate.of(2025, 3, 7));
        preHolidays.add(LocalDate.of(2025, 4, 30));
        preHolidays.add(LocalDate.of(2025, 6, 11));
        preHolidays.add(LocalDate.of(2025, 11, 1));

        // Выходные дни, которые стали рабочими из-за переноса
        workDays.add(LocalDate.of(2025, 11, 1));
    }
    
    @Override
    public Integer getWorkingMinutes(LocalDate date) {
        var dayOfWeek = date.getDayOfWeek();
        int workingMinutes = switch (dayOfWeek) {
            case FRIDAY -> MIN_PER_FRIDAY_WORKDAY;
            case SATURDAY, SUNDAY -> 0;
            default -> MIN_PER_COMMON_WORKDAY;
        };
        workingMinutes = isHolidaysAndWeekend(date) ? 0 : workingMinutes;
        
        // В предпраздничный день рабочее время сокращается на 1 час короче
        workingMinutes = isPreHolidays(date) ? workingMinutes - MIN_PER_HOUR : workingMinutes;
        
        // Если рабочий день был перенесен на выходные, то считаем его рабочим
        workingMinutes = workingMinutes == 0 && isWorkDays(date) ? MIN_PER_COMMON_WORKDAY : workingMinutes;
        
        return workingMinutes;
    }
    
    @Override
    public LocalDateTime addWorkingMinutes(LocalDateTime dateTime, long workingMinutes) {
        
        long leftWorkingMinutes = workingMinutes;
        LocalDateTime cursor = LocalDateTime.of(dateTime.toLocalDate(), dateTime.toLocalTime());
        
        while (leftWorkingMinutes > 0) {
            // Если текущее время до начала рабочего дня, установим его на начало рабочего дня
            if (cursor.toLocalTime().isBefore(BEGINNING_OF_WORKING_DAY)) {
                cursor = LocalDateTime.of(cursor.toLocalDate(), BEGINNING_OF_WORKING_DAY);
            }
            // Если текущее время попало в обеденный перерыв, сдвинем его на коне обеденного перерыва
            if (inLunchBreak(cursor.toLocalTime())) {
                cursor = LocalDateTime.of(cursor.toLocalDate(), BEGINNING_OF_LUNCH_BREAK.plusMinutes(LUNCH_BREAK_DURATION));
            }
            
            // Если текущее время после окончания рабочего дня, то пропускаем этот день и начинаем считать следующий
            long workingMinutesForDayOfCursor = getWorkingMinutes(cursor.toLocalDate());
            var endTimeOfWorkingDay = getEndTimeOfWorkingDay(workingMinutesForDayOfCursor);
            if (cursor.toLocalTime().isAfter(endTimeOfWorkingDay)) {
                cursor = LocalDateTime.of(cursor.plusDays(1).toLocalDate(), BEGINNING_OF_WORKING_DAY);
            }
            
            // Подсчитаем сколько рабочих минут можно использовать в текущем дне (availableWorkMinutes)
            // 0 - выходной/праздник, 495 - полный рабочий день, 435 - полный предпраздничный день,
            // 420 - полная рабочая пятница, 360 - полная предпраздничная пятница
            // промежуточное значение - остаток рабочих минут до конца рабочего дня, если отчет начинается не сначала рабочего дня
            long availableWorkMinutes = getAvailableWorkMinutes(cursor);
            
            // Сдвигаем курсор на количество доступных рабочих минут в текущем дне до обеденного перерыва или после, но не больше остатка
            long delta = Math.min(availableWorkMinutes, leftWorkingMinutes);
            cursor = cursor.plusMinutes(delta);
            
            // Если нужно еще больше рабочих минут, то сдвигаем курсор на конец обеденного перерыва или на следующий день и продолжаем поиск
            leftWorkingMinutes = leftWorkingMinutes - delta;
            if (leftWorkingMinutes > 0) {
                cursor = isBeforeLunchBreak(cursor.toLocalTime()) || inLunchBreak(cursor.toLocalTime()) ?
                         LocalDateTime.of(cursor.toLocalDate(), BEGINNING_OF_LUNCH_BREAK.plusMinutes(LUNCH_BREAK_DURATION)) :
                         LocalDateTime.of(cursor.plusDays(1).toLocalDate(), BEGINNING_OF_WORKING_DAY);
            }
        }
        return cursor;
    }
    
    private boolean isBeforeLunchBreak(LocalTime time) {
        return time.isBefore(BEGINNING_OF_LUNCH_BREAK);
    }
    
    private boolean isAfterLunchBreak(LocalTime time) {
        return (!isBeforeLunchBreak(time) && !time.isBefore(BEGINNING_OF_LUNCH_BREAK.plusMinutes(LUNCH_BREAK_DURATION)));
    }
    
    private boolean inLunchBreak(LocalTime time) {
        return (!isBeforeLunchBreak(time) && !isAfterLunchBreak(time));
    }
    
    private long getAvailableWorkMinutes(LocalDateTime cursor) {
        long workingMinutesForDayOfCursor = getWorkingMinutes(cursor.toLocalDate());
        var endTimeOfWorkingDay = getEndTimeOfWorkingDay(workingMinutesForDayOfCursor);
        
        var endOfWorking = isBeforeLunchBreak(cursor.toLocalTime()) ?
                           LocalDateTime.of(cursor.toLocalDate(), BEGINNING_OF_LUNCH_BREAK) :
                           LocalDateTime.of(cursor.toLocalDate(), endTimeOfWorkingDay);
        return Math.max(0, Math.min(workingMinutesForDayOfCursor, Duration.between(cursor, endOfWorking).toMinutes()));
    }
    
    private LocalTime getEndTimeOfWorkingDay(long workingMinutesForDayOfCursor) {
        return workingMinutesForDayOfCursor > 0 ?
               BEGINNING_OF_WORKING_DAY.plusMinutes(workingMinutesForDayOfCursor).plusMinutes(LUNCH_BREAK_DURATION)
                                                :
               BEGINNING_OF_WORKING_DAY;
    }
    
    @Override
    public LocalDateTime addWorkingMinutes(LocalDateTime dateTime, long workingMinutes, String timeZone) {
        LocalDateTime localDateTime = dateTime.atZone(ZoneId.of("UTC"))
                                              .withZoneSameInstant(ZoneId.of(timeZone))
                                              .withZoneSameLocal(ZoneId.of(timeZone))
                                              .toLocalDateTime();
        
        var localDateTimeWithAddedWorkingHours = addWorkingMinutes(localDateTime, workingMinutes);
        
        return localDateTimeWithAddedWorkingHours.atZone(ZoneId.of(timeZone))
                                                 .withZoneSameInstant(ZoneId.of("UTC"))
                                                 .withZoneSameLocal(ZoneId.of("UTC"))
                                                 .toLocalDateTime();
    }
    
    private boolean isHolidaysAndWeekend(LocalDate date) {
        return isSpecialDate(holidaysAndWeekend, date);
    }
    
    private boolean isPreHolidays(LocalDate date) {
        return isSpecialDate(preHolidays, date);
    }
    
    private boolean isWorkDays(LocalDate date) {
        return isSpecialDate(workDays, date);
    }
    
    private boolean isSpecialDate(List<LocalDate> specialDates, LocalDate date) {
        for (LocalDate specialDate : specialDates) {
            if (specialDate.equals(date)) {
                return true;
            }
        }
        return false;
    }
}
