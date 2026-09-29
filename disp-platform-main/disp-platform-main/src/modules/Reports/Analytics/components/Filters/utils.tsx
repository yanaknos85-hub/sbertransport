import { MIN_ANALYTICS_START_DATE } from 'constants/app.constants';

export const formatFormValue = (value?: string | string[]) => {
  if (!value) return undefined;

  if (typeof value === 'string') {
    return [value];
  }

  return value.length ? value : undefined;
};

export const disabledYears = (date: moment.Moment) => {
  const currentYear = new Date().getFullYear();
  const year = date.year();

  return date && (year > currentYear || year < MIN_ANALYTICS_START_DATE);
};
