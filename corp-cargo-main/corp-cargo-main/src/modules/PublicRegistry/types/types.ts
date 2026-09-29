import { Value } from 'shared/components/DateInput/types';
import {
  DateRange,
  DateRangeISO,
  PublicRegistryFilters,
  PublicUIVisibilityDTO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { PageSetting } from '../../TaxiRegistry/types/types';

export interface SortSetting {
  property: string;
  directionAsc: boolean;
}

export interface Filters {
  requestHumanId?: string;
  tariffIdSet?: string[];
  requestStatusSet?: string[];
  paymentPeriod?: number;
  compensationType?: string[];
  purposeSet?: string[];
  ratingMarkSet?: string[];
  creationDate?: Value;
  orderPaymentFormationStartDate?: Value;
  balanceUnitSet?: number[];
  costCenter?: string;
  employeeFIO?: string;
  personnelNumber?: string;
  departmentCode?: string;
  sortSetting?: SortSetting;
  pageSetting?: { page: number; size: number };
}

export interface JournalRecord {
  id: string;
  requestIdVisible: string;
  mvzVisible: string;
  desiredDateVisible: string;
  controlPeriodOfPayment: string;
  approveDateVisible: string;
  passengerFioVisible: string;
  requestStatusVisible: string;
  plannedPriceVisible: string;
  expectedDistanceVisible: string;
  paymentPeriodVisible: string | number;
  departmentCodeVisible: string;
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
  filters: PublicRegistryFilters;
  columnsVisibility?: PublicUIVisibilityDTO;
}

export type DescriptionsField = [string, string | number];

export type Records = DescriptionsField[];
