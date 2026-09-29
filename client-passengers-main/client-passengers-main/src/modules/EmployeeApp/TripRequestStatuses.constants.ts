/* eslint-disable @typescript-eslint/no-duplicate-enum-values */
import * as t from 'io-ts';
import * as R from 'ramda';

import * as tt from 'utils/io-ts';

import { LabeledValue } from 'utils/Types';
import { TitleMap } from './components/TripsPage/TripRequestDetailedView/utils/mergeArrays';

export enum CommonTitles {
  AWAITING_APPROVAL = 'На согласовании',
  APPROVED = 'Согласовано',
  CANCELED = 'Отменено',
}

export interface TransportTypeStatuses {
  TAXI: LabeledValue<string>[];
  PERSONAL: LabeledValue<string>[];
  PUBLIC: LabeledValue<string>[];
  CARSHARING: LabeledValue<string>[];
  GROUP_TRANSFER: LabeledValue<string>[];
}

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
  'GENAI_CHECK',
] as const;

const TripRequestStatusesTaxi = tt.oneOf([...tripRequestStatusesTaxi]);
export type TripRequestStatusesTaxi = t.TypeOf<typeof TripRequestStatusesTaxi>;

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
  'PERSONAL_TRIP_FINISHED',
  'GENAI_CHECK',
] as const;

const TripRequestStatusesPersonal = tt.oneOf([...tripRequestStatusesPersonal]);
export type TripRequestStatusesPersonal = t.TypeOf<typeof TripRequestStatusesPersonal>;

export type TripRequestStatuses = TripRequestStatusesTaxi | TripRequestStatusesPersonal | TripRequestStatusesPublic | TripRequestStatusesCarSharing | TripRequestStatusesGroupTransfer;

const tripRequestStatusesPublic = [
  'PUBLIC_AWAITING_APPROVAL',
  'PUBLIC_TRIP_CONFIRMATION',
  'PUBLIC_AWAITING_AFFIRMATIVE',
  'PUBLIC_ORDER_PAYMENT_FORMATION',
  'PUBLIC_PAYMENT_AWAITING',
  'PUBLIC_PAYMENT_DONE',
  'PUBLIC_PAYMENT_NOT_DONE',
  'PUBLIC_CANCELLED',
  'GENAI_CHECK',
] as const;

const TripRequestStatusesPublic = tt.oneOf([...tripRequestStatusesPublic]);
export type TripRequestStatusesPublic = t.TypeOf<typeof TripRequestStatusesPublic>;

const tripRequestStatusesGroupTransfer = [
  'GROUP_TRANSFER_AWAITING_APPROVAL',
  'GROUP_TRANSFER_APPROVED',
  'GROUP_TRANSFER_AWAITING_SEARCH',
  'GROUP_TRANSFER_DRIVER_SEARCH',
  'GROUP_TRANSFER_DRIVER_FOUND',
  'GROUP_TRANSFER_DRIVER_ON_THE_WAY',
  'GROUP_TRANSFER_DRIVER_ARRIVED',
  'GROUP_TRANSFER_TRIP_IN_PROGRESS',
  'GROUP_TRANSFER_TRIP_FINISHED',
  'GROUP_TRANSFER_CANCELLED',
] as const;

const TripRequestStatusesGroupTransfer = tt.oneOf([...tripRequestStatusesGroupTransfer]);
export type TripRequestStatusesGroupTransfer = t.TypeOf<typeof TripRequestStatusesGroupTransfer>;

const tripRequestStatusesCarSharing = [
  'CARSHARING_AWAITING_APPROVAL',
  'CARSHARING_APPROVED',
  'CARSHARING_DECLINED',
  'CARSHARING_AWAITING_SEARCH',
  'CARSHARING_TRIP_IN_PROGRESS',
  'CARSHARING_AWAITING_TRIP_APPROVAL',
  'CARSHARING_TRIP_FINISHED',
  'CARSHARING_CANCELLED',
] as const;

const TripRequestStatusesCarSharing = tt.oneOf([...tripRequestStatusesCarSharing]);
export type TripRequestStatusesCarSharing = t.TypeOf<typeof TripRequestStatusesCarSharing>;

export const TTripRequestStatuses = tt.oneOf([
  ...tripRequestStatusesPersonal,
  ...tripRequestStatusesTaxi,
  ...tripRequestStatusesPublic,
  ...tripRequestStatusesCarSharing,
  ...tripRequestStatusesGroupTransfer,
]);

export type TTripRequestStatuses = t.TypeOf<typeof TTripRequestStatuses>;

export const TripRequestStatusesTaxiTitles: Record<TripRequestStatusesTaxi, string> = {
  TAXI_AWAITING_APPROVAL: `${CommonTitles.AWAITING_APPROVAL} (Такси)`,
  TAXI_APPROVED: `${CommonTitles.APPROVED} (Такси)`,
  TAXI_AWAITING_SEARCH: 'Ожидает поиска (Такси)',
  TAXI_DRIVER_SEARCH: 'Поиск водителя (Такси)',
  TAXI_DRIVER_FOUND: 'Водитель назначен (Такси)',
  TAXI_DRIVER_ON_THE_WAY: 'Водитель в пути (Такси)',
  TAXI_DRIVER_ARRIVED: 'Водитель ожидает в точке отправления (Такси)',
  TAXI_TRIP_IN_PROGRESS: 'Поездка началась (Такси)',
  TAXI_TRIP_FINISHED: 'Поездка завершена (Такси)',
  TAXI_CANCELLED: `${CommonTitles.CANCELED} (Такси)`,
  GENAI_CHECK: 'Проверка Gen-AI',
};

export const TripRequestStatusesPersonalTitles: Record<TripRequestStatusesPersonal, string> = {
  PERSONAL_AWAITING_APPROVAL: `${CommonTitles.AWAITING_APPROVAL} (ЛТ)`,
  PERSONAL_APPROVED: `${CommonTitles.APPROVED} (ЛТ)`,
  PERSONAL_AWAITING_SHARED_RIDE_APPROVAL: 'Согласование присоединения к СП (ЛТ)',
  PERSONAL_SHARED_RIDE_DECLINED: 'Присоединение не согласовано (ЛТ)',
  PERSONAL_TRIP_IN_PROGRESS: 'Поездка началась (ЛТ)',
  PERSONAL_AWAITING_TRIP_APPROVAL: 'Утверждение маршрута (ЛТ)',
  PERSONAL_ORDER_PAYMENT_FORMATION: 'Формирование приказа на выплату (ЛТ)',
  PERSONAL_PAYMENT_AWAITING: 'Ожидание выплаты (ЛТ)',
  PERSONAL_PAYMENT_DONE: 'Выплата произведена (ЛТ)',
  PERSONAL_PAYMENT_DECLINED: 'Выплата не произведена (ЛТ)',
  PERSONAL_CANCELLED: `${CommonTitles.CANCELED} (ЛТ)`,
  PERSONAL_TRIP_FINISHED: 'Поездка завершена (ЛТ)',
  GENAI_CHECK: 'Проверка Gen-AI',
};

export const TripRequestStatusesPublicTitles: Record<TripRequestStatusesPublic, string> = {
  PUBLIC_AWAITING_APPROVAL: `${CommonTitles.AWAITING_APPROVAL} (ОТ)`,
  PUBLIC_TRIP_CONFIRMATION: 'Подтверждение поездки (ОТ)',
  PUBLIC_AWAITING_AFFIRMATIVE: 'Ожидание подтверждения (ОТ)',
  PUBLIC_ORDER_PAYMENT_FORMATION: 'Формирование приказа на выплату (ОТ)',
  PUBLIC_PAYMENT_AWAITING: 'Ожидание выплаты (ОТ)',
  PUBLIC_PAYMENT_DONE: 'Выплата произведена (ОТ)',
  PUBLIC_PAYMENT_NOT_DONE: 'Выплата не произведена (ОТ)',
  PUBLIC_CANCELLED: `${CommonTitles.CANCELED} (ОТ)`,
  GENAI_CHECK: 'Проверка Gen-AI',
};

export const TripRequestStatusesCarSharingTitles: Record<TripRequestStatusesCarSharing, string> = {
  CARSHARING_AWAITING_APPROVAL: `${CommonTitles.AWAITING_APPROVAL} (Каршеринг)`,
  CARSHARING_APPROVED: `${CommonTitles.APPROVED} (Каршеринг)`,
  CARSHARING_DECLINED: 'Не согласована (Каршеринг)',
  CARSHARING_AWAITING_SEARCH: 'Поездка запланирована (Каршеринг)',
  CARSHARING_TRIP_IN_PROGRESS: 'Поездка началась (Каршеринг)',
  CARSHARING_AWAITING_TRIP_APPROVAL: 'Утверждение маршрута (Каршеринг)',
  CARSHARING_TRIP_FINISHED: 'Поездка завершена (Каршеринг)',
  CARSHARING_CANCELLED: 'Отменено (Каршеринг)',
};

export const TripRequestStatusesGroupTransferTitles: Record<TripRequestStatusesGroupTransfer, string> = {
  GROUP_TRANSFER_AWAITING_APPROVAL: `${CommonTitles.AWAITING_APPROVAL} (Трансфер)`,
  GROUP_TRANSFER_APPROVED: `${CommonTitles.APPROVED} (Трансфер)`,
  GROUP_TRANSFER_AWAITING_SEARCH: 'Ожидайте назначения водителя (Трансфер)',
  GROUP_TRANSFER_DRIVER_SEARCH: 'Поиск водителя (Трансфер)',
  GROUP_TRANSFER_DRIVER_FOUND: 'Водитель назначен (Трансфер)',
  GROUP_TRANSFER_DRIVER_ON_THE_WAY: 'Водитель в пути (Трансфер)',
  GROUP_TRANSFER_DRIVER_ARRIVED: 'Водитель ожидает в точке отправления (Трансфер)',
  GROUP_TRANSFER_TRIP_IN_PROGRESS: 'Поездка началась (Трансфер)',
  GROUP_TRANSFER_TRIP_FINISHED: 'Поездка завершена (Трансфер)',
  GROUP_TRANSFER_CANCELLED: 'Отменено (Трансфер)',
};

export const TripRequestStatusesTitles = R.mergeAll([
  TripRequestStatusesTaxiTitles,
  TripRequestStatusesPersonalTitles,
  TripRequestStatusesPublicTitles,
  TripRequestStatusesCarSharingTitles,
  TripRequestStatusesGroupTransferTitles,
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
  'PERSONAL_TRIP_FINISHED',
  'PUBLIC_CANCELLED',
  'PUBLIC_PAYMENT_DONE',
  'PUBLIC_PAYMENT_NOT_DONE',
];

export enum TripStatusesEnum {
  ALL = 'ALL',
  CARSHARING_AWAITING_APPROVAL = 'CARSHARING_AWAITING_APPROVAL',
  CARSHARING_APPROVED = 'CARSHARING_APPROVED',
  CARSHARING_DECLINED = 'CARSHARING_DECLINED',
  CARSHARING_AWAITING_SEARCH = 'CARSHARING_AWAITING_SEARCH',
  CARSHARING_TRIP_IN_PROGRESS = 'CARSHARING_TRIP_IN_PROGRESS',
  CARSHARING_AWAITING_TRIP_APPROVAL = 'CARSHARING_AWAITING_TRIP_APPROVAL',
  CARSHARING_TRIP_FINISHED = 'CARSHARING_TRIP_FINISHED',
  CARSHARING_CANCELLED = 'CARSHARING_CANCELLED',
  PUBLIC_AWAITING_APPROVAL = 'PUBLIC_AWAITING_APPROVAL',
  PUBLIC_TRIP_CONFIRMATION = 'PUBLIC_TRIP_CONFIRMATION',
  PUBLIC_AWAITING_AFFIRMATIVE = 'PUBLIC_AWAITING_AFFIRMATIVE',
  PUBLIC_ORDER_PAYMENT_FORMATION = 'PUBLIC_ORDER_PAYMENT_FORMATION',
  PUBLIC_PAYMENT_AWAITING = 'PUBLIC_PAYMENT_AWAITING',
  PUBLIC_PAYMENT_DONE = 'PUBLIC_PAYMENT_DONE',
  PUBLIC_PAYMENT_NOT_DONE = 'PUBLIC_PAYMENT_NOT_DONE',
  PUBLIC_CANCELLED = 'PUBLIC_CANCELLED',
  PERSONAL_AWAITING_APPROVAL = 'PERSONAL_AWAITING_APPROVAL',
  PERSONAL_APPROVED = 'PERSONAL_APPROVED',
  PERSONAL_AWAITING_SHARED_RIDE_APPROVAL = 'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  PERSONAL_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  PERSONAL_TRIP_IN_PROGRESS = 'PERSONAL_TRIP_IN_PROGRESS',
  PERSONAL_AWAITING_TRIP_APPROVAL = 'PERSONAL_AWAITING_TRIP_APPROVAL',
  PERSONAL_ORDER_PAYMENT_FORMATION = 'PERSONAL_ORDER_PAYMENT_FORMATION',
  PERSONAL_PAYMENT_AWAITING = 'PERSONAL_PAYMENT_AWAITING',
  PERSONAL_PAYMENT_DONE = 'PERSONAL_PAYMENT_DONE',
  PERSONAL_PAYMENT_DECLINED = 'PERSONAL_PAYMENT_DECLINED',
  PERSONAL_CANCELLED = 'PERSONAL_CANCELLED',
  PERSONAL_TRIP_FINISHED = 'PERSONAL_TRIP_FINISHED',
  TAXI_AWAITING_APPROVAL = 'TAXI_AWAITING_APPROVAL',
  TAXI_APPROVED = 'TAXI_APPROVED',
  TAXI_AWAITING_SEARCH = 'TAXI_AWAITING_SEARCH',
  TAXI_DRIVER_SEARCH = 'TAXI_DRIVER_SEARCH',
  TAXI_DRIVER_FOUND = 'TAXI_DRIVER_FOUND',
  TAXI_DRIVER_ON_THE_WAY = 'TAXI_DRIVER_ON_THE_WAY',
  TAXI_DRIVER_ARRIVED = 'TAXI_DRIVER_ARRIVED',
  TAXI_TRIP_IN_PROGRESS = 'TAXI_TRIP_IN_PROGRESS',
  TAXI_TRIP_FINISHED = 'TAXI_TRIP_FINISHED',
  TAXI_CANCELLED = 'TAXI_CANCELLED',
  GROUP_TRANSFER_AWAITING_APPROVAL = 'GROUP_TRANSFER_AWAITING_APPROVAL',
  GROUP_TRANSFER_APPROVED = 'GROUP_TRANSFER_APPROVED',
  GROUP_TRANSFER_AWAITING_SEARCH = 'GROUP_TRANSFER_AWAITING_SEARCH',
  GROUP_TRANSFER_DRIVER_SEARCH = 'GROUP_TRANSFER_DRIVER_SEARCH',
  GROUP_TRANSFER_DRIVER_FOUND = 'GROUP_TRANSFER_DRIVER_FOUND',
  GROUP_TRANSFER_DRIVER_ON_THE_WAY = 'GROUP_TRANSFER_DRIVER_ON_THE_WAY',
  GROUP_TRANSFER_DRIVER_ARRIVED = 'GROUP_TRANSFER_DRIVER_ARRIVED',
  GROUP_TRANSFER_TRIP_IN_PROGRESS = 'GROUP_TRANSFER_TRIP_IN_PROGRESS',
  GROUP_TRANSFER_TRIP_FINISHED = 'GROUP_TRANSFER_TRIP_FINISHED',
  GROUP_TRANSFER_CANCELLED = 'GROUP_TRANSFER_CANCELLED',
  GENAI_CHECK = 'GENAI_CHECK',
}

export enum TripStatusesChangeableEnum {
  TAXI_AWAITING_APPROVAL = 'TAXI_AWAITING_APPROVAL',
  PERSONAL_AWAITING_APPROVAL = 'PERSONAL_AWAITING_APPROVAL',
  PUBLIC_AWAITING_APPROVAL = 'PUBLIC_AWAITING_APPROVAL',
  CARSHARING_AWAITING_APPROVAL = 'CARSHARING_AWAITING_APPROVAL',
  GROUP_TRANSFER_AWAITING_APPROVAL = 'GROUP_TRANSFER_AWAITING_APPROVAL',
  TAXI_AWAITING_SEARCH = 'TAXI_AWAITING_SEARCH',
  GROUP_TRANSFER_AWAITING_SEARCH = 'GROUP_TRANSFER_AWAITING_SEARCH',
  CARSHARING_AWAITING_SEARCH = 'CARSHARING_AWAITING_SEARCH',
  PERSONAL_AWAITING_TRIP_APPROVAL = 'PERSONAL_AWAITING_TRIP_APPROVAL',
  PERSONAL_ORDER_PAYMENT_FORMATION = 'PERSONAL_ORDER_PAYMENT_FORMATION',
  PUBLIC_ORDER_PAYMENT_FORMATION = 'PUBLIC_ORDER_PAYMENT_FORMATION',
  PERSONAL_AWAITING_SHARED_RIDE_APPROVAL = 'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  PERSONAL_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  PERSONAL_PAYMENT_AWAITING = 'PERSONAL_PAYMENT_AWAITING',
  PUBLIC_TRIP_CONFIRMATION = 'PUBLIC_TRIP_CONFIRMATION',
  PUBLIC_PAYMENT_AWAITING = 'PUBLIC_PAYMENT_AWAITING',
  PUBLIC_AWAITING_AFFIRMATIVE = 'PUBLIC_AWAITING_AFFIRMATIVE',
  GENAI_CHECK = 'GENAI_CHECK',
}

export enum TripStatusesFinalEnum {
  TAXI_CANCELLED = 'TAXI_CANCELLED',
  PERSONAL_CANCELLED = 'PERSONAL_CANCELLED',
  PUBLIC_CANCELLED = 'PUBLIC_CANCELLED',
  CARSHARING_CANCELLED = 'CARSHARING_CANCELLED',
  GROUP_TRANSFER_CANCELLED = 'GROUP_TRANSFER_CANCELLED',
  PERSONAL_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  PUBLIC_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  TAXI_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  PERSONAL_PAYMENT_DECLINED = 'PERSONAL_PAYMENT_DECLINED',
  PUBLIC_PAYMENT_DECLINED = 'PERSONAL_PAYMENT_DECLINED',
  TAXI_PAYMENT_DECLINED = 'PERSONAL_PAYMENT_DECLINED',
  PUBLIC_PAYMENT_NOT_DONE = 'PUBLIC_PAYMENT_NOT_DONE',
  CARSHARING_DECLINED = 'CARSHARING_DECLINED',
}

export enum TripStatusesPositiveEnum {
  TAXI_APPROVED = 'TAXI_APPROVED',
  PERSONAL_APPROVED = 'PERSONAL_APPROVED',
  CARSHARING_APPROVED = 'CARSHARING_APPROVED',
  GROUP_TRANSFER_APPROVED = 'GROUP_TRANSFER_APPROVED',
  PUBLIC_PAYMENT_DONE = 'PUBLIC_PAYMENT_DONE',
  PERSONAL_PAYMENT_DONE = 'PERSONAL_PAYMENT_DONE',
}

export enum tripStatusesEnum {
  TAXI_TRIP_IN_PROGRESS = 'TAXI_TRIP_IN_PROGRESS',
  PERSONAL_TRIP_IN_PROGRESS = 'PERSONAL_TRIP_IN_PROGRESS',
  CARSHARING_TRIP_IN_PROGRESS = 'CARSHARING_TRIP_IN_PROGRESS',
  GROUP_TRANSFER_TRIP_IN_PROGRESS = 'GROUP_TRANSFER_TRIP_IN_PROGRESS',
  PERSONAL_TRIP_FINISHED = 'PERSONAL_TRIP_FINISHED',
  GROUP_TRANSFER_TRIP_FINISHED = 'GROUP_TRANSFER_TRIP_FINISHED',
  TAXI_TRIP_FINISHED = 'TAXI_TRIP_FINISHED',
  CARSHARING_TRIP_FINISHED = 'PERSONAL_TRIP_FINISHED',
  TAXI_DRIVER_SEARCH = 'TAXI_DRIVER_SEARCH',
  GROUP_TRANSFER_DRIVER_SEARCH = 'GROUP_TRANSFER_DRIVER_SEARCH',
  TAXI_DRIVER_FOUND = 'TAXI_DRIVER_FOUND',
  GROUP_TRANSFER_DRIVER_FOUND = 'GROUP_TRANSFER_DRIVER_FOUND',
  TAXI_DRIVER_ARRIVED = 'TAXI_DRIVER_ARRIVED',
  GROUP_TRANSFER_DRIVER_ARRIVED = 'GROUP_TRANSFER_DRIVER_ARRIVED',
  TAXI_DRIVER_ON_THE_WAY = 'TAXI_DRIVER_ON_THE_WAY',
  GROUP_TRANSFER_DRIVER_ON_THE_WAY = 'GROUP_TRANSFER_DRIVER_ON_THE_WAY',
}

export enum TripsRangeEnum {
  YESTERDAY = 'вчера',
  BEFORE_YESTERDAY = 'позавчера',
  WEEK = 'неделя',
  MONTH = 'месяц',
}

export const tripStatusOptions: LabeledValue<TripStatusesEnum | string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_APPROVAL,
    value: TripStatusesEnum.CARSHARING_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_APPROVED,
    value: TripStatusesEnum.CARSHARING_APPROVED,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_DECLINED,
    value: TripStatusesEnum.CARSHARING_DECLINED,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_SEARCH,
    value: TripStatusesEnum.CARSHARING_AWAITING_SEARCH,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.CARSHARING_TRIP_IN_PROGRESS,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_TRIP_APPROVAL,
    value: TripStatusesEnum.CARSHARING_AWAITING_TRIP_APPROVAL,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_TRIP_FINISHED,
    value: TripStatusesEnum.CARSHARING_TRIP_FINISHED,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_CANCELLED,
    value: TripStatusesEnum.CARSHARING_CANCELLED,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_AWAITING_APPROVAL,
    value: TripStatusesEnum.PUBLIC_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_TRIP_CONFIRMATION,
    value: TripStatusesEnum.PUBLIC_TRIP_CONFIRMATION,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_AWAITING_AFFIRMATIVE,
    value: TripStatusesEnum.PUBLIC_AWAITING_AFFIRMATIVE,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_ORDER_PAYMENT_FORMATION,
    value: TripStatusesEnum.PUBLIC_ORDER_PAYMENT_FORMATION,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_AWAITING,
    value: TripStatusesEnum.PUBLIC_PAYMENT_AWAITING,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_DONE,
    value: TripStatusesEnum.PUBLIC_PAYMENT_DONE,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_NOT_DONE,
    value: TripStatusesEnum.PUBLIC_PAYMENT_NOT_DONE,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_CANCELLED,
    value: TripStatusesEnum.PUBLIC_CANCELLED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_APPROVAL,
    value: TripStatusesEnum.PERSONAL_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_APPROVED,
    value: TripStatusesEnum.PERSONAL_APPROVED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
    value: TripStatusesEnum.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_SHARED_RIDE_DECLINED,
    value: TripStatusesEnum.PERSONAL_SHARED_RIDE_DECLINED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.PERSONAL_TRIP_IN_PROGRESS,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_TRIP_APPROVAL,
    value: TripStatusesEnum.PERSONAL_AWAITING_TRIP_APPROVAL,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_ORDER_PAYMENT_FORMATION,
    value: TripStatusesEnum.PERSONAL_ORDER_PAYMENT_FORMATION,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_AWAITING,
    value: TripStatusesEnum.PERSONAL_PAYMENT_AWAITING,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_DONE,
    value: TripStatusesEnum.PERSONAL_PAYMENT_DONE,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_DECLINED,
    value: TripStatusesEnum.PERSONAL_PAYMENT_DECLINED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_CANCELLED,
    value: TripStatusesEnum.PERSONAL_CANCELLED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_AWAITING_APPROVAL,
    value: TripStatusesEnum.TAXI_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_APPROVED,
    value: TripStatusesEnum.TAXI_APPROVED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_AWAITING_SEARCH,
    value: TripStatusesEnum.TAXI_AWAITING_SEARCH,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_SEARCH,
    value: TripStatusesEnum.TAXI_DRIVER_SEARCH,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_FOUND,
    value: TripStatusesEnum.TAXI_DRIVER_FOUND,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_ON_THE_WAY,
    value: TripStatusesEnum.TAXI_DRIVER_ON_THE_WAY,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_ARRIVED,
    value: TripStatusesEnum.TAXI_DRIVER_ARRIVED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.TAXI_TRIP_IN_PROGRESS,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_TRIP_FINISHED,
    value: TripStatusesEnum.TAXI_TRIP_FINISHED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_CANCELLED,
    value: TripStatusesEnum.TAXI_CANCELLED,
  },
];

export const taxiStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_AWAITING_APPROVAL,
    value: TripStatusesEnum.TAXI_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_APPROVED,
    value: TripStatusesEnum.TAXI_APPROVED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_AWAITING_SEARCH,
    value: TripStatusesEnum.TAXI_AWAITING_SEARCH,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_SEARCH,
    value: TripStatusesEnum.TAXI_DRIVER_SEARCH,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_FOUND,
    value: TripStatusesEnum.TAXI_DRIVER_FOUND,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_ON_THE_WAY,
    value: TripStatusesEnum.TAXI_DRIVER_ON_THE_WAY,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_DRIVER_ARRIVED,
    value: TripStatusesEnum.TAXI_DRIVER_ARRIVED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.TAXI_TRIP_IN_PROGRESS,
  },
];

export const taxiCompletedStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_CANCELLED,
    value: TripStatusesEnum.TAXI_CANCELLED,
  },
  {
    label: TripRequestStatusesTaxiTitles.TAXI_TRIP_FINISHED,
    value: TripStatusesEnum.TAXI_TRIP_FINISHED,
  },
];

export const personalStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_APPROVAL,
    value: TripStatusesEnum.PERSONAL_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_APPROVED,
    value: TripStatusesEnum.PERSONAL_APPROVED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
    value: TripStatusesEnum.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.PERSONAL_TRIP_IN_PROGRESS,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_TRIP_APPROVAL,
    value: TripStatusesEnum.PERSONAL_AWAITING_TRIP_APPROVAL,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_ORDER_PAYMENT_FORMATION,
    value: TripStatusesEnum.PERSONAL_ORDER_PAYMENT_FORMATION,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_AWAITING,
    value: TripStatusesEnum.PERSONAL_PAYMENT_AWAITING,
  },
];

export const personalCompletedStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_CANCELLED,
    value: TripStatusesEnum.PERSONAL_CANCELLED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_SHARED_RIDE_DECLINED,
    value: TripStatusesEnum.PERSONAL_SHARED_RIDE_DECLINED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_DECLINED,
    value: TripStatusesEnum.PERSONAL_PAYMENT_DECLINED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_FINISHED,
    value: TripStatusesEnum.PERSONAL_TRIP_FINISHED,
  },
  {
    label: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_DONE,
    value: TripStatusesEnum.PERSONAL_PAYMENT_DONE,
  },
];

export const allCancelStatuses: TitleMap = {
  PERSONAL_CANCELLED: {
    title: TripRequestStatusesPersonalTitles.PERSONAL_CANCELLED,
    description: '',
  },
  PERSONAL_SHARED_RIDE_DECLINED: {
    title: TripRequestStatusesPersonalTitles.PERSONAL_SHARED_RIDE_DECLINED,
    description: '',
  },
  PERSONAL_PAYMENT_DECLINED: {
    title: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_DECLINED,
    description: '',
  },
  TAXI_CANCELLED: {
    title: TripRequestStatusesTaxiTitles.TAXI_CANCELLED,
    description: '',
  },
  CARSHARING_CANCELLED: {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_CANCELLED,
    description: '',
  },
  CARSHARING_DECLINED: {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_DECLINED,
    description: '',
  },
  PUBLIC_CANCELLED: {
    title: TripRequestStatusesPublicTitles.PUBLIC_CANCELLED,
    description: '',
  },
  PUBLIC_PAYMENT_NOT_DONE: {
    title: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_NOT_DONE,
    description: '',
  },
  GROUP_TRANSFER_CANCELLED: {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_CANCELLED,
    description: '',
  },
};

export const publicStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_AWAITING_APPROVAL,
    value: TripStatusesEnum.PUBLIC_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_TRIP_CONFIRMATION,
    value: TripStatusesEnum.PUBLIC_TRIP_CONFIRMATION,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_AWAITING_AFFIRMATIVE,
    value: TripStatusesEnum.PUBLIC_AWAITING_AFFIRMATIVE,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_ORDER_PAYMENT_FORMATION,
    value: TripStatusesEnum.PUBLIC_ORDER_PAYMENT_FORMATION,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_AWAITING,
    value: TripStatusesEnum.PUBLIC_PAYMENT_AWAITING,
  },
];

export const publicCompletedStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_CANCELLED,
    value: TripStatusesEnum.PUBLIC_CANCELLED,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_NOT_DONE,
    value: TripStatusesEnum.PUBLIC_PAYMENT_NOT_DONE,
  },
  {
    label: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_DONE,
    value: TripStatusesEnum.PUBLIC_PAYMENT_DONE,
  },
];

export const CarSharingStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_APPROVAL,
    value: TripStatusesEnum.CARSHARING_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_APPROVED,
    value: TripStatusesEnum.CARSHARING_APPROVED,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_SEARCH,
    value: TripStatusesEnum.CARSHARING_AWAITING_SEARCH,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.CARSHARING_TRIP_IN_PROGRESS,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_TRIP_APPROVAL,
    value: TripStatusesEnum.CARSHARING_AWAITING_TRIP_APPROVAL,
  },
];

export const CarSharingCompletedStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_CANCELLED,
    value: TripStatusesEnum.CARSHARING_CANCELLED,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_TRIP_FINISHED,
    value: TripStatusesEnum.CARSHARING_TRIP_FINISHED,
  },
  {
    label: TripRequestStatusesCarSharingTitles.CARSHARING_DECLINED,
    value: TripStatusesEnum.CARSHARING_DECLINED,
  },
];

export const GroupTransferStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_AWAITING_APPROVAL,
    value: TripStatusesEnum.GROUP_TRANSFER_AWAITING_APPROVAL,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_APPROVED,
    value: TripStatusesEnum.GROUP_TRANSFER_APPROVED,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_AWAITING_SEARCH,
    value: TripStatusesEnum.GROUP_TRANSFER_AWAITING_SEARCH,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_SEARCH,
    value: TripStatusesEnum.GROUP_TRANSFER_DRIVER_SEARCH,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_FOUND,
    value: TripStatusesEnum.GROUP_TRANSFER_DRIVER_FOUND,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_ON_THE_WAY,
    value: TripStatusesEnum.GROUP_TRANSFER_DRIVER_ON_THE_WAY,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_ARRIVED,
    value: TripStatusesEnum.GROUP_TRANSFER_DRIVER_ARRIVED,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_TRIP_IN_PROGRESS,
    value: TripStatusesEnum.GROUP_TRANSFER_TRIP_IN_PROGRESS,
  },
];

export const GroupTransferCompletedStatuses: LabeledValue<string>[] = [
  {
    label: 'Все статусы',
    value: 'ALL',
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_TRIP_FINISHED,
    value: TripStatusesEnum.GROUP_TRANSFER_TRIP_FINISHED,
  },
  {
    label: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_CANCELLED,
    value: TripStatusesEnum.GROUP_TRANSFER_CANCELLED,
  },
];

export const selectedTransportTypeStatuses: TransportTypeStatuses = {
  TAXI: taxiStatuses,
  PERSONAL: personalStatuses,
  PUBLIC: publicStatuses,
  CARSHARING: CarSharingStatuses,
  GROUP_TRANSFER: GroupTransferStatuses,
};

export const selectedTransportTypeCompletedStatuses: TransportTypeStatuses = {
  TAXI: taxiCompletedStatuses,
  PERSONAL: personalCompletedStatuses,
  PUBLIC: publicCompletedStatuses,
  CARSHARING: CarSharingCompletedStatuses,
  GROUP_TRANSFER: GroupTransferCompletedStatuses,
};

export const taxiStatusesStepper = [
  {
    title: TripRequestStatusesTaxiTitles.TAXI_AWAITING_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_APPROVED,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_AWAITING_SEARCH,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_DRIVER_SEARCH,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_DRIVER_FOUND,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_DRIVER_ON_THE_WAY,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_DRIVER_ARRIVED,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_TRIP_IN_PROGRESS,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_TRIP_FINISHED,
    description: '',
  },
  {
    title: TripRequestStatusesTaxiTitles.TAXI_CANCELLED,
    description: '',
  },
];

export const personaStatusesStepper = [
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_APPROVED,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_IN_PROGRESS,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_TRIP_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_ORDER_PAYMENT_FORMATION,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_AWAITING,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_PAYMENT_DONE,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_FINISHED,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_CANCELLED,
    description: '',
  },
];

export const personaStatusesStepperShared = [
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_APPROVED,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_IN_PROGRESS,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_TRIP_FINISHED,
    description: '',
  },
  {
    title: TripRequestStatusesPersonalTitles.PERSONAL_CANCELLED,
    description: '',
  },
];

export const carsharingStatusesStepper = [
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_APPROVED,
    description: '',
  },
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_SEARCH,
    description: '',
  },
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_TRIP_IN_PROGRESS,
    description: '',
  },
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_AWAITING_TRIP_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_TRIP_FINISHED,
    description: '',
  },
  {
    title: TripRequestStatusesCarSharingTitles.CARSHARING_CANCELLED,
    description: '',
  },
];

export const publicStatusesStepper = [
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_AWAITING_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_TRIP_CONFIRMATION,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_AWAITING_AFFIRMATIVE,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_ORDER_PAYMENT_FORMATION,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_AWAITING,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_DONE,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_PAYMENT_NOT_DONE,
    description: '',
  },
  {
    title: TripRequestStatusesPublicTitles.PUBLIC_CANCELLED,
    description: '',
  },
];

export const GroupTransferStatusesStepper = [
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_AWAITING_APPROVAL,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_APPROVED,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_AWAITING_SEARCH,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_SEARCH,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_FOUND,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_ON_THE_WAY,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_DRIVER_ARRIVED,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_TRIP_IN_PROGRESS,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_TRIP_FINISHED,
    description: '',
  },
  {
    title: TripRequestStatusesGroupTransferTitles.GROUP_TRANSFER_CANCELLED,
    description: '',
  },
];

export const cancelStatuses = [
  'PERSONAL_CANCELLED',
  'TAXI_CANCELLED',
  'CARSHARING_CANCELLED',
  'PUBLIC_CANCELLED',
  'PUBLIC_PAYMENT_NOT_DONE',
  'PERSONAL_PAYMENT_DECLINED',
  'PERSONAL_SHARED_RIDE_DECLINED',
  'GROUP_TRANSFER_CANCELLED',
];

export const finishedStatuses = [
  'PERSONAL_TRIP_FINISHED',
  'CARSHARING_TRIP_FINISHED',
  'TAXI_TRIP_FINISHED',
  'GROUP_TRANSFER_TRIP_FINISHED',
  'PERSONAL_PAYMENT_DONE',
  'PERSONAL_PAYMENT_AWAITING',
  'PUBLIC_PAYMENT_AWAITING',
  'PUBLIC_PAYMENT_DONE',
  'PERSONAL_CANCELLED',
  'TAXI_CANCELLED',
  'CARSHARING_CANCELLED',
  'PUBLIC_CANCELLED',
  'GROUP_TRANSFER_CANCELLED',
];

export enum DataTitles {
  PLANNED_DATA = 'Плановые данные',
  ACTUAL_DATA = 'Фактические данные',
}

export const StatusesHintText = 'На статусах: Водитель назначен, Водитель в пути и Водитель ожидает,'
  + ' в детальной карточке заявки можно посмотреть расположение водителя на карте';
