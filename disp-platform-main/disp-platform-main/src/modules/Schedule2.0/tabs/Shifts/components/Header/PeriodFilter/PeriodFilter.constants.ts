export enum PeriodType {
  Day = 'day',
  Week = 'week',
  Month = 'month',
}

export const periodOptions = [
  {
    value: PeriodType.Day,
    label: 'Сегодня',
  },
  {
    value: PeriodType.Week,
    label: 'Неделя',
  },
  {
    value: PeriodType.Month,
    label: 'Месяц',
  },
];
