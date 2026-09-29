import moment, { Moment } from 'moment';

import { SHIFT_DATE_FORMAT } from './constants/schedule.constants';

/**
 * Часовая ячейка, в которой будет находиться начало поездки.
 * Например, если смена начинается 20.05.2024 в 12:35, то calendarStartHour = 20.05.2024 12:00
 * @param startTime время начала
 * @param times - массив объектов Moment - время, отображаемое на календаре
 */
export const getCalendarStartHour = (startTime: string, times: Moment[]) => (
  moment.max([moment.utc(startTime), times[0]]).format(SHIFT_DATE_FORMAT)
);

/**
 * Начало поездки с учетом календаря.
 * Если поездка начинается раньше, чем календарь, то calendarStartTime = начало календаря
 * @param startTime время начала
 * @param times - массив объектов Moment - время, отображаемое на календаре
 */
export const getCalendarStartTime = (startTime: string, times: Moment[]) => (
  moment.max([moment.utc(startTime), times[0]]).format()
);

/**
 * Окончание поездки с учетом календаря.
 * Если поездка заказчивается позже, чем календарь, то calendarEndTime = конец календаря
 * @param endTime время окончания
 * @param times - массив объектов Moment - время, отображаемое на календаре
 */
export const getCalendarEndTime = (endTime: string, times: Moment[]) => (
  moment.min([moment.utc(endTime), times.at(-1)!.clone().add(1, 'h')]).format()
);
