export const SHIFT_DATE_FORMAT = 'YYYY-MM-DDTHH';
export const CELL_PADDING = 4;

export enum PeriodicityTypes {
  Weekly = 'WEEKLY',
  Flexible = 'FLEXIBLE',
}

export const periodicityTitles: Record<PeriodicityTypes, string> = {
  [PeriodicityTypes.Weekly]: 'Еженедельно',
  [PeriodicityTypes.Flexible]: 'Плавающий график',
};

export const flexibleDays = Array.from(Array(7).keys()).map(value => ({ value: value + 1 }));

export const ONE_HOUR = 60 * 60 * 1000;

export const WORKING_DAY_LENGTH = 8;

export const WORKING_DAY_KEY = 'WORKING_DAY_KEY';

export enum SchedulerTabs {
  Shift = 'shift',
  Conflict = 'conflict',
}
