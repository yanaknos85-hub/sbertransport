import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { Periodicity } from "./constants";

const Period = t.intersection([
  t.type({
    periodType: ioTypeFromEnum<Periodicity>('Periodicity', Periodicity),
    dayOfWeek: t.array(t.number),
    weekOfMonth: t.array(t.number),
    monthOfQuartal: t.array(t.number),
    beginDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    cron: t.string,
    countRequest: t.number,
    cost: t.number,
  }),
]);

export type PeriodType = t.TypeOf<typeof Period>;