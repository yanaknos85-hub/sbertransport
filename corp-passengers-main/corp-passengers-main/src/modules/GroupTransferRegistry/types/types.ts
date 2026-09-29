import * as t from 'io-ts';
import { Value } from 'shared/components/DateInput/types';
import { RegistrySearchQuery } from 'stores/GroupTransferRegistry/GroupTransferRegistry';

export enum SortFields {
  PASSENGER_FULL_NAME = 'PASSENGER_FULL_NAME',
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
  DESIRED_DATE = 'DESIRED_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
  REQUEST_ID = 'REQUEST_ID',
  CREATION_DATE = 'CREATION_DATE',
}

export interface TableRecord {
  id: string;
  requestIdVisible: string | null;
  mvzVisible: string;
  desiredDateVisible: string;
  controlPeriodOfPayment: string;
  orderPaymentFormationStartDateVisible: string;
  passengerFioVisible: string | string[];
  requestStatusVisible: string | null;
  plannedPriceVisible: string | number;
  tripFactPriceVisible: string | number;
  plannedRangeVisible: string | number;
  paymentPeriodVisible: string | number;
  departmentCodeVisible: string;
  sharedRideOwnerVisible: string | null | undefined;
  carVisible: string;
  carEngineVolumeVisible: string | number;
  tripTypeVisible: string;
}

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
  savings?: string;
  requestClosedDatetime?: Value;
  department1?: string[];
  department2?: string[];
  department3?: string[];
  department4?: string[];
  department5?: string[];
  department6?: string[];
  employeeOrganizationSet?: string[];
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

export interface DepartmentLevels {
  department1: string[];
  department2: string[];
  department3: string[];
  department4: string[];
  department5: string[];
  department6: string[];
  departmentLevel: number;
}

export type SettingType = [string, { active: boolean; index: number }];
