import { EmployeeStatus, EmployeeStatusTitle } from 'constants/constants.app';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { DepartmentEmployee } from 'stores/Corporate/Corporate.interface';

const Sort = t.partial({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  sort: Sort,
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

const PageableUnion = t.union([Pageable, t.string]);

const DepartmentHead = t.partial({
  id: t.string,
  lastName: t.string,
  firstName: t.string,
  patronymic: t.string,
});

const Parent = t.partial({
  id: t.string,
  departmentName: t.string,
});

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
    fullStructurePath: t.string,
    humanReadableId: t.string,
    departmentHead: DepartmentHead,
    parent: Parent,
    location: t.string,
    geozoneId: tt.uuid,
    easupId: t.string,
    level: t.number,
  }),
]);

export type Department = t.TypeOf<typeof Department>;

export const DepartmentSelectProjection = t.intersection([
  t.type({
    code: t.string,
    departmentName: t.string,
    id: tt.uuid,
    organizationId: tt.uuid,
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
  }),
  t.partial({
    departmentHead: DepartmentHead,
    endRecordingDate: t.string,
    startRecordingDate: t.string,
    handmade: t.boolean,
    humanReadableId: t.string,
    level: t.number,
    location: t.string,
    fullStructurePath: t.string,
    patronymic: t.string,
    lastName: t.string,
    personnelNumber: t.string,
    firstName: t.string,
  }),
]);

export type DepartmentSelectProjection = t.TypeOf<typeof DepartmentSelectProjection>;

export interface DepartmentChildrenType { id: string; departmentName: string; status: EmployeeStatus }

export interface DepartmentDetailed {
  humanReadableId?: string;
  statusTitle?: EmployeeStatusTitle;
  departmentHead?: string;
  departmentHeadId?: string;
  parent?: string | Partial<DepartmentChildrenType>;
  location?: string;
  easupId?: string;
  fullStructurePath?: string;
  id: tt.UUID;
  organization: string;
  organizationId: tt.UUID;
  code: string;
  departmentName: string;
  children: DepartmentChildrenType[];
  employees: DepartmentEmployee[];
  status: EmployeeStatus;
}

export const DepartmentsResponse = t.type({
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
  content: t.array(Department),
});

export type DepartmentsResponse = t.TypeOf<typeof DepartmentsResponse>;

export const DepartmentsResponseSearch = t.array(Department);

export type DepartmentsResponseSearch = t.TypeOf<typeof DepartmentsResponseSearch>;

export const DepartmentsAllResponse = t.array(DepartmentSelectProjection);

export type DepartmentsAllResponse = t.TypeOf<typeof DepartmentsAllResponse>;

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});
export type PaginationParams = t.TypeOf<typeof PaginationParams>;
