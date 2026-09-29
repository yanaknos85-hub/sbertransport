import * as t from 'io-ts';

export const TransportClass = t.strict({
  value: t.string,
  rusName: t.string,
});

export enum TariffTransportClasses {
  TRANSFER = 'TRANSFER',
  TRANSFER_COMFORT = 'TRANSFER_COMFORT',
  TRANSFER_COMFORT_PLUS = 'TRANSFER_COMFORT_PLUS',
  TRANSFER_BUSINESS = 'TRANSFER_BUSINESS',
  TRANSFER_VIP = 'TRANSFER_VIP',
  TRANSFER_CAR_CHOICE = 'TRANSFER_CAR_CHOICE',
}

export type TransportClass = t.TypeOf<typeof TransportClass>;
