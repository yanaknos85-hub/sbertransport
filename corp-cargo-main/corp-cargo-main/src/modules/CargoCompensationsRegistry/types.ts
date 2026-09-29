import * as t from 'io-ts';
import { ColumnType } from 'antd/lib/table/interface';
import { SearchRequest } from 'stores/CargoRegistry/CargoRegistry.interface';
import { VisibleFields } from './constants';

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

const Sorted = t.type({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
  sort: Sorted,
});

export interface FilterValues {
  humanReadableId: string;
  approvalDateRange: { start: string; end: string } | undefined;
  formationDateRange: { start: string; end: string } | undefined;
  statusSet: string[];
  contractorSet: string[];
  transportType?: string;
}

export type TransformedFilterValues = Partial<FilterValues>;

export type ColumnVisibilitySettings = Partial<Record<VisibleFields, boolean>>;

export type RequestBodyCargoParams = { cargoUIVisibilityDTO?: ColumnVisibilitySettings } & SearchRequest;

export const Compensation = t.type({
  id: t.string,
  humanReadableId: t.string,
  routeNumber: t.string,
  courier: t.string,
  department: t.string,
  mvz: t.string,
  cost: t.number,
  status: t.string,
  deadline: t.string,
  approvedBy: t.string,
  approvalDate: t.string,
  formationDate: t.string,
});

export type CompensationType = t.TypeOf<typeof Compensation>;

export const SearchCompensationResponse = t.type({
  totalElements: t.number,
  totalPages: t.number,
  size: t.number,
  number: t.number,
  numberOfElements: t.number,
  first: t.boolean,
  last: t.boolean,
  empty: t.boolean,
  sort: Sorted,
  pageable: Pageable,
  content: t.array(Compensation),
});
export type SearchCompensationResponse = t.TypeOf<typeof SearchCompensationResponse>;

export enum Statuses {
  COMPENSATION_IN_PROCESS = 'COMPENSATION_IN_PROCESS',
  COMPENSATION_APPROVED = 'COMPENSATION_APPROVED',
  COMPENSATION_WAITING_FOR_PAYMENT = 'COMPENSATION_WAITING_FOR_PAYMENT',
  COMPENSATION_COMPLETED = 'COMPENSATION_COMPLETED',
  COMPENSATION_REJECTED = 'COMPENSATION_REJECTED',
}

export const StatusNames: TKeys<string> = {
  [Statuses.COMPENSATION_IN_PROCESS]: 'В обработке',
  [Statuses.COMPENSATION_APPROVED]: 'Выплата компенсации согласована',
  [Statuses.COMPENSATION_WAITING_FOR_PAYMENT]: 'Ожидание выплаты',
  [Statuses.COMPENSATION_COMPLETED]: 'Компенсация выплачена',
  [Statuses.COMPENSATION_REJECTED]: 'Компенсация отклонена',
};

export interface DepartmentLevels {
  department1: string[];
  department2: string[];
  department3: string[];
  department4: string[];
  department5: string[];
  department6: string[];
  departmentLevel: number;
}
