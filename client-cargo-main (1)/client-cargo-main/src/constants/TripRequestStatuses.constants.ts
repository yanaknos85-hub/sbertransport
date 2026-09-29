import * as t from 'io-ts';
import * as R from 'ramda';

import * as tt from 'utils/io-ts';

const tripRequestStatusesTaxi = [
  'TAXI_AWAITING_APPROVAL',
  'TAXI_APPROVED',
  'TAXI_AWAITING_SEARCH',
  'TAXI_DRIVER_SEARCH',
  'TAXI_DRIVER_FOUND',
  'TAXI_DRIVER_ON_THE_WAY',
  'TAXI_DRIVER_ARRIVED',
  'TAXI_TRIP_IN_PROGRESS',
  'TAXI_TRIP_FINISHED',
  'TAXI_CANCELLED',
] as const;

const TripRequestStatusesTaxi = tt.oneOf([...tripRequestStatusesTaxi]);
type TripRequestStatusesTaxi = t.TypeOf<typeof TripRequestStatusesTaxi>;

const tripRequestStatusesPersonal = [
  'PERSONAL_AWAITING_APPROVAL',
  'PERSONAL_APPROVED',
  'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  'PERSONAL_SHARED_RIDE_DECLINED',
  'PERSONAL_TRIP_IN_PROGRESS',
  'PERSONAL_AWAITING_TRIP_APPROVAL',
  'PERSONAL_ORDER_PAYMENT_FORMATION',
  'PERSONAL_PAYMENT_AWAITING',
  'PERSONAL_PAYMENT_DONE',
  'PERSONAL_PAYMENT_DECLINED',
  'PERSONAL_CANCELLED',
] as const;

const TripRequestStatusesPersonal = tt.oneOf([...tripRequestStatusesPersonal]);
type TripRequestStatusesPersonal = t.TypeOf<typeof TripRequestStatusesPersonal>;

const tripRequestStatusesPublic = [
  'PUBLIC_AWAITING_APPROVAL',
  'PUBLIC_TRIP_CONFIRMATION',
  'PUBLIC_AWAITING_AFFIRMATIVE',
  'PUBLIC_ORDER_PAYMENT_FORMATION',
  'PUBLIC_PAYMENT_AWAITING',
  'PUBLIC_PAYMENT_DONE',
  'PUBLIC_PAYMENT_NOT_DONE',
  'PUBLIC_CANCELLED',
] as const;

const TripRequestStatusesPublic = tt.oneOf([...tripRequestStatusesPublic]);
type TripRequestStatusesPublic = t.TypeOf<typeof TripRequestStatusesPublic>;

const tripRequestStatusesCarSharing = [
  'CARSHARING_AWAITING_APPROVAL',
  'CARSHARING_APPROVED',
  'CARSHARING_AWAITING_SEARCH',
  'CARSHARING_TRIP_IN_PROGRESS',
] as const;

const TripRequestStatusesCarSharing = tt.oneOf([...tripRequestStatusesCarSharing]);
type TripRequestStatusesCarSharing = t.TypeOf<typeof TripRequestStatusesCarSharing>;

export const TTripRequestStatuses = tt.oneOf([
  ...tripRequestStatusesPersonal,
  ...tripRequestStatusesTaxi,
  ...tripRequestStatusesPublic,
  ...tripRequestStatusesCarSharing,
]);

export type TTripRequestStatuses = t.TypeOf<typeof TTripRequestStatuses>;

const TripRequestStatusesTaxiTitles: Record<TripRequestStatusesTaxi, string> = {
  TAXI_AWAITING_APPROVAL: 'На согласовании',
  TAXI_APPROVED: 'Согласовано',
  TAXI_AWAITING_SEARCH: 'Ожидает поиска',
  TAXI_DRIVER_SEARCH: 'Поиск водителя',
  TAXI_DRIVER_FOUND: 'Водитель назначен',
  TAXI_DRIVER_ON_THE_WAY: 'Водитель в пути',
  TAXI_DRIVER_ARRIVED: 'Водитель ожидает в точке отправления',
  TAXI_TRIP_IN_PROGRESS: 'Поездка началась',
  TAXI_TRIP_FINISHED: 'Поездка завершена',
  TAXI_CANCELLED: 'Отменено',
};

const TripRequestStatusesPersonalTitles: Record<TripRequestStatusesPersonal, string> = {
  PERSONAL_AWAITING_APPROVAL: 'На согласовании (ЛТ)',
  PERSONAL_APPROVED: 'Согласовано (ЛТ)',
  PERSONAL_AWAITING_SHARED_RIDE_APPROVAL: 'Согласование присоединения к СП (ЛТ)',
  PERSONAL_SHARED_RIDE_DECLINED: 'Присоединение не согласовано (ЛТ)',
  PERSONAL_TRIP_IN_PROGRESS: 'Поездка началась (ЛТ)',
  PERSONAL_AWAITING_TRIP_APPROVAL: 'На согласовании маршрута (ЛТ)',
  PERSONAL_ORDER_PAYMENT_FORMATION: 'Формирование приказа на выплату (ЛТ)',
  PERSONAL_PAYMENT_AWAITING: 'Ожидание выплаты (ЛТ)',
  PERSONAL_PAYMENT_DONE: 'Выплата произведена (ЛТ)',
  PERSONAL_PAYMENT_DECLINED: 'Выплата не произведена (ЛТ)',
  PERSONAL_CANCELLED: 'Отменено (ЛТ)',
};

const TripRequestStatusesPublicTitles: Record<TripRequestStatusesPublic, string> = {
  PUBLIC_AWAITING_APPROVAL: 'На согласовании (ОТ)',
  PUBLIC_TRIP_CONFIRMATION: 'Подтверждение поездки (ОТ)',
  PUBLIC_AWAITING_AFFIRMATIVE: 'Ожидание подтверждения (ОТ)',
  PUBLIC_ORDER_PAYMENT_FORMATION: 'Формирование приказа на выплату (ОТ)',
  PUBLIC_PAYMENT_AWAITING: 'Ожидание выплаты (ОТ)',
  PUBLIC_PAYMENT_DONE: 'Выплата произведена (ОТ)',
  PUBLIC_PAYMENT_NOT_DONE: 'Выплата не произведена (ОТ)',
  PUBLIC_CANCELLED: 'Отменено (ОТ)',
};

const TripRequestStatusesCarSharingTitles: Record<TripRequestStatusesCarSharing, string> = {
  CARSHARING_AWAITING_APPROVAL: 'На согласовании',
  CARSHARING_APPROVED: 'Согласовано',
  CARSHARING_AWAITING_SEARCH: 'Ожидает поиска',
  CARSHARING_TRIP_IN_PROGRESS: 'Поездка началась',
};

export const TripRequestStatusesTitles = R.mergeAll([
  TripRequestStatusesTaxiTitles,
  TripRequestStatusesPersonalTitles,
  TripRequestStatusesPublicTitles,
  TripRequestStatusesCarSharingTitles,
]) as Record<TTripRequestStatuses, string>;

export const TripStatusesChangeable: TTripRequestStatuses[] = [
  'TAXI_AWAITING_APPROVAL',
  'PERSONAL_AWAITING_APPROVAL',
  'PUBLIC_AWAITING_APPROVAL',
  'CARSHARING_AWAITING_APPROVAL',
  'TAXI_APPROVED',
  'PERSONAL_APPROVED',
  'CARSHARING_APPROVED',
  'TAXI_TRIP_IN_PROGRESS',
  'PERSONAL_TRIP_IN_PROGRESS',
  'CARSHARING_TRIP_IN_PROGRESS',
  'TAXI_AWAITING_SEARCH',
  'CARSHARING_AWAITING_SEARCH',
  'TAXI_DRIVER_SEARCH',
  'PERSONAL_AWAITING_TRIP_APPROVAL',
  'PERSONAL_ORDER_PAYMENT_FORMATION',
  'PUBLIC_ORDER_PAYMENT_FORMATION',
];

export const TripStatusesCancellable: TTripRequestStatuses[] = [
  ...TripStatusesChangeable,
  'TAXI_DRIVER_FOUND',
  'TAXI_DRIVER_ARRIVED',
  'TAXI_DRIVER_ON_THE_WAY',
  'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  'PERSONAL_SHARED_RIDE_DECLINED',
  'PERSONAL_PAYMENT_AWAITING',
  'PUBLIC_TRIP_CONFIRMATION',
  'PUBLIC_AWAITING_APPROVAL',
  'PUBLIC_ORDER_PAYMENT_FORMATION',
  'PUBLIC_PAYMENT_AWAITING',
];

export const TripStatusesFinal: TTripRequestStatuses[] = [
  'TAXI_CANCELLED',
  'TAXI_TRIP_FINISHED',
  'PERSONAL_CANCELLED',
  'PERSONAL_SHARED_RIDE_DECLINED',
  'PERSONAL_PAYMENT_DECLINED',
  'PERSONAL_PAYMENT_DONE',
  'PUBLIC_CANCELLED',
  'PUBLIC_PAYMENT_DONE',
  'PUBLIC_PAYMENT_NOT_DONE',
];
