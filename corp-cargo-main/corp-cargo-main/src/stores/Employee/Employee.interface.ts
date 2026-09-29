/* eslint-disable no-use-before-define */
import { EmployeeStatus, OrgStructureType } from 'constants/constants.app';
import * as t from 'io-ts';
import { IEmployeeDetailed } from 'stores/Corporate/Corporate.interface';
import { EmployeesAttribute } from 'stores/EmployeesAttribute/EmployeesAttribute.interface';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export interface IEmployeeStore {
  savedUser: Omit<User, 'password'> | undefined;
  clearSavedUser(): void;
  initStore(): Promise<void>;
}

export interface IEmployeeService {
  getAllEmployeesByOrganization(orgId: string): Promise<Employee[]>;
  getAllEmployeesByDepartment(orgId: string, depId: string): Promise<Employee[]>;
  getEmployeesByIds(orgId: string, empIds: string[]): Promise<Employee[]>;
  addEmployee(orgId: string, depId: string, employee: PartialBy<IEmployeeDetailed, 'id'>): Promise<Employee>;
  editEmployee(orgId: string, depId: string, empId: string, employee: IEmployeeDetailed): Promise<number>;
  deleteEmployee(orgId: string, depId: string, empId: string): Promise<number>;

  addUser(user: Omit<User, 'id'>): Promise<Omit<User, 'password'> | number>;
}

export const Employee = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    organizationId: tt.uuid,
    departmentId: tt.uuid,
    positionId: tt.uuid,
    attributes: t.array(EmployeesAttribute),
    status: t.keyof(EmployeeStatus),
  }),
  t.partial({
    organizationName: t.string,
    departmentName: t.string,
    availableTransportTypes: t.UnknownArray,
    userId: t.string,
    personnelNumber: t.string,
    roles: t.array(t.string),
    patronymic: t.string,
    mobilePhone: t.string,
    email: t.string,
    supervisorId: t.string,
    delegatedById: t.string,
    personalCars: t.UnknownArray,
    positionName: t.string,
    gender: t.string,
    orgStructureType: ioTypeFromEnum<OrgStructureType>('OrgStructureType', OrgStructureType),
  }),
]);
export type Employee = t.TypeOf<typeof Employee>;

export const SelfEmployee = t.intersection([
  Employee,
  t.type({
    consent: t.boolean,
  }),
]);
export type SelfEmployee = t.TypeOf<typeof SelfEmployee>;

const Sort = t.partial({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  offset: t.number,
  sort: Sort,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});
const PageableUnion = t.union([Pageable, t.string]);

export const EmployeeResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: PageableUnion,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(Employee),
});
export type EmployeeResponse = t.TypeOf<typeof EmployeeResponse>;

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});
export type PaginationParams = t.TypeOf<typeof PaginationParams>;

export const User = t.intersection([
  t.type({
    id: t.string,
    lastName: t.string,
    firstName: t.string,
    login: t.string,
    password: t.string,
    departmentId: tt.uuid,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);

export type User = t.TypeOf<typeof User>;

export interface IEmployeeDetails {
  delegatedBy: string;
  department: string;
  organization: string;
  position: string;
  supervisor: string;
}

export interface IPersonalCar {
  employeeId?: string;
  ownerInfo?: string;
  id: string;

  brandName: string;
  model: string;
  registrationNumber: string;
  registrationCertificate: string;
  engineVolume: number;
  insuranceNumber: string;
}
