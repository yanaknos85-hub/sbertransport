
import { IOPersonalCar } from '@sber-sbertransport/mf-core';
import { EmployeeStatus, EmployeeStatusTitle } from 'constants/constants.app';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export interface IEmployeeDetailed {
  userId?: string;
  id: tt.UUID;
  humanReadableId: string;
  fullNameString: string;
  nameWithInitials: string;
  personnelNumber: string;
  department: string;
  position: string;
  mobilePhone: string;
  email: string;
  supervisor: string;
  delegatedBy: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  availableTransportTypes: any[];
  personalCars: typeof IOPersonalCar[];
  organization: string;
  status: EmployeeStatus;
  statusTitle: EmployeeStatusTitle;
  code: string;
}

export interface Organization {
  id: string;
  officialName: string;
  address: string;
}

export interface ShortDepartment {
  id: string;
  departmentName: string;
}

export const DepartmentEmployee = t.type({
  id: t.string,
  firstName: t.string,
  lastName: t.string,
  personnelNumber: t.string,
});

export type DepartmentEmployee = t.TypeOf<typeof DepartmentEmployee>;

export const CreateDepartmentResponse = t.intersection([
  t.type({
    id: tt.uuid,
    organizationId: tt.uuid,
    code: t.string,
    departmentName: t.string,
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
    humanReadableId: t.string,
  }),
  t.partial({
    departmentHeadId: t.string,
    geozoneId: tt.uuid,
    fullStructurePath: t.string,
    parentId: t.string,
  }),
]);

export type CreateDepartmentResponse = t.TypeOf<typeof CreateDepartmentResponse>;

export const Department = t.intersection([
  t.type({
    id: tt.uuid,
    organizationId: tt.uuid,
    code: t.string,
    departmentName: t.string,
    employees: t.array(DepartmentEmployee),
    children: t.array(
      t.strict({
        id: t.string,
        departmentName: t.string,
        status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
      })
    ),
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
  }),
  t.partial({
    humanReadableId: t.string,
    departmentHeadId: t.string,
    parent: t.partial({ id: t.string, departmentName: t.string }),
    location: t.string,
    geozoneId: tt.uuid,
    fullStructurePath: t.string,
  }),
]);

export type Department = t.TypeOf<typeof Department>;

export interface DepartmentDetailed {
  id: tt.UUID;
  humanReadableId?: string;
  statusTitle?: EmployeeStatusTitle;
  departmentHead?: string;
  departmentHeadId?: string;
  parent?: string;
  location?: string;
  organization: string;
  organizationId: tt.UUID;
  code: string;
  departmentName: string;
  children: { id: string; departmentName: string; status: EmployeeStatus }[];
  employees: DepartmentEmployee[];
  status: EmployeeStatus;
}

export interface ShortPosition {
  id: string;
  organizationId: string;
  positionName: string;
}

export const DepartmentFirstLevelChildInfo = t.type({
  id: tt.uuid,
  departmentName: t.string,
  status: t.string,
});

export type DepartmentFirstLevelChildInfo = t.TypeOf<typeof DepartmentFirstLevelChildInfo>;
