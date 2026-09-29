import { EmployeeStatus } from 'constants/constants.app';

import { DepartmentDetailedModel } from 'stores/Corporate/models/DepartmentDetailed.model';

export interface ICorporateFiltersStore {
  filters: Map<TDepartmentFiltersTitles, string>;
  departmentsFiltered: DepartmentDetailedModel[];
  editFilteres(filters: Map<TDepartmentFiltersTitles, string>): void;
  refreshFilteres(): void;
}

export interface TDepartmentFilters {
  id?: string;
  organization?: string;
  departmentName?: string;
  departmentHead?: string;
  statusTitle?: EmployeeStatus;
  code?: string;
  parent?: string;
  location?: string;
}

export type TDepartmentFiltersTitles = keyof TDepartmentFilters;
