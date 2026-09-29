/* eslint-disable @typescript-eslint/ban-types */
import { FormInstance } from 'antd/lib/form';
import { Store } from 'antd/lib/form/interface';
import { isMoment, Moment } from 'moment';

/**
 * @description Pick fields from T and make them required
 */
export type RequiredPick<T, K extends keyof T> = Required<Pick<T, K>>;

/**
 * @description Pick only required fields from T
 */
export type RequiredKeys<T> = { [K in keyof T]-?: Record<string, unknown> extends Pick<T, K> ? never : K }[keyof T];

/**
 * @description Pick only optional fields from T
 */
export type OptionalKeys<T> = { [K in keyof T]-?: Record<string, unknown> extends Pick<T, K> ? K : never }[keyof T];

/**
 * @description Tuple of Moment instances
 */
export type MomentTuple = [Moment, Moment];

export function isMomentTuple(tuple: unknown): tuple is MomentTuple {
  return Array.isArray(tuple) && Boolean(tuple.length === 2) && isMoment(tuple[0]) && isMoment(tuple[1]);
}

/**
 * @description Antd RangePicker "onChange" method argument
 */
export type TRangePickerArg = [Moment | null, Moment | null] | null;

export interface LabeledValue<T = string | number> {
  key?: string;
  value: T;
  label: React.ReactNode;
}

export interface FormFinishInfo {
  values: Store;
  forms: Forms;
}

export type Forms = Record<string, FormInstance>;
