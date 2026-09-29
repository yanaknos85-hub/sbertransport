import type { UUID } from 'utils/io-ts';
import type { Category, Tab } from '../constants/Tabs';
import type { IFilter } from './Filters.interface';

export interface IStatus<T> {
  name: T;
  rusName: string;
  editable: boolean;
  approvable: boolean;
  cancelable: boolean;
  finalStatus: boolean;
  backgroundColor?: string;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type TCarServiceStore = any;

export interface IOrganization {
  id: UUID;
  officialName: string;
}

export interface IDepartment {
  id: UUID;
  departmentName: string;
  parentId: UUID | null;
}

export interface IDepartmentListResponse {
  officialName: string;
  departmentDtoList: IDepartment[];
}

export interface ISearchEmployeeResponse {
  id: UUID;
  firstName: string;
  lastName: string;
  patronymic: string;
  personnelNumber: string;
}

export interface IOrdersService {
  getStateNumberList: (stateNumber: string) => Promise<string[] | void>;
  getEmployeeList: (name: string) => Promise<ISearchEmployeeResponse[] | void>;
  getEmployeeOrganization: () => Promise<IOrganization | void>;
  getOrganizationList: () => Promise<IOrganization[] | void>;
  getDepartmentList: (organizationIdList: UUID) => Promise<IDepartmentListResponse[] | void>;
}

export interface IOrdersStore {
  organizationId: UUID;
  activeTabKey: Tab;
  activeCategory: Category;
  filter: IFilter;
  filterCounter: number;
  filterOrganizationId: UUID | null;
  resetFilterFieldsFunction: () => void;
  isCarService: boolean;
  setOrganizationId(organizationId: UUID): void;
  setActiveTabKey(activeKey: Tab): void;
  setActiveCategory(category: Category): void;
  setFilter(filter: IFilter): void;
  resetFilter(): void;
  setResetFilterFieldsFunction(newFunction: () => void): void;
  setFilterOrganizationId(organizationId: UUID | null): void;
  resetFilterWithApply(): void;
  getStateNumberList(stateNumber: string): Promise<string[] | void>;
  searchEmployee(name: string): Promise<ISearchEmployeeResponse[] | void>;
  getEmployeeOrganization(): Promise<IOrganization | void>;
  getOrganizationList(): Promise<IOrganization[] | void>;
  getDepartmentList(): Promise<IDepartmentListResponse[] | void>;
}
