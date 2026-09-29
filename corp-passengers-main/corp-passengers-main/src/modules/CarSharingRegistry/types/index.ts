/* eslint-disable no-use-before-define */
import { ColumnProps } from 'antd/lib/table';
import { CostRange, RegisterSearchQuery, SortFields } from 'api/register-search';
import * as t from 'io-ts';
import { CarSharingReportFilters, DateRangeISO, DistanceRange } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { VisibleFields } from '../constants';

const PageSetting = t.type({
  page: t.number,
  size: t.number,
});

export type Row = {
  id: string;
} & { [key in VisibleFields]: string };

export const SortSetting = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export type PageSetting = t.TypeOf<typeof PageSetting>;

export interface PageOptions {
  pageSetting: PageSetting;
  onPaginationChange: (page: number, size?: number | undefined) => void;
}

export interface reportParams {
  filters: CarSharingReportFilters;
  withFilters: boolean;
}

export type SortSetting = t.TypeOf<typeof SortSetting>;

export type CarSharingRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export interface TableRecord {
  id: string;
  requestIdVisible: string;
  mvzVisible: string;
  desiredDateVisible: string;
  approveDateVisible: string;
  employeeFioVisible: string;
  requestStatusVisible: string;
  totalCostVisible: string;
  drivingLengthVisible: string;
  organizationalUnitCode: string;
  carVisible: string;
}

export interface RequestBodyCarSharing {
  withFilters: boolean;
  filters: string;
}

export interface RequestBodyCarSharingParams {
  withFilters?: boolean;
  organizationId?: string;
  requestHumanId?: string;
  requestStatusSet?: string[];
  creationDate?: DateRangeISO;
  purposeSet?: { id: string }[];
  expectedCost?: CostRange;
  expectedDistance?: DistanceRange;
  employeeFIO?: string;
  costCenter?: string;
  contractorSet?: string[];
  employeePositionSet?: string[];
  employeeDepartmentSet?: string[];
  organizationSetting?: { employeeOrganizationSet: string[] };
  departureAddress?: string;
  destinationAddress?: string;
  employeeItinerantTypeSet?: string[];
  sortSetting?: SortSetting;
  pageSetting?: PageSetting;
  ratingMarkSet?: number[];
  finishedDate?: DateRangeISO;
  empty?: boolean;
  desiredDate?: DateRangeISO;
}

export interface DepartmentLevels {
  department1: string[];
  department2: string[];
  department3: string[];
  department4: string[];
  department5: string[];
  department6: string[];
  departmentLevel: number;
}

export interface TableRecordCarSharing {
  requestIdVisible: string;
  mvzVisible: string;
  desiredDateVisible: string;
  deadlineVisible: string;
  driverArrivedDatetimeVisible: string;
  counterpartyNameVisible: string;
  approveDateVisible: string;
  passengerFioVisible: string;
  requestStatusVisible: string;
  plannedPriceVisible: string;
  tripFactPriceVisible: number | string;
  expectedDistanceVisible: number | string;
  organizationalUnitCode: number | string;
  carVisible: string;
  tripTypeVisible: string;
}

export type FilterValues = Omit<RegisterSearchQuery, 'pageSetting' | 'sortSetting'>;
