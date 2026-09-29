import { useEffect, useMemo } from 'react';

import moment, { Moment } from 'moment';

import { useProfile } from 'api/profile/profile.api';
import { useSchedule } from 'api/schedule2.0/schedule.api';

import { getCalendarEndTime, getCalendarStartHour, getCalendarStartTime } from 'modules/Schedule2.0/Schedule.lib';
import { CalendarShift } from 'modules/Schedule2.0/Schedule.types';
import { useWorkload } from 'modules/Schedule2.0/tabs/Shifts/context/analyticsWorkload.context';
import { useShiftsQuery } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';

/**
 * - Получает данные о сменах
 * - Добавляет нужные поля для более удобного позиционирования на графике
 * - Собирает список id авто в полученных сменах
 * - Добавляет список авто в workload
*/
export const useScheduleData = (times: Moment[]) => {
  const { contractorId, autoparkId } = useProfile().data;
  const { query } = useShiftsQuery();

  const vehicleShifts = useSchedule({
    contractorId,
    autoparkId,
    ...query,
  }, {
    keepPreviousData: true,
  }).data;

  // Добавляем данные для более удобного позиционирования смен на графике
  const schedule = useMemo(() => ({
    ...vehicleShifts,
    content: vehicleShifts.content.map(({ shifts, ...content }) => ({
      ...content,
      shifts: shifts.map((shift): CalendarShift => {
        const calendarStartTime = getCalendarStartTime(shift.startDate, times);
        const calendarStartHour = getCalendarStartHour(shift.startDate, times);
        const calendarEndTime = getCalendarEndTime(shift.endDate, times);

        return {
          ...shift,
          calendarStartTime,
          calendarStartHour,
          calendarEndTime,
          isStartOuter: moment(calendarStartTime).isAfter(moment.utc(shift.startDate)),
        };
      }),
    })),
  }), [vehicleShifts, times]);

  const vehicles = vehicleShifts.content;

  // Собираем список id авто
  const vehicleIds = useMemo(() => vehicles.map(v => v.vehicle.id), [vehicles]);

  // Добавляем список авто в workload
  const { setVehicleIds } = useWorkload();
  useEffect(() => setVehicleIds(vehicleIds), [vehicleIds, setVehicleIds]);

  return {
    schedule,
    vehicleIds,
  };
};
