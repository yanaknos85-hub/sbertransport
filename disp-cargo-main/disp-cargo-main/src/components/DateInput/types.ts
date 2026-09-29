import { Moment } from 'moment';
import * as t from 'io-ts';

export type Mode = 'date' | 'range' | 'quarter' | 'year';

export interface Value {
  mode: Mode;
  value: [Moment | null, Moment | null];
}

export interface DateInputProps {
  value?: Value;
  onChange?: (newValue: Value) => void;
  isDisabledFutureDate?: boolean;
  dropdownClassName?: string;
}

export interface FormatDateInput { format: string; placeholder: string }

export const ISOStringRange = t.strict({ start: t.string, end: t.string });
export type ISOStringRange = t.TypeOf<typeof ISOStringRange>;
