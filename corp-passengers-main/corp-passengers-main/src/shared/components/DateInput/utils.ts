import moment, { Moment } from 'moment';
import { DATE_FORMAT } from 'constants/constants.app';
import { DateRangeISO } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { DateType, Mode, Value } from './types';

export const toDateRange = (
  { mode, value: [a, b] }: Value,
  dateTimeShow = false,
  rangeTimeShow = false
): { start: Moment; end: Moment } | undefined => {
  if (a === null && b === null) {
    return;
  }

  const [start, end] = [a!, b!];

  const dateRange = {
    date: () => dateTimeShow
      ? { start: start.clone(), end: start.clone().endOf('day') }
      : { start: start.clone().startOf('day'), end: start.clone().endOf('day') },
    range: () => rangeTimeShow
      ? { start: start ?? moment.unix(0), end: (end ?? moment()).clone() }
      : { start: start ?? moment.unix(0), end: (end ?? moment()).clone().endOf('day') },
    year: () => ({ start: start.clone().startOf('year'), end: start.clone().endOf('year') }),
    quarter: () => ({ start: start.clone().startOf('quarter'), end: start.clone().endOf('quarter') }),
    weeks: () => ({ start: start.clone().startOf('week'), end: start.clone().endOf('week') }),
  };
  return dateRange[mode]();
};

export const toDateRangeISO = ({ mode, value: [a, b] }: Value): DateRangeISO | undefined => {
  if (a === null && b === null) {
    return;
  }

  const [start, end] = [a!, b!];

  const dateRange = {
    date: () => ({ start: start.clone().startOf('day').toISOString(), end: start.clone().endOf('day').toISOString() }),
    range: () => ({
      start: start ? start.clone().startOf('day').toISOString() : moment.unix(0).toISOString(),
      end: end ? end.clone().endOf('day').toISOString() : moment().endOf('day').toISOString(),
    }),
    year: () => ({
      start: start.clone().startOf('year').toISOString(),
      end: start.clone().endOf('year').toISOString(),
    }),
    quarter: () => ({
      start: start.clone().startOf('quarter').toISOString(),
      end: start.clone().endOf('quarter').toISOString(),
    }),
    weeks: () => ({
      start: start.clone().format('YYYY-MM-DDTHH:mm:ss'),
      end: end.clone().format('YYYY-MM-DDTHH:mm:ss'),
    }),
  };
  return dateRange[mode]();
};

export const toWeekRange = () => {
  const today = moment();

  const firstDay = today.clone().startOf('M');
  const lastDay = today.clone().endOf('M');

  const weeks = [
    { start: firstDay.clone(), end: firstDay.clone().add(7, 'd') },
    { start: firstDay.clone().add(7, 'd'), end: firstDay.clone().add(15, 'd') },
    { start: firstDay.clone().add(15, 'd'), end: firstDay.clone().add(23, 'd') },
    { start: firstDay.clone().add(23, 'd'), end: lastDay.clone() },
  ];

  const weekRange = weeks.find(week => week.start.diff(today) < 0 && week.end.diff(today) > 0);

  return {
    mode: 'weeks',
    value: [weekRange?.start ?? today, weekRange?.end ?? today],
  };
};

export const toRequestDateRange = (
  dateType: DateType | undefined,
  date: Value | undefined,
  deadline: Value | undefined,
  dateTimeShow = false,
  rangeTimeShow = false
): Record<string, string | undefined> => {
  const dateParam = date || deadline;

  const dateFrom = dateParam && toDateRange(dateParam, dateTimeShow, rangeTimeShow)?.start.format();
  const dateTo = dateParam && toDateRange(dateParam, dateTimeShow, rangeTimeShow)?.end.format();

  const dateTypes = {
    CREATION_DATE: () => ({ creationTimeFrom: dateFrom, creationTimeTo: dateTo }),
    DESIRED_DATE: () => ({ desiredTimeFrom: dateFrom, desiredTimeTo: dateTo }),
    START_TRIP_DATE: () => ({ startTimeFrom: dateFrom, startTimeTo: dateTo }),
    END_TRIP_DATE: () => ({ finishTimeFrom: dateFrom, finishTimeTo: dateTo }),
    APPROVAL_DATE: () => ({ approvalTimeFrom: dateFrom, approvalTimeTo: dateTo }),
    TRANSFER_DATE: () => ({ transferTimeFrom: dateFrom, transferTimeTo: dateTo }),
    SHIPMENT_DATE: () => ({ shipmentTimeFrom: dateFrom, shipmentTimeTo: dateTo }),
    DEADLINE: () => ({ deadlineFrom: dateFrom, deadlineTo: dateTo }),
    default: () => ({ creationTimeFrom: dateFrom, creationTimeTo: dateTo }),
  };

  if (deadline && !dateType && !date) {
    return dateTypes[DateType.DEADLINE]();
  }

  return dateType ? dateTypes[dateType]() : dateTypes.default();
};

export const getFormatDateInput = (
  mode: Mode,
  dateTimeShow: boolean,
  rangeTimeShow: boolean,
  start: Moment | null
): Record<string, string> => {
  const dateFormat = dateTimeShow ? DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS : DATE_FORMAT.BASE_REVERTED_DOTS;
  const rangeFormat = rangeTimeShow ? DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS : DATE_FORMAT.BASE_REVERTED_DOTS;

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

  const weeks = `${moment(start).startOf('week').format(DATE_FORMAT.BASE_REVERTED_DOTS)}
    -  ${moment(start).endOf('week').format(DATE_FORMAT.BASE_REVERTED_DOTS)}`;

  return {
    weeks: () => ({ format: weeks, placeholder: '' }),
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
    return [moment().startOf('day').format(DATE_FORMAT.DATE_WITH_TIME_SECONDS_DOTS), DATE_FORMAT.TO_THE_CURRENT_DATE];
  }
  return undefined;
};

export const isValue = (payload: unknown): payload is Value => {
  return (
    typeof payload === 'object'
    && payload !== null
    && 'mode' in payload
    && typeof payload.mode === 'string'
    && 'value' in payload
    && Array.isArray(payload.value)
  );
};
