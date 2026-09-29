import { RangeNumber } from 'api/register-search';
import moment from 'moment';
import { MomentDates, Month } from '../types/types';

export const transformDesiredDate = (value?: MomentDates): RangeNumber | undefined => value && {
  start: value[0].startOf('day').valueOf(),
  end: value[1].endOf('day').valueOf(),
};

const isToday = (value: moment.Moment) => value.isSame(moment(), 'day');

export const transformCreationDate = (value?: MomentDates): RangeNumber | undefined => value && {
  start: value[0].startOf('day').valueOf(),
  end: (isToday(value[1]) ? moment() : value[1].endOf('day')).valueOf(),
};

export const getTimeInMsWithoutDate = (value: moment.Moment): number => moment(value).valueOf() - moment(value).startOf('day').valueOf();

export const rubblesFormatter = (value: string | number | undefined): string => (value ? `${value} ₽` : '');

export const timeFormatter = (value: string | number | undefined): string => (value ? `${value} мин` : '');

export const parseTimePicker = (value: MomentDates | undefined): RangeNumber | undefined => value && {
  start: getTimeInMsWithoutDate(value[0]),
  end: getTimeInMsWithoutDate(value[1]),
};

export const creatingListOfMonths = (): Month[] => {
  const now = moment();
  const currentYear = Number(now.format('YYYY'));
  const currentMonth = Number(now.format('MM'));
  const tomorrow = Number(now.add(1, 'days').format('DD'));
  const monthsList = moment
    .months()
    .slice(0, currentMonth)
    .map((v, ind) => ({
      id: ind + 1, title: v, year: currentYear,
    }));

  return tomorrow !== 1 && currentMonth !== 1 ? [...monthsList.slice(0, -1)] : monthsList;
};
