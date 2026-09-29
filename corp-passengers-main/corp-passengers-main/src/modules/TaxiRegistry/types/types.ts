/* eslint-disable no-use-before-define */
import { RangeNumber, RegisterSearchQuery } from 'api/register-search';
import { Value } from 'shared/components/DateInput/types';

export type MomentDates = [moment.Moment, moment.Moment];

export interface TaxiRegistryFilter {
  withFilters?: boolean;
  requestHumanId?: string;
  coopTrip?: number | boolean;
  requestStatusSet?: string[];
  expectedCost?: RangeNumber;
  economyPercent?: RangeNumber;
  passengerCountSet?: number[];
  contractorSet?: string[];
  tariffIdSet?: string[];
  factDistance?: RangeNumber;
  ratingMarkSet?: number[];
  creationDate?: Value;
  desiredDateRange?: Value;
  costCenter?: string;
  employeeFIO?: string;
  personnelNumber?: string;
  departmentCode?: string;
  sharedRideOwnerFIO?: string;
  sortSetting?: SortSetting;
  pageSetting?: PageSetting;
  purposeSet?: string[];
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

export interface TableRecord {
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

export const SessionSortProperties = {
  SortProperty: 'sortProperty',
  DirectionAsc: 'directionAsc',
};
export enum TripsTabsFilters {
  planned = 'planned',
  final = 'final',
}

export interface SortSetting {
  property: string;
  directionAsc: boolean;
}

export type TTripsTabsFilters = keyof typeof TripsTabsFilters;
export type FilterValues = Omit<RegisterSearchQuery, 'pageSetting' | 'sortSetting'>;

export interface Month {
  id: number;
  title: string;
  year: number;
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

export enum ItinerantType {
  FULL = 'FULL',
  PARTIAL = 'PARTIAL',
}

export const ItinerantTypeDescriptions: Record<ItinerantType, string> = {
  FULL: 'Разъездной',
  PARTIAL: 'Частично разъездной',
};

export enum RangeObjectEnum {
  cost = 'cost',
  distance = 'distance',
  time = 'time',
}

export enum CostField {
  factCost = 'factCost',
  expectedCost = 'expectedCost',
}

export enum PercentFields {
  economyPercent = 'economyPercent',
}

export enum TimeFields {
  waypointWaitTime = 'waypointWaitTime',
}

export type DescriptionsField = [string | JSX.Element, string | string[] | number | number[] | null | undefined];

export type Records = DescriptionsField[];

export interface CoopTripFieldProps { description: string | number | string[] | number[] | null | undefined }

export enum FinishStatusTripFields {
  'Фактическое время ожидания по адресу отправления',
  'Фактическая стоимость, руб',
  'Фактическая дальность, км',
  'Фактическая длительность, мин',
  'Дата внесения фактических параметров поездки',
}

export enum PassengerInfo {
  fullName = 'fullName',
  approvedBy = 'approvedBy',
  department = 'department',
  position = 'position',
  departureAddress = 'departureAddress',
  destinationAddress = 'destinationAddress',
  intermediateAddress = 'intermediateAddress',
  departureWaitTime = 'departureWaitTime',
  destinationWaitTime = 'destinationWaitTime',
  purpose = 'purpose',
  plannedCost = 'plannedCost',
  cost = 'cost',
  plannedDistance = 'plannedDistance',
  distance = 'distance',
  plannedTime = 'plannedTime',
  time = 'time',
  limit = 'limit',
  comment = 'comment',
  rating = 'rating',
  VspGosbTbExist = 'VspGosbTbExist',
  ratingComment = 'ratingComment',
  minTaxiTariffCost = 'minTaxiTariffCost',
  totalSharedRequestCount = 'totalSharedRequestCount',
  passengerCount = 'passengerCount',
  departmentEconomy = 'departmentEconomy',
  limitDebit = 'limitDebit',
  financialImpact = 'financialImpact',
  financialImpactShared = 'financialImpactShared',
  financialImpactJoined = 'financialImpactJoined',
}

export interface reportParams {
  filters: RegisterSearchQuery;
  withFilters: boolean;
}

export interface RequestBodyParamsTaxi {
  withFilters: boolean;
  filters: string;
}

export interface CoopTripPassengerInfo {
  fullName: string;
  approvedBy: string;
  department: string;
  position: string;
  departureAddress: string;
  destinationAddress: string;
  intermediateAddress: string;
  departureWaitTime: string;
  destinationWaitTime: string;
  purpose: string;
  plannedCost: string;
  cost: string;
  plannedDistance: string;
  distance: string;
  plannedTime: string;
  time: string;
  limit: string;
  comment: string;
  rating: string | number;
  VspGosbTbExist: string;
  creationTime: number;
  ratingComment: string;
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
