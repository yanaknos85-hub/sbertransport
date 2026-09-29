import * as t from 'io-ts';
import { StatusTagTypeEnum } from 'shared/components/Cargo/StatusTag/StatusTag';

import * as tt from 'utils/io-ts';

export enum CommonTitlesExchange {
  CARGO_AWAITING_DATA = 'Доступна к доставке',
  TRANSFER_FINISHED = 'Доставка',
  SHIPMENT_FINISHED = 'Доставлено',
  CANCELED = 'Отменено',
}

const cargoRequestStatusesExchange = [
  'CARGO_AWAITING_DATA',
  'CARGO_TRANSFER_FINISHED',
  'CARGO_SHIPMENT_FINISHED',
  'CARGO_CANCELED',
] as const;

export const statusDictionary: Record<string, number> = {
  CARGO_AWAITING_DATA: 0,
  CARGO_TRANSFER_FINISHED: 1,
  CARGO_SHIPMENT_FINISHED: 2,
  CARGO_CANCELED: 0,
};

export const CargoRequestStatusesExchange = tt.oneOf([...cargoRequestStatusesExchange]);
export type CargoRequestStatusesExchangeType = t.TypeOf<typeof CargoRequestStatusesExchange>;

/**
 * Маппинг статус - название статуса для биржи
 */
export const CargoRequestStatusesTitlesExchange: Record<CargoRequestStatusesExchangeType, string> = {
  CARGO_AWAITING_DATA: CommonTitlesExchange.CARGO_AWAITING_DATA,
  CARGO_TRANSFER_FINISHED: CommonTitlesExchange.TRANSFER_FINISHED,
  CARGO_SHIPMENT_FINISHED: CommonTitlesExchange.SHIPMENT_FINISHED,
  CARGO_CANCELED: CommonTitlesExchange.CANCELED,
};
/**
 * Маппинг статус - тип компонента StatusTag
 */
export const CargoRequestStatusesTypesExchange: Record<CargoRequestStatusesExchangeType, StatusTagTypeEnum> = {
  CARGO_AWAITING_DATA: StatusTagTypeEnum.PROCESSING,
  CARGO_TRANSFER_FINISHED: StatusTagTypeEnum.PROCESSING,
  CARGO_SHIPMENT_FINISHED: StatusTagTypeEnum.PROCESSING,
  CARGO_CANCELED: StatusTagTypeEnum.ERROR,
};
