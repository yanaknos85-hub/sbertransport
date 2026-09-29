import moment from 'moment';
import { DATE_FORMAT } from 'constants/constants.app';

export const formatTime = (
  t: string | number | number[] | undefined | null,
  emptyValue = '-',
  dateFormat = DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS
): string => {
  if (Array.isArray(t) && t.length) {
    const [year, month, day, hour, minute, second] = t;
    return moment([year, month - 1, day, hour + 4, minute, second]).format(dateFormat);
  }
  return t ? moment(t).format(dateFormat) : emptyValue;
};

export const formatShortTime = (t: string | number | null | undefined, emptyValue = '-'): string => t || t === 0 ? `${moment(t).format(DATE_FORMAT.TIME_SHORT_MINUTE)} мин` : emptyValue;

export const formatBaseTime = (t: string | number | null | undefined, emptyValue = '-'): string => t || t === 0 ? moment(t).format(DATE_FORMAT.TIME_BASE) : emptyValue;

export const formatBaseDate = (t: string | number | null | undefined, emptyValue = '-'): string => t ? moment(t).format(DATE_FORMAT.BASE_REVERTED_DOTS) : emptyValue;

export const formatTimeDate = (t: string | number | null | undefined, emptyValue = '-'): string => t ? moment(t).format(DATE_FORMAT.DATE_WITH_TIME_DOTS) : emptyValue;

export const getTimeString = (time: number | string | null | undefined, emptyValue = '-'): string => {
  if (time === null || time === undefined) {
    return emptyValue;
  }

  const minute = typeof time === 'number' ? time / 60000 : Number(time) / 60000;

  if (minute < 60) {
    return `${Math.round(minute)} мин`;
  }

  return `${Math.trunc(minute / 60)} ч ${Math.round(minute % 60)} мин`;
};

export const formatBaseUtcDate = (t: string | number | null | undefined, emptyValue = '-'): string => t ? moment(t).utc().format(DATE_FORMAT.DATE_WITH_UTC_TIME) : emptyValue;
