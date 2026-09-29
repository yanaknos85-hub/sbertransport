import { ColumnProps } from 'antd/lib/table';
import { CostRange, SortFields } from 'api/register-search';
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

// eslint-disable-next-line no-use-before-define
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
