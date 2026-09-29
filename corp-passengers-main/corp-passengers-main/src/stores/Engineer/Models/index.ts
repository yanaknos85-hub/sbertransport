import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

// dates
export enum DATE_TYPE {
  CREATION_DATE = 'creationDate',
  DESIRED_DATE = 'desiredDate',
  START_TRIP_DATE = 'startTripDate',
  FINISH_TRIP_DATE = 'finishTripDate',
}

export const dateType = ioTypeFromEnum<DATE_TYPE>('dateType', DATE_TYPE);
export type dateType = t.TypeOf<typeof dateType>;

export const dateTypeDescriptions: Record<dateType, string> = {
  creationDate: 'Дата создания',
  desiredDate: 'Желаемая дата',
  startTripDate: 'Дата Начала поездки',
  finishTripDate: 'Дата окончания поездки',
};

// addresses
export enum ADDRESS_TYPE {
  DEPARTURE_ADDRESS = 'DEPARTURE_ADDRESS',
  DESTINATION_ADDRESS = 'DESTINATION_ADDRESS',
  WAYPOINT_ADDRESS = 'WAYPOINT_ADDRESS',
  RECIPIENT_ADDRESS = 'RECIPIENT_ADDRESS',
}

export const addressType = ioTypeFromEnum<ADDRESS_TYPE>('addressType', ADDRESS_TYPE);
type addressType = t.TypeOf<typeof addressType>;

export const addressTypeDescriptions: Record<addressType, string> = {
  DEPARTURE_ADDRESS: 'Отправления',
  DESTINATION_ADDRESS: 'Назначения',
  WAYPOINT_ADDRESS: 'Промежуточный',
  RECIPIENT_ADDRESS: 'Получения',
};
