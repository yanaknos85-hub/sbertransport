import { Value } from 'shared/components/DateInput/types';
import {
  DateRange,
  DateRangeISO,
  PublicUIVisibilityDTO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { CargoRegistryFilters, SortSettings } from 'stores/CargoRegistry/CargoRegistry.interface';
import { PageSetting } from '../../TaxiRegistry/types/types';

export interface SortSetting {
  property: string;
  directionAsc: boolean;
}
export interface Filters {
  requestHumanId?: string;
  transportType?: string[];
  contractorSet?: string[];
  authorFIO?: string;
  requestStatusSet?: string[];
  deadlineDate?: string;
  desiredDate?: Value;
  creationDate?: Value;
  changeDate?: Value;
  sortSetting?: SortSetting;
  pageSetting?: { page: number; size: number };
  organizationSet?: string[];
  department1?: string[];
  department2?: string[];
  department3?: string[];
  department4?: string[];
  department5?: string[];
  department6?: string[];
}

export interface BusinessJournalRecord {
  id: string;
  status: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  filterRequest: any; // TODO поправить
  creationTime: string;
  actions: string;
  url?: string;
}
export interface SorterT {
  column?: {
    dataIndex?: string;
    defaultSortOrder?: undefined;
    key?: string;
    sortProperty?: string;
    sorter?: boolean;
    sorting?: boolean;
    title?: string;
  };
  columnKey?: string;
  field?: string;
  order?: string;
}

export interface RequestBodyPublic {
  withFilters?: boolean;
  withView?: boolean;
  filters?: string;
  organizationId?: string;
  publicUIVisibilityDTO?: PublicUIVisibilityDTO;
  [key: string]: unknown;
}

export interface RequestBodyPublicParams {
  withFilters: boolean;
  withView: boolean;
  organizationId: string;
  requestHumanId: string;
  tariffIdSet: string[];
  requestStatusSet: string[];
  paymentPeriod: number;
  compensationType: string[];
  purposeSet: { id: string }[];
  ratingMarkSet?: string[];
  creationDate: DateRange | DateRangeISO;
  orderPaymentFormationStartDate: DateRange | DateRangeISO;
  balanceUnitSet: number[];
  costCenter: string;
  employeeFIO: string;
  personnelNumber: string;
  departmentCode: string;
  sortSetting: SortSetting;
  pageSetting: PageSetting;
  publicUIVisibilityDTO: PublicUIVisibilityDTO;
  coopTrip: boolean;
  sharedRideId: string;
  empty: boolean;
}

export interface reportParams {
  reportType: 'REGISTRY' | 'COMPENSATIONS';
  withFilters: boolean;
  withView: boolean;
  filters: CargoRegistryFilters;
  columnsVisibility?: PublicUIVisibilityDTO;
}

export type DescriptionsField = [string, string | number];

export type Records = DescriptionsField[];

export interface FilterRequest {
  status?: string[];
  organizationSet?: string[];
  empty?: boolean;
  requestHumanId?: string;
  cargoTransportType?: string[];
  contractorSet?: string[];
  authorFIO?: string;
  requestStatusSet?: string[];
  deadlineDate?: boolean;
  desiredDate?: string;
  creationDate?: string;
  sortSetting?: SortSettings;
  organizationId?: string;
  changeDate?: DateRangeISO;
  department1?: string[];
  department2?: string[];
  department3?: string[];
  department4?: string[];
  department5?: string[];
  department6?: string[];
}
