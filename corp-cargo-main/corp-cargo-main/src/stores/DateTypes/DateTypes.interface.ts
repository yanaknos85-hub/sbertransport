import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum DATE_TYPES {
  CREATION_DATE = 'CREATION_DATE',
  DESIRED_DATE = 'DESIRED_DATE',
  START_TRIP_DATE = 'START_TRIP_DATE',
  END_TRIP_DATE = 'END_TRIP_DATE',
  APPROVAL_DATE = 'APPROVAL_DATE',
  TRANSFER_DATE = 'TRANSFER_DATE',
  SHIPMENT_DATE = 'SHIPMENT_DATE',
}

export const DateTypeDescriptions: Record<DATE_TYPES, string> = {
  CREATION_DATE: 'создания заявки',
  DESIRED_DATE: 'желаемая отправления',
  START_TRIP_DATE: 'начала поездки',
  END_TRIP_DATE: 'завершения поездки',
  APPROVAL_DATE: 'согласования',
  TRANSFER_DATE: 'сбора',
  SHIPMENT_DATE: 'доставки',
};

export const DateType = t.strict({
  id: t.string,
  name: t.string,
  rusName: t.string,
});

export const dateType = ioTypeFromEnum<DATE_TYPES>('dateType', DATE_TYPES);
export type dateType = t.TypeOf<typeof dateType>;
