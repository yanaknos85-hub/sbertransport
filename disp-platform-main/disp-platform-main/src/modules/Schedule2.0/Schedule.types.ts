import { BusynessTrip, ScheduleVehicle, Shift } from 'api/schedule2.0/schedule.types';

export interface CalendarData {
  calendarStartTime: string;
  calendarStartHour: string;
  calendarEndTime: string;
  isStartOuter: boolean;
}

export interface CalendarShift extends Shift, CalendarData {}

export interface CalendarTrip extends BusynessTrip, CalendarData {}

export interface ShiftWithCar extends Shift {
  vehicle: ScheduleVehicle;
}
