import { Employee, IOHumanReadable } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { Moment } from 'moment';
import { IOWaypoint, RequestRoute, Segment } from 'shared/models/geo/types';
import { ApprovalStateStatuses, RequestType } from 'shared/models/types';

import { cargoListItem, totalSizes } from 'types/Cargo';
import { CargoRequestStatuses, CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { TransportTypeEnum } from '../TransportTypes/TransportTypes.interface';

export enum SortProperty {
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
  CREATION_DATE = 'CREATION_DATE',
  DESIRED_DATE = 'DESIRED_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
}

export enum Filters {
  all = 'all',
  authorId = 'authorId',
  senderId = 'senderId',
  recipientId = 'recipientId',
}

export enum KeyFilterState {
  regular = 'regular',
  once = 'once',
}

/**
 * Тип статуса истории заявки - может быть прошлым, будущим и текущим
 */
export enum CargoHistoryTypeEnum {
  PAST = 'PAST',
  NOW = 'NOW',
  FUTURE = 'FUTURE',
}

export type RangePickerArg = [Moment | null, Moment | null] | null;

export interface Settings {
  sortSetting: {
    directionAsc: boolean;
    property: SortProperty;
    sortingOrders: string;
  };
  tabFilterName?: string;
}

export interface TabSettings {
  cargoRegularRequestList: {
    active: Settings & { tabFilterName?: string };
    final: Settings & { tabFilterName?: string };
  };
  cargoRequestList: {
    active: Settings;
    final: Settings;
  };
  cargoMultipleApprovalList: {
    active: Settings;
    final: Settings;
  };
}

/**
 * Статус истории заявки
 */
export const CargoHistory = t.type({
  status: CargoRequestStatuses,
  date: t.number,
  type: ioTypeFromEnum<CargoHistoryTypeEnum>('CargoHistoryTypeEnum', CargoHistoryTypeEnum),
});

export type CargoHistoryType = t.TypeOf<typeof CargoHistory>;

export enum Periodicity {
  week = 'WEEK',
  month = 'MONTH',
  quarter = 'QUARTER',
}

const Period = t.intersection([
  t.type({
    periodType: ioTypeFromEnum<Periodicity>('Periodicity', Periodicity),
    dayOfWeek: t.array(t.number),
    weekOfMonth: t.array(t.number),
    monthOfQuartal: t.array(t.number),
    beginDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    cron: t.string,
    countRequest: t.number,
    cost: t.number,
  }),
]);

export type PeriodType = t.TypeOf<typeof Period>;

const TransportType = t.type({
  id: t.string,
  name: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
  nameRus: t.string,
});

const PriceDetails = t.type({
  baseCost: t.number,
  expressCost: t.number,
  loaderCost: t.number,
});

const Package = t.type({
  id: t.string,
  count: t.number,
  name: t.string,
});

export type PackageType = t.TypeOf<typeof Package>;

const CarInfo = t.type({
  auto: t.string,
  carDriver: t.string,
  carDriverPhone: t.number,
  registrationNumber: t.string,
});

export type CarInfoType = t.TypeOf<typeof CarInfo>;

const CalculatedTariff = t.intersection([
  t.type({
    id: t.string,
    cost: t.number,
    distance: t.number,
    priceDetails: PriceDetails,
    deliveryTime: t.number,
    transportType: TransportType,
    loaders: t.number,
  }),
  t.partial({
    active: t.boolean,
  }),
]);

export const Pack = t.type({
  id: t.string,
  name: t.string,
  unit: t.string,
});

export type Pack = t.TypeOf<typeof Pack>;

export const PackData = t.type({
  content: t.array(Pack),
});

export type PackData = t.TypeOf<typeof PackData>;

export const CargoRequest = t.intersection([
  IOHumanReadable,
  t.type({
    id: t.string,
    author: t.union([Employee, t.null]),
    sender: t.union([Employee, t.null]),
    recipient: t.union([Employee, t.null]),
    desiredDate: t.number,
    deliveryTimeDate: t.number,
    creationTime: t.union([t.string, t.number]),
    approvedBy: t.array(Employee),
    express: t.boolean,
    totalSizes,
    listCargo: t.array(cargoListItem),
  }),
  t.partial({
    recipient2: Employee,
    recipient3: Employee,
    comment: t.string,
    commentEng: t.string,
    senderOrganization: t.string,
    recipientOrganization: t.string,
    humanReadableId: t.string,
    restOfLimit: t.number,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    requestType: ioTypeFromEnum<RequestType>('RequestType', RequestType),
    packages: t.array(Package),
    loaders: t.number,
    transportTypeRus: t.string,
    transportTypeId: t.string,
    status: CargoRequestStatuses,
    expected: RequestRoute,
    segments: t.array(Segment),
    sourceLoaders: t.boolean,
    destinationLoaders: t.boolean,
    tariffId: t.string,
    length: t.number,
    width: t.number,
    height: t.number,
    volume: t.number,
    weight: t.number,
    occupiedPlacesCount: t.number,
    nextDelivery: t.number,
    cargoDetails: t.array(cargoListItem),
    period: Period,
    active: t.boolean,
    creationTime: t.union([t.number, t.string, t.undefined]),
    approvalState: ApprovalStateStatuses,
    allCost: t.number,
    totalCost: t.number,
    resolution: t.string,
    calculatedTariff: CalculatedTariff,
    sourceLoadersCount: t.number,
    destinationLoadersCount: t.number,
    internalNote: t.string,
    waypoints: t.array(IOWaypoint),
    carInfo: CarInfo,
    qrs: t.array(t.string),
  }),
]);

export type CargoRequestType = t.TypeOf<typeof CargoRequest>;

export interface CargoRegularRequestNewType {
  id: string;
  humanReadableId: string;
  period: PeriodType;
  template: Partial<CargoRequestType>;
  active: boolean;
  creationTime: string | number;
  status: CargoRequestStatusesType;
  approvalState: ApprovalStateStatuses;
  approvedBy: Employee[];
  allCost: number;
  source: string;
}

export interface CargoRegularRequestResponse {
  active: boolean;
  approvalState: ApprovalStateStatuses;
  creationTime: string | number;
  humanReadableId: string;
  id: string;
  period: PeriodType;
  status: CargoRequestStatusesType;
  template: Partial<CargoRequestType>;
  totalCost: number;
}

export type CargoRequestNewType = Pick<
  CargoRequestType,
  'author' | 'sender' | 'recipient' | 'transportType' | 'expected'
> &
Partial<CargoRequestType>;

export const CargoRequestJournal = t.intersection(
  [
    IOHumanReadable,
    t.type({
      id: t.string,
      desiredDate: t.number,
      creationTime: t.union([t.string, t.number]),
      express: t.boolean,
      approvedBy: t.array(Employee),
      transportTypeRus: t.string,
      transportType: t.string, // тут нужен enum INTERREGIONAL
      listCargo: t.array(t.string), // тут нужен enum FURNITURE
      approvalState: ApprovalStateStatuses, // тут нужен enum APPROVED
      approvalDate: t.array(t.string), // тут нужен enum APPROVED
      status: t.string, // CARGO_AWAITING_TRANSFER
      waypoints: t.array(IOWaypoint),
      internalNote: t.string,
      requestType: ioTypeFromEnum<RequestType>('RequestType', RequestType), // тут нужен enum REQUEST_TYPE
    }),
    t.partial({
      comment: t.string,
      packages: t.array(Package),
      loaders: t.number,
    }),
  ]);

export type CargoRequestJournalType = t.TypeOf<typeof CargoRequestJournal>;

const PageSetting = t.type({
  page: t.number,
  size: t.number,
});

const SortSetting = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export const FilterSettings = t.partial({
  requestHumanId: t.string,
  requestStatusSet: t.array(CargoRequestStatuses),
  desiredDate: t.array(t.string),
  shipmentTime: t.array(t.string),
  creationDate: t.array(t.string),
  regionTo: t.array(t.string),
  regionFrom: t.array(t.string),
  regionRoute: t.array(t.string),
  transportTypeEnum: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
  role: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});

export type FilterSettingsType = t.TypeOf<typeof FilterSettings>;
