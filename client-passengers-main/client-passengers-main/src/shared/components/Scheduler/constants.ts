export enum Periodicity {
  week = 'WEEK',
  month = 'MONTH',
  quarter = 'QUARTER',
}

export const PeriodicityLabel = {
  [Periodicity.week]: 'Еженедельно',
  [Periodicity.month]: 'Ежемесячно',
  [Periodicity.quarter]: 'Ежеквартально',
};

export enum FieldName {
  periodicity = 'periodType',
  monthsOfQuarter = 'monthOfQuartal',
  weeksOfMonth = 'weekOfMonth',
  daysOfWeek = 'dayOfWeek',
  beginDate = 'beginDate',
  endDate = 'endDate',
}
