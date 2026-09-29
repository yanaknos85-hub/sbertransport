import moment, { Moment } from 'moment';
import {
  ISOStringRange, Value, Mode, FormatDateInput
} from './types';
import { DATE_FORMAT } from 'constants/app.constants';

export const toDateRange = ({ mode, value: [a, b] }: Value): ISOStringRange | undefined => {
  if (a === null && b === null) {
    return;
  }

  const [start, end] = [a!, b!];

  const dateRange = {
    date: () => ({ start: start.clone().startOf('day').toISOString(), end: start.clone().endOf('day').toISOString() }),
    range: () => {
      const isFutureDate = start && start.isAfter(moment());

      const futureEnd
        = isFutureDate && !end
          ? start.clone().add(1, 'month').endOf('day').toISOString()
          : moment().endOf('day').toISOString();

      return {
        start: start ? start.clone().startOf('day').toISOString() : moment.unix(0).toISOString(),
        end: end ? end.clone().endOf('day').toISOString() : futureEnd,
      };
    },
    year: () => ({
      start: start.clone().startOf('year').toISOString(),
      end: start.clone().endOf('year').toISOString(),
    }),
    quarter: () => ({
      start: start.clone().startOf('quarter').toISOString(),
      end: start.clone().endOf('quarter').toISOString(),
    }),
  };
  return dateRange[mode]();
};

export const getFormatDateInput = (mode: Mode, start: Moment | null): FormatDateInput => {
  const dateFormat = DATE_FORMAT.BASE_REVERTED_DOTS;
  const rangeFormat = DATE_FORMAT.BASE_REVERTED_DOTS;

  const quarterFormat = start
    ? `${moment(start).startOf('quarter').format(dateFormat)}  -  ${moment(start).endOf('quarter').format(dateFormat)}`
    : `${moment().startOf('quarter').format(dateFormat)}  -  ${moment().endOf('quarter').format(dateFormat)}`;

  const quarterPlaceholder = `${moment().startOf('quarter').format(DATE_FORMAT.BASE_REVERTED_DOTS)}
    -  ${moment().endOf('quarter').format(DATE_FORMAT.BASE_REVERTED_DOTS)}`;

  const yearFormat = start
    ? `${moment(start).startOf('year').format(dateFormat)}  -  ${moment(start).endOf('year').format(dateFormat)}`
    : `${moment().startOf('year').format(dateFormat)}  -  ${moment().endOf('year').format(dateFormat)}`;

  const yearPlaceholder = `${moment().startOf('year').format(DATE_FORMAT.BASE_REVERTED_DOTS)}
    -  ${moment().endOf('year').format(DATE_FORMAT.BASE_REVERTED_DOTS)}`;

  return {
    date: () => ({ format: dateFormat, placeholder: `${moment().format(DATE_FORMAT.BASE_REVERTED_DOTS)}` }),
    range: () => ({ format: rangeFormat, placeholder: '' }),
    quarter: () => ({ format: quarterFormat, placeholder: quarterPlaceholder }),
    year: () => ({ format: yearFormat, placeholder: yearPlaceholder }),
  }[mode]();
};

export const getFormatRangeInput = (
  mode: Mode,
  start: Moment | null,
  end: Moment | null
): [string, string] | undefined => {
  if (!start && !end) {
    return [
      moment().startOf('day').format(DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS),
      moment().endOf('day').format(DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS),
    ];
  }
  if (!start) {
    return [
      DATE_FORMAT.BEFORE_THE_SPECIFIED_DATE,
      moment().endOf('day').format(DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS),
    ];
  }
  if (!end) {
    const isFutureDate = start.isAfter(moment());
    const futureDate = start.clone().add(1, 'month').format(DATE_FORMAT.BASE_REVERTED_DOTS);
    const format = isFutureDate ? futureDate : DATE_FORMAT.TO_THE_CURRENT_DATE;
    return [moment().startOf('day').format(DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS), format];
  }
};
