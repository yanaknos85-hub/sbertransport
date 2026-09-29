import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const RangeNumber = t.strict({
  start: t.number,
  end: t.number,
});

export type RangeNumber = t.TypeOf<typeof RangeNumber>;

export const RangeNumberPartial = t.partial({
  start: t.number,
  end: t.number,
});

export type RangeNumberPartial = t.TypeOf<typeof RangeNumberPartial>;

export const TimeRange = t.strict({
  start: tt.time,
  end: tt.time,
});

export type TimeRange = t.TypeOf<typeof TimeRange>;

export const CostRange = t.strict({
  start: tt.money,
  end: tt.money,
});

export type CostRange = t.TypeOf<typeof CostRange>;
