import { ColumnType } from 'antd/lib/table/interface';
import { DateRangeISO } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { VisibleFields } from './constants';
import * as t from 'io-ts';

export type TKeys<T> = {
  [key in string]: T;
};

export type Row = {
  id: string;
} & { [key in VisibleFields]?: string | number };

export type Column = Omit<ColumnType<Row>, 'dataIndex'> & {
  dataIndex: VisibleFields;
  sortProperty?: string;
};

export enum Statuses {
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_PLANNING_FINISHED = 'CARGO_PLANNING_FINISHED',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export const StatusNames: TKeys<string> = {
  [Statuses.CARGO_PLANNING]: 'Планирование',
  [Statuses.CARGO_PLANNING_FINISHED]: 'Планирование завершено',
  [Statuses.CARGO_AWAITING_DATA]: 'Отправлено контрагенту',
  [Statuses.CARGO_AWAITING_TRANSFER]: 'На сборе',
  [Statuses.CARGO_TRANSFER_FINISHED]: 'Доставка',
  [Statuses.CARGO_SHIPMENT_FINISHED]: 'Доставлено',
  [Statuses.CARGO_CANCELED]: 'Отменено',
};

export interface FilterValues {
  humanReadableId: string;
  regionFrom: string[];
  regionTo: string[];
  creationDateRange: { start: string; end: string };
  desiredDateRange: { start: string; end: string };
  statusSet: string[];
  contractorSet: string[];
  organizationId: string;
  transportType?: string[];
}

export type TransformedFilterValues = Record<string, string | string[] | DateRangeISO | undefined>;

export type ColumnVisibilitySettings = Partial<Record<VisibleFields, boolean>>;

const PageSetting = t.type({
  page: t.number,
  size: t.number,
});

const SortSetting = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

const MonitorFilters = t.type({
  humanReadableId: t.string,
  statusSet: t.string,
  desiredDateRange: t.array(t.string),
  creationDateRange: t.array(t.string),
  regionFrom: t.array(t.string),
  regionTo: t.array(t.string),
  employeeId: t.string,
  autoId: t.string,
  authorEmployeeId: t.string,
  departmentId: t.string,
  organizationId: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});

export type MonitorFiltersType = t.TypeOf<typeof MonitorFilters>;

const Sort = t.type({
  empty: t.boolean,
  sorted: t.boolean,
  unsorted: t.boolean,
});

const Pageable = t.type({
  pageNumber: t.number,
  pageSize: t.number,
  sort: Sort,
  offset: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

export const Route = t.type({
  id: t.string,
  humanReadableId: t.string,
  routelistIdVisible: t.string,
  author: t.string,
  status: t.string,
  driver: t.string,
  driverPhone: t.string,
  carInfo: t.string,
  timeZone: t.string,
  creationTime: t.number,
  shipmentTime: t.string,
  desiredDate: t.number,
  active: t.boolean,
  cost: t.number,
  distance: t.number,
  plannedDistance: t.number,
  volume: t.number,
  weight: t.number,
  loaders: t.number,
  actualCost: t.number,
  organizations: t.array(t.string),
  capacity: t.number,
  waypointCount: t.number,
  contractorName: t.string,
  plannedCost: t.number,
  transportType: t.string,
});

// Описание ответа сущности «Маршрут»
export const SearchMonitorRouteResponse = t.type({
  content: t.array(Route),
  pageable: Pageable,
  totalElements: t.number,
  totalPages: t.number,
  last: t.boolean,
  size: t.number,
  number: t.number,
  sort: Sort,
  numberOfElements: t.number,
  first: t.boolean,
  empty: t.boolean,
});

export type RouteType = t.TypeOf<typeof Route>;

export type MonitorRouteResponseType = t.TypeOf<typeof SearchMonitorRouteResponse>;
