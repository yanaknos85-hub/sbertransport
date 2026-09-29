/* eslint-disable @typescript-eslint/no-explicit-any */

import { EmployeeModel } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { Dictionary } from 'lodash';

import { EmployeeStatus, EmployeeStatusTitle } from 'modules/EmployeeApp/EmployeeApp.constants';

import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { DepartmentModel } from './models/Department.model';
import { DepartmentDetailedModel } from './models/DepartmentDetailed.model';
import { OrganizationNormalizedModel } from './models/OrganizationNormalized.model';

export interface ICorporateStore {
  selfEmployee: EmployeeModel | undefined;
  organizations: IOrganization[];
  departments: DepartmentModel[];
  positions: IPosition[];
  organizationsMapped: Dictionary<OrganizationNormalizedModel>;
  departmentsMapped: Dictionary<TDepartment>;
  positionsMapped: Dictionary<IPosition>;
  savedDepartment?: DepartmentDetailedModel;
  initStore(): void;
  clearSavedDepartment(): void;
  editSavedDepartment(model: DepartmentDetailedModel): void;
  refreshDepartments(): void;

  getDepartment(depId: string): TDepartment;
  editDepartment(model: DepartmentDetailedModel): void;
  deleteDepartment(depId: string): void;
}

export interface ICorporateService {
  getAllOrganizations(): Promise<IOrganization[]>;
  getAllDepartments(orgId: string): Promise<{ content: TDepartment[] }>;
  getAllPositions(orgId: string): Promise<IPosition[]>;
  getOrganization(orgId: string): Promise<IOrganization>;
  getPosition(orgId: string, depId: string, posId: string): Promise<IPosition>;

  getDepartment(orgId: string, depId: string): Promise<TDepartment>;
  addDepartment(orgId: string, model: DepartmentDetailedModel): Promise<TDepartment>;
  editDepartment(orgId: string, depId: string, model: DepartmentDetailedModel): Promise<number>;
  deleteDepartment(orgId: string, depId: string): Promise<number>;
}

export interface IEmployeeDetailed {
  id: string;
  humanReadableId: string;
  userId: string;
  fullNameString: string;
  nameWithInitials: string;
  personnelNumber: string;
  department: string;
  position: string;
  mobilePhone: string;
  email: string;
  supervisor: string;
  delegatedBy: string;
  availableTransportTypes: any[];
  organization: string;
}

export interface IOrganizationPosition {
  id: string;
  positionName: string;
  organizationId: string;
}

export interface IOrganization {
  id: string;
  officialName: string;
  address: string;
  positions: IShortPosition[];
  departments: IShortDepartment[];
}

export interface IShortDepartment {
  id: string;
  departmentName: string;
}

export const IODepartmentEmployee = t.type({
  id: t.string,
  firstName: t.string,
  lastName: t.string,
  personnelNumber: t.string,
});

export type TDepartmentEmployee = t.TypeOf<typeof IODepartmentEmployee>;

export type IDepartments = Record<string, IDepartment[]>;

export interface IDepartmentHead {
  id: string;
  firstName: string;
  lastName: string;
  patronymic?: string;
}

export interface IDepartment {
  id?: string;
  humanReadableId?: string;
  departmentHeadId?: string;
  parentId?: string;
  location?: string;
  code: string;
  organizationId: string;
  departmentName: string;
  fullStructurePath: string;
  children: IDepartment[];
  employees: TDepartmentEmployee[];
  status: EmployeeStatus;
  departmentHead?: IDepartmentHead;
}

export const IODepartment: t.Type<IDepartment> = t.recursion('IODepartment', () => t.intersection([
  t.type({
    organizationId: t.string,
    code: t.string,
    departmentName: t.string,
    fullStructurePath: t.string,
    employees: t.array(IODepartmentEmployee),
    children: t.array(IODepartment),
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
  }),
  t.partial({
    id: t.string,
    humanReadableId: t.string,
    departmentHeadId: t.string,
    parentId: t.string,
    location: t.string,
  }),
])
);

export type TDepartment = t.TypeOf<typeof IODepartment>;

export interface TDepartmentDetailed {
  id?: string;
  humanReadableId?: string;
  statusTitle?: EmployeeStatusTitle;
  departmentHead?: string;
  departmentHeadId?: string;
  parent?: string;
  location?: string;
  organization: string;
  organizationId: string;
  code: string;
  departmentName: string;
  fullStructurePath: string;
  children: TDepartment[];
  employees: TDepartmentEmployee[];
  status: EmployeeStatus;
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

export interface IPosition {
  id: string;
  organizationId: string;
  positionName: string;
  selfApproved: boolean;
  availableClasses: TaxiClassEnum[];
}

export interface IShortPosition {
  id: string;
  organizationId: string;
  positionName: string;
}
