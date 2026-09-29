import { stringify } from 'qs';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import {
  GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS,
  EMPLOYEES_SEARCH,
  DEPARTMENT_SEARCH
} from 'constants/constants.api';
import { DepartmentsResponseSearch } from 'stores/Department/Department.interface';
import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';
import { EmployeeResponse, PaginationParams } from 'stores/Employee/Employee.interface';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';
import * as tt from 'utils/io-ts';

/* НА ДАННЫЙ МОМЕНТ ИСПОЛЬЗУЕТСЯ ТОЛЬКО В ГРУППАХ ИСПОЛНИТЕЛЕЙ */
export const EmployeeSearchQuery = t.partial({
  humanReadableId: t.string,
  personnelNumber: t.string,
  status: t.string,
  email: t.string,
  page: t.number,
  size: t.number,
  mobilePhone: tt.mobilePhone,
  fullName: t.string,
  employees: t.array(t.string),
  projection: t.string,
});

export type EmployeeSearchQuery = t.TypeOf<typeof EmployeeSearchQuery>;

export const DepartmentSearchQuery = t.partial({
  humanReadableId: t.string,
  personnelNumber: t.string,
  status: t.string,
  email: t.string,
  page: t.number,
  size: t.number,
  mobilePhone: tt.mobilePhone,
  departmentName: t.string,
  employees: t.array(t.string),
  projection: t.string,
  id: t.union([t.string, t.array(t.string)]),
  departments: t.array(t.string),
});

export type DepartmentSearchQuery = t.TypeOf<typeof DepartmentSearchQuery>;

declare module 'api' {
  interface Cache {
    searchEmployeeByOrganization: {
      key: ['searchEmployeeByOrganization', EmployeeSearchQuery, PaginationParams, UUID];
      value: EmployeeResponse;
    };
    searchEmployeeByDepartments: {
      key: ['searchEmployeeByDepartments', EmployeeSearchQuery, PaginationParams];
      value: EmployeeResponse;
    };
  }
}

export const useSearchEmployee = (): MutationResultPair<
  EmployeeResponse,
  Error,
  EmployeeSearchQuery,
  unknown
> => (
  useAPIMutation(({ http, process }, params) => http
    .get<EmployeeResponse>(EMPLOYEES_SEARCH, {
      params,
      paramsSerializer: params => stringify(params, { arrayFormat: 'repeat' }),
    })
    .then(process.decodeResponseData(EmployeeResponse)),
  { onSuccess: ignore }
  )
);

export const useSearchDepartment = (): MutationResultPair<
  DepartmentsResponseSearch,
  Error,
  DepartmentSearchQuery,
  unknown
> => (
  useAPIMutation(({ http, process }, params) => http
    .get<DepartmentsResponseSearch>(DEPARTMENT_SEARCH, {
      params,
      paramsSerializer: params => stringify(params, { arrayFormat: 'repeat' }),
    })
    .then(process.decodeResponseData(DepartmentsResponseSearch)),
  { onSuccess: ignore }
  )
);

export const useSearchEmployeeByOrganization = (
  query: EmployeeSearchQuery,
  pagination: PaginationParams,
  orgId: UUID,
  options?: QueryConfig<EmployeeResponse, Error>
): APIQueryResult<EmployeeResponse, Error> => useAPI(
  ['searchEmployeeByOrganization', query, pagination, orgId],
  ({ http, process }) => http
    .get<EmployeeResponse>(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS, {
      urlParams: { orgId: orgId || '' },
      params: EmployeeSearchQuery.encode({
        ...query, ...pagination, personnelNumber: query?.personnelNumber,
      }),
    })
    .then(process.decodeResponseData(EmployeeResponse)),
  {
    cacheTime: 200, staleTime: 0, refetchOnMount: false, ...options,
  }
);
