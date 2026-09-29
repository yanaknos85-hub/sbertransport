import * as t from 'io-ts';
import { StatusTagTypeEnum } from 'shared/components/Cargo/StatusTag/StatusTag';

import * as tt from 'utils/io-ts';

export enum CommonTitles {
  AWAITING_APPROVAL = 'На согласовании',
  APPROVED = 'Согласовано',
  AWAITING_DATA = 'Отправлено контрагенту',
  AWAITING_TRANSFER = 'На сборе',
  TRANSFER_FINISHED = 'Доставка',
  SHIPMENT_FINISHED = 'Доставлено',
  CONFIRMATION_FINISHED = 'Завершено',
  CANCELED = 'Отменено',
  PLANNING = 'Планирование',
  PLANNING_FINISHED = 'Планирование завершено',
}

/**
 * Статусы для грузоперевозок
 */
export enum CargoRequestStatusesEnum {
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_PLANNING_FINISHED = 'CARGO_PLANNING_FINISHED',
  CARGO_AWAITING_APPROVAL = 'CARGO_AWAITING_APPROVAL',
  CARGO_APPROVED = 'CARGO_APPROVED',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_DATA_RECEIVED = 'CARGO_DATA_RECEIVED',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_AWAITING_SHIPMENT = 'CARGO_AWAITING_SHIPMENT',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_AWAITING_DELIVERY_CONFIRMATION = 'CARGO_AWAITING_DELIVERY_CONFIRMATION',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
  CARGO_TRIAL = 'CARGO_TRIAL',
  CARGO_LOST = 'CARGO_LOST',
}

const cargoRequestStatuses = [
  'CARGO_AWAITING_DATA',
  'CARGO_PLANNING',
  'CARGO_PLANNING_FINISHED',
  'CARGO_AWAITING_APPROVAL',
  'CARGO_APPROVED',
  'CARGO_AWAITING_DATA',
  'CARGO_DATA_RECEIVED',
  'CARGO_AWAITING_TRANSFER',
  'CARGO_TRANSFER_FINISHED',
  'CARGO_AWAITING_SHIPMENT',
  'CARGO_SHIPMENT_FINISHED',
  'CARGO_AWAITING_DELIVERY_CONFIRMATION',
  'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  'CARGO_CANCELED',
  'CARGO_TRIAL',
  'CARGO_LOST',
] as const;

export const CargoRequestStatuses = tt.oneOf([...cargoRequestStatuses]);
export type CargoRequestStatusesType = t.TypeOf<typeof CargoRequestStatuses>;

/**
 * Маппинг статус - название статуса
 */
export const CargoRequestStatusesTitles: Record<CargoRequestStatusesType, string> = {
  CARGO_PLANNING: CommonTitles.PLANNING,
  CARGO_PLANNING_FINISHED: CommonTitles.PLANNING_FINISHED,
  CARGO_AWAITING_APPROVAL: CommonTitles.AWAITING_APPROVAL,
  CARGO_APPROVED: CommonTitles.APPROVED,
  CARGO_AWAITING_DATA: CommonTitles.AWAITING_DATA,
  CARGO_AWAITING_TRANSFER: CommonTitles.AWAITING_TRANSFER,
  CARGO_TRANSFER_FINISHED: CommonTitles.TRANSFER_FINISHED,
  CARGO_AWAITING_SHIPMENT: 'Доставка', // deprecated
  CARGO_SHIPMENT_FINISHED: CommonTitles.SHIPMENT_FINISHED,
  CARGO_DELIVERY_CONFIRMATION_FINISHED: CommonTitles.CONFIRMATION_FINISHED,
  CARGO_CANCELED: CommonTitles.CANCELED,
  CARGO_AWAITING_DELIVERY_CONFIRMATION: 'Ожидайте подтверждение получения', // deprecated
  CARGO_DATA_RECEIVED: 'Передача данных перевозчику завершена', // deprecated
  CARGO_TRIAL: 'Разбирательство', // deprecated
  CARGO_LOST: 'Груз утерян', // deprecated
};

/**
 * Маппинг статус - тип компонента StatusTag
 */
export const CargoRequestStatusesTypes: Record<CargoRequestStatusesType, StatusTagTypeEnum> = {
  CARGO_PLANNING: StatusTagTypeEnum.DEFAULT,
  CARGO_PLANNING_FINISHED: StatusTagTypeEnum.DEFAULT,
  CARGO_AWAITING_APPROVAL: StatusTagTypeEnum.WARNING,
  CARGO_APPROVED: StatusTagTypeEnum.SUCCESS,
  CARGO_AWAITING_DATA: StatusTagTypeEnum.PROCESSING,
  CARGO_DATA_RECEIVED: StatusTagTypeEnum.PROCESSING,
  CARGO_AWAITING_TRANSFER: StatusTagTypeEnum.PROCESSING,
  CARGO_TRANSFER_FINISHED: StatusTagTypeEnum.PROCESSING,
  CARGO_AWAITING_SHIPMENT: StatusTagTypeEnum.PROCESSING,
  CARGO_SHIPMENT_FINISHED: StatusTagTypeEnum.SUCCESS,
  CARGO_AWAITING_DELIVERY_CONFIRMATION: StatusTagTypeEnum.SUCCESS,
  CARGO_DELIVERY_CONFIRMATION_FINISHED: StatusTagTypeEnum.DEFAULT,
  CARGO_CANCELED: StatusTagTypeEnum.ERROR,
  CARGO_TRIAL: StatusTagTypeEnum.PROCESSING,
  CARGO_LOST: StatusTagTypeEnum.ERROR,
};

/* TODO: разобраться со статусами, вычистить лишние, которые давно не используются */
export const statusDictionary: Record<string, number> = {
  CARGO_AWAITING_APPROVAL: 0,
  CARGO_APPROVED: 1,
  CARGO_AWAITING_DATA: 2,
  CARGO_AWAITING_TRANSFER: 3,
  CARGO_TRANSFER_FINISHED: 4,
  CARGO_SHIPMENT_FINISHED: 5,
  CARGO_DELIVERY_CONFIRMATION_FINISHED: 6,
  CARGO_CANCELED: 0,
};

export const journalCancelStatuses = ['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED', 'CARGO_AWAITING_DATA', 'CARGO_AWAITING_TRANSFER'];

export enum Statuses {
  CARGO_ACCEPTED = 'CARGO_ACCEPTED',
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_PLANNING_FINISHED = 'CARGO_PLANNING_FINISHED',
  CARGO_AWAITING_APPROVAL = 'CARGO_AWAITING_APPROVAL',
  CARGO_APPROVED = 'CARGO_APPROVED',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export enum CancelReasons {
  MISTAKE = 'mistake',
  CHANGE_DATE_TIME = 'change_date_time',
  CHANGE_CARGO = 'change_cargo',
  OTHER_REASON = 'other_reason',
}

const CANCEL_REASONS = {
  [CancelReasons.MISTAKE]: 'Ошибочно создана',
  [CancelReasons.CHANGE_DATE_TIME]: 'Изменились дата/время',
  [CancelReasons.CHANGE_CARGO]: 'Изменился груз',
  [CancelReasons.OTHER_REASON]: 'Другая причина',
};

export const CANCEL_ORDER_OPTIONS = [
  {
    value: CancelReasons.MISTAKE,
    label: CANCEL_REASONS[CancelReasons.MISTAKE],
  },
  {
    value: CancelReasons.CHANGE_DATE_TIME,
    label: CANCEL_REASONS[CancelReasons.CHANGE_DATE_TIME],
  },
  {
    value: CancelReasons.CHANGE_CARGO,
    label: CANCEL_REASONS[CancelReasons.CHANGE_CARGO],
  },
  {
    value: CancelReasons.OTHER_REASON,
    label: CANCEL_REASONS[CancelReasons.OTHER_REASON],
  },
];

export const DRIVER_INFO_STATUSES = new Set<string>([
  Statuses.CARGO_AWAITING_TRANSFER,
  Statuses.CARGO_TRANSFER_FINISHED,
  Statuses.CARGO_SHIPMENT_FINISHED,
  Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
]);

export const FINAL_STATUSES = new Set<string>([
  Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
  Statuses.CARGO_CANCELED,
]);
