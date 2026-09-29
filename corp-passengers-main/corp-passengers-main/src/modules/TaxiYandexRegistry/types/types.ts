import { Value } from 'shared/components/DateInput/types';
import {
  PageSetting, SortSetting, YandexTaxiReportSearchQuery
} from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import { PublicUIVisibilityDTO } from 'stores/PublicRegistry/models/PublicRegistry.interface';

export interface LabeledValue<T = string | number> {
  key?: string;
  value: T;
  label: React.ReactNode;
}

export interface TableRecord {
  id: string;
  humanReadableId: string;
  passengerFullName: string;
  desiredDate: string;
  // requestCompletionDate: string; // есть в БТ, пока не приходит в ответе с бэка
  tariff: string;
  requestStatus: string;
  // approvalDate: string; // есть в БТ, пока не приходит в ответе с бэка
  approverFullName: string;
  plannedRoute: string;
  // actualRoute: string; // есть в БТ, пока не приходит в ответе с бэка
  plannedCost: number;
  actualCost: number;
  // actualDistance: number; // есть в БТ, пока не приходит в ответе с бэка
  receiptLink: string;
  comment: string;
  reason: string;
  costCenter: string;
}

export interface PageOptions {
  pageSetting: PageSetting;
  onPaginationChange: (page: number, size?: number | undefined) => void;
}

export interface SelectedRowData {
  requestId: string;
  payed: boolean;
}

export interface TaxiYandexRegistryFilter {
  withFilters?: boolean;
  humanReadableId?: string;
  status?: string[];
  desiredDateRange?: Value;
  passengerFullName?: string;
  approverFullName?: string;
  sortSetting?: SortSetting;
  pageSetting?: PageSetting;
  balanceUnits?: string[];
  costCenter?: string;
  orderPaymentFormationStartRange?: Value;
  department1?: string[];
  department2?: string[];
  department3?: string[];
  department4?: string[];
  department5?: string[];
  department6?: string[];
}

export type FilterValues = Omit<YandexTaxiReportSearchQuery, 'pageSetting' | 'sortSetting'>;

export interface reportParams {
  filters: TaxiYandexRegistryFilter;
  withFilters: boolean;
}

export interface RequestBodyPublic {
  withFilters?: boolean;
  withView?: boolean;
  filters?: string;
  organizationId?: string;
  executorGroupIds?: string[];
  publicUIVisibilityDTO?: PublicUIVisibilityDTO;
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

export interface RequestBodyParamsYandexTaxi {
  withFilters: boolean;
  filters: string;
}
