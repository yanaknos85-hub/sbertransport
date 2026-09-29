import { YEARS_OPTIONS_LENGTH } from 'constants/constants.app';
import moment, { Moment } from 'moment/moment';

export const FORMAT = 'DD.MM.YYYY, HH:mm';
export const VALUE_NOT_FOUND = '-';

export const localTimeStringToUtcString = (local: string): string => moment(local).utc().format();
export const utcTimeStringToLocalMoment = (utc: string): Moment => moment.utc(utc).local();

export const getYearRange = (options?: { before: number; after: number }): number[] => {
  const currentYear = new Date().getFullYear();
  const { before = YEARS_OPTIONS_LENGTH, after = YEARS_OPTIONS_LENGTH } = options || {};

  const startYear = currentYear - before;
  const endYear = currentYear + after;
  const years = [];

  for (let year = startYear; year <= endYear; year++) {
    // @ts-ignore
    years.push(year);
  }

  return years;
};

export const getFormatDate = (date: number | string | undefined, format = FORMAT, emptyValue = VALUE_NOT_FOUND) => {
  if (!date) return emptyValue;

  return moment(date).locale('ru').format(format);
};
