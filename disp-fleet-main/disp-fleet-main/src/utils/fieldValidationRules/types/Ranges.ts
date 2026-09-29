import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

/**
 * Диапазон чисел с start и end
 */
export const RangeNumber = t.strict({
  start: t.number,
  end: t.number,
});

/**
 * Тип диапазона чисел
 */
export type RangeNumber = t.TypeOf<typeof RangeNumber>;

/**
 * Частичный диапазон чисел (start и end опциональны)
 */
export const RangeNumberPartial = t.partial({
  start: t.number,
  end: t.number,
});

/**
 * Тип частичного диапазона чисел
 */
export type RangeNumberPartial = t.TypeOf<typeof RangeNumberPartial>;

/**
 * Диапазон времени (в минутах)
 */
export const TimeRange = t.strict({
  start: tt.time,
  end: tt.time,
});

/**
 * Тип диапазона времени
 */
export type TimeRange = t.TypeOf<typeof TimeRange>;

/**
 * Диапазон стоимости (в рублях)
 */
export const CostRange = t.strict({
  start: tt.money,
  end: tt.money,
});

/**
 * Тип диапазона стоимости
 */
export type CostRange = t.TypeOf<typeof CostRange>;
