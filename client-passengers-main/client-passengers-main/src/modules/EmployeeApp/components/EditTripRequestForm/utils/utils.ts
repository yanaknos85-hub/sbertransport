
import moment from 'moment';
import { Moment } from 'moment/moment';

import { TripPurpose } from 'stores/Trip/Trip.interface';

export const checkPurposeValidity = (rule: boolean, isPurpose?: boolean): Promise<void> => rule
  ? Promise.resolve()
  : Promise.reject(
    new Error(
      isPurpose
        ? 'Укажите цель поездки, не противоречащую условиям запланированной поездки'
        : 'Укажите дату, не противоречащую условиям выбранной цели поездки'
    )
  );

export const validatePurposeTime = (purpose: Partial<TripPurpose>, when: Moment): boolean => {
  if (!purpose.tripPurposeTimes?.length) {
    return true;
  }

  // time conditions will be checked with OR operator
  let result = false;
  (purpose.tripPurposeTimes || []).forEach(times => {
    if (times.startTime && times.endTime) {
      const startMoment = moment.utc(times.startTime);
      const endMoment = moment.utc(times.endTime);

      const start = when.clone().set({ hours: startMoment.hours(), minutes: startMoment.minutes() });
      const end = when.clone().set({ hours: endMoment.hours(), minutes: endMoment.minutes() });

      const isTwoDays = start.diff(end) <= 0;
      const condition = isTwoDays
        ? when.diff(start) >= 0 && when.diff(end) <= 0
        : when.diff(start) >= 0 || when.diff(end) <= 0;
      if (condition) {
        result = true;
      }
    }
  });
  return result;
};
