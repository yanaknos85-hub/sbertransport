import * as t from 'io-ts';
import { Value } from 'shared/components/DateInput/types';
import { RegistrySearchQuery } from 'stores/GroupTransferRegistry/GroupTransferRegistry';

export const RangeNumber = t.strict({
  start: t.number,
  end: t.number,
});
export type RangeNumber = t.TypeOf<typeof RangeNumber>;

export interface GroupTransferRegistryFilter {
  withFilters?: boolean;
  requestHumanId?: string;
  requestStatusSet?: string[];
  expectedCost?: RangeNumber;
  contractorSet?: string[];
  tariffIdSet?: string[];
  regionSet?: string[];
  purposeSet?: { label: string; value: string }[];
  factDistance?: RangeNumber;
  expectedDistance?: RangeNumber;
  groupTransferClassList?: string[];
  ratingMarkSet?: number[];
  creationDate?: Value;
  desiredDateRange?: Value;
  costCenter?: string;
  employeeFIO?: string;
  personnelNumber?: string;
  departmentCode?: string;
  sortSetting?: SortSetting;
  pageSetting?: PageSetting;
}

export interface SortSetting {
  property: string;
  directionAsc: boolean;
}

export interface InfoContractor {
  contractorId: string;
  monthName: string;
  contractorName: string;
}

export interface PageSetting {
  page: number;
  size: number;
}

export interface PageOptions {
  pageSetting: PageSetting;
  onPaginationChange: (page: number, size?: number | undefined) => void;
}

export enum RangeObjectEnum {
  cost = 'cost',
  distance = 'distance',
  time = 'time',
}

export interface reportParams {
  filters: RegistrySearchQuery;
  withFilters: boolean;
}

export interface RequestBodyParamsGroupTransfer {
  withFilters: boolean;
  filters: string;
}

export enum TransportTypes {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  COURIER = 'COURIER',
  DEDICATED = 'DEDICATED',
  INDIVIDUAL = 'INDIVIDUAL',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  OFFICIAL = 'OFFICIAL',
  SPECIAL = 'SPECIAL',
  PRIVATE = 'PRIVATE',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}
