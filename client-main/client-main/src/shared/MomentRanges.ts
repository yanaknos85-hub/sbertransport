import moment from 'moment';

import { LabeledValue, TRangePickerArg } from 'utils/Types';

export enum RangeKeys {
  all = 'all',
  custom = 'custom',
  today = 'today',
  yesteday = 'yesteday',
  thisweek = 'thisweek',
  lastweek = 'lastweek',
  thismonth = 'thismonth',
  lastmonth = 'lastmonth',
  thisquarter = 'thisquarter',
  lastquarter = 'lastquarter',
}

export const pickerOptions: LabeledValue<RangeKeys>[] = [
  {
    label: 'за всё время',
    value: RangeKeys.all,
  },
  {
    label: 'выберите даты',
    value: RangeKeys.custom,
  },
  {
    label: 'сегодня',
    value: RangeKeys.today,
  },
  {
    label: 'вчера',
    value: RangeKeys.yesteday,
  },
  {
    label: 'на текущей неделе',
    value: RangeKeys.thisweek,
  },
  {
    label: 'на прошлой неделе',
    value: RangeKeys.lastweek,
  },
  {
    label: 'за текущий месяц',
    value: RangeKeys.thismonth,
  },
  {
    label: 'за прошлый месяц',
    value: RangeKeys.lastmonth,
  },
  {
    label: 'за текущий квартал',
    value: RangeKeys.thisquarter,
  },
  {
    label: 'за прошлый квартал',
    value: RangeKeys.lastquarter,
  },
];

export const MomentRanges: Record<string, TRangePickerArg> = {
  [RangeKeys.all]: [null, null],
  [RangeKeys.today]: [moment().startOf('day'), moment().endOf('day')],
  [RangeKeys.yesteday]: [moment().subtract(1, 'day').startOf('day'), moment().subtract(1, 'day').endOf('day')],
  [RangeKeys.thisweek]: [moment().startOf('week'), moment().endOf('week')],
  [RangeKeys.lastweek]: [moment().subtract(1, 'weeks').startOf('week'), moment().subtract(1, 'weeks').endOf('week')],
  [RangeKeys.thismonth]: [moment().startOf('month'), moment().endOf('month')],
  [RangeKeys.lastmonth]: [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')],
  [RangeKeys.thisquarter]: [moment().startOf('quarter'), moment().endOf('quarter')],
  [RangeKeys.lastquarter]: [
    moment().subtract(1, 'quarter').startOf('quarter'),
    moment().subtract(1, 'quarter').endOf('quarter'),
  ],
  [RangeKeys.custom]: null,
};
