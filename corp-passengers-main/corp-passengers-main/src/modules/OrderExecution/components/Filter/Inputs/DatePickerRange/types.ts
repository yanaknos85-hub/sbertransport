import { Moment } from 'moment';

export type Mode = 'date' | 'range' | 'quarter' | 'year';

export interface Value {
  mode: Mode;
  value: [Moment | null, Moment | null];
}

export interface DateInputProps {
  allowEmpty?: boolean;
  value?: Value;
  onChange?: (newValue: Value) => void;
  isDisabledFutureDate?: boolean;
  dateTimeShow?: boolean;
  rangeTimeShow?: boolean;
}

export enum DateType {
  CREATION_DATE = 'CREATION_DATE',
  DESIRED_DATE = 'DESIRED_DATE',
  START_TRIP_DATE = 'START_TRIP_DATE',
  END_TRIP_DATE = 'END_TRIP_DATE',
  APPROVAL_DATE = 'APPROVAL_DATE',
  TRANSFER_DATE = 'TRANSFER_DATE',
  SHIPMENT_DATE = 'SHIPMENT_DATE',
  DEADLINE = 'DEADLINE',
}
