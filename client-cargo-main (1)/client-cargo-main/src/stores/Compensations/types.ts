import { Employee, IOHumanReadable } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';

import { CargoRequestStatuses, CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum TransportTypeEnum {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
}

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

export interface Settings {
  sortSetting: {
    directionAsc: boolean;
    property: SortProperty;
    sortingOrders: string;
  };
  tabFilterName?: string;
}

export interface IFilters {
  authorId: string;
  senderId: string;
  recipientId: string;
}

export interface ICompensationRequestSearch extends Partial<IFilters> {
  pageSetting?: { page: number; size: number };
  desiredDate?: { start?: number | null; end?: number | null };
  creationDate?: { start?: number | null; end?: number | null };
  page?: { sort: { sorted: boolean } };
  sortSetting?: { directionAsc: boolean; property?: string };
  transportTypeEnum?: TransportTypeEnum;
  requestStatusSet?: CargoRequestStatusesType[];
}

export const CompensationRequest = t.intersection([
  IOHumanReadable,
  t.type({
    id: t.string,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    recipient: t.union([Employee, t.null]),
    creationTime: t.union([t.string, t.number]),
    status: CargoRequestStatuses,
    cost: t.number,
  }),
]);

export type CompensationRequestType = t.TypeOf<typeof CompensationRequest>;
