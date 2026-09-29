import moment from "moment";

export const START_PAGE = 0;

/**
 * Helper функция для приведения дат к единому формату timestamp (number).
 * Принимает различные форматы дат и возвращает timestamp в миллисекундах.
 */
export const toTimestamp = (date: number | string | moment.Moment): number => {
  if (typeof date === 'number') {
    return date; // уже timestamp
  }
  if (typeof date === 'string') {
    return moment(date).valueOf(); // ISO string → timestamp
  }
  if (moment.isMoment(date)) {
    return date.valueOf(); // moment object → timestamp
  }
  return 0; // fallback для некорректных значений
};