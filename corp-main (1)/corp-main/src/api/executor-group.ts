/* eslint-disable @typescript-eslint/no-explicit-any */
import { useAPI, APIQueryResult } from 'api';
import * as t from 'io-ts';

export const ExecutorGroupsQuery = t.partial({
  page: t.number,
  size: t.number,
});
export type ExecutorGroupsQuery = t.TypeOf<typeof ExecutorGroupsQuery>;
export const Customer = t.type({
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  id: t.string,
});

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});
export type PaginationParams = t.TypeOf<typeof PaginationParams>;

export const CustomerResponse = t.array(Customer);
export type CustomerResponse = t.TypeOf<typeof CustomerResponse>;

const Customers = t.type({
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  id: t.string,
});

const Executors = t.type({
  employeeId: t.string,
  employeeName: t.string,
  employeePersonnelNumber: t.string,
  employeeStatus: t.string,
  departmentId: t.string,
  departmentName: t.string,
  departmentHumanReadableId: t.string,
  departmentStatus: t.string,
});

const GeoZones = t.type({
  id: t.string,
  name: t.string,
});

const Organizations = t.type({
  id: t.string,
  officialName: t.string,
  status: t.string,
});

const Departments = t.type({
  id: t.string,
  officialName: t.string,
  status: t.string,
});

export const ExecutorGroups = t.type(
  {
    id: t.string,
    service: t.string,
    humanReadableId: t.string,
    active: t.boolean,
    name: t.string,
    creationTime: t.string,
    updatedAt: t.string,
    authorId: t.string,
    userId: t.string,
    executors: t.array(Executors),
    organizations: t.array(Organizations),
    departments: t.array(Departments),
    customers: t.array(Customers),
    geoZones: t.array(GeoZones),
  });

export type ExecutorGroups = t.TypeOf<typeof ExecutorGroups>;

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

export const ExecutorGroupsResponse = t.type({
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
  content: t.array(ExecutorGroups),
});

export type ExecutorGroupsResponse = t.TypeOf<typeof ExecutorGroupsResponse>;

declare module 'api' {
  interface Cache {
    ExecutorGroupsRequest: { key: ['ExecutorGroupsRequest', pagination?: PaginationParams, cargoType?: string] };
  }
}

export const useGetExecutorGroups = (
  pagination: PaginationParams,
  isCargo?: boolean
): APIQueryResult<ExecutorGroupsResponse, Error> => {
  const link = isCargo ? 'organizations/executorGroup?serviceType=CARGO_TRANSPORTATION' : `organizations/executorGroup`;

  const cargoType = isCargo ? 'cargo' : 'passenger';

  return useAPI(
    ['ExecutorGroupsRequest', pagination, cargoType],
    ({ http, process }) => http.get<ExecutorGroups[]>(link, {
      params: {
        ...pagination,
      },
      headers: { 'X-Paged': true },
    })
      .then(process.decodeResponseData())
  );
};
