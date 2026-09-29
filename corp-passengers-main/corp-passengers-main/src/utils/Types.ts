/* eslint-disable @typescript-eslint/ban-types */
import { Moment } from 'moment';

/**
 * @description Pick fields from T and make them required
 */
export type RequiredPick<T, K extends keyof T> = Required<Pick<T, K>>;

/**
 * @description Pick only required fields from T
 */
export type RequiredKeys<T> = { [K in keyof T]-?: {} extends Pick<T, K> ? never : K }[keyof T];

/**
 * @description Pick only optional fields from T
 */
export type OptionalKeys<T> = { [K in keyof T]-?: {} extends Pick<T, K> ? K : never }[keyof T];

/**
 * @description Antd RangePicker "onChange" method argument
 */
export type TRangePickerArg = [Moment | null, Moment | null] | null;

export interface LabeledValue<T = string | number> {
  key?: string;
  value: T;
  label: React.ReactNode;
}
