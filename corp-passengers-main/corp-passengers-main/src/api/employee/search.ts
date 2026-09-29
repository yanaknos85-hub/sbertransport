import { stringify } from 'qs';
import { useAPIMutation } from 'api';
import { APIQueryResult, useAPI } from 'api';
import {
  GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS,
  GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS,
  GET_ALL_EMPLOYEES
} from 'constants/constants.api';
import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';
import { EmployeeResponse, PaginationParams } from 'stores/Employee/Employee.interface';
import { UUID } from 'utils/io-ts';
import * as tt from 'utils/io-ts';
import { ignore } from 'utils';

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

declare module 'api' {
  interface Cache {
    searchEmployee: { key: ['searchEmployee', EmployeeSearchQuery, PaginationParams, UUID]; value: EmployeeResponse };
    searchEmployeeByDepartments: {
      key: ['searchEmployeeByDepartments', EmployeeSearchQuery, PaginationParams];
      value: EmployeeResponse;
    };
  }
}

export const useSearchEmployee = (
  query: EmployeeSearchQuery,
  pagination: PaginationParams,
  orgId: UUID,
  options?: QueryConfig<EmployeeResponse, Error>
): APIQueryResult<EmployeeResponse, Error> => useAPI(
  ['searchEmployee', query, pagination, orgId],
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

export const useSearchEmployeeByDepartments = (
  query: EmployeeSearchQuery,
  pagination: PaginationParams,
  orgId?: UUID,
  depId?: UUID,
  options?: QueryConfig<EmployeeResponse, Error>
): APIQueryResult<EmployeeResponse, Error> => useAPI(
  ['searchEmployeeByDepartments', query, pagination],
  ({ http, process }) => http
    .get<EmployeeResponse>(GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS, {
      urlParams: { orgId: orgId || '', depId: depId || '' },
      params: EmployeeSearchQuery.encode({
        ...query, ...pagination, personnelNumber: query?.personnelNumber,
      }),
    })
    .then(process.decodeResponseData(EmployeeResponse)),
  {
    cacheTime: 0, staleTime: 0, refetchOnMount: false, ...options,
  }
);

export const useSearchEmployeeMutation = (): MutationResultPair<
  EmployeeResponse,
  Error,
  EmployeeSearchQuery,
  unknown
> => (
  useAPIMutation(({ http, process }, params) => http
    .get<EmployeeResponse>(GET_ALL_EMPLOYEES, {
      params,
      paramsSerializer: params => stringify(params, { arrayFormat: 'repeat' }),
    })
    .then(process.decodeResponseData(EmployeeResponse)),
  { onSuccess: ignore }
  )
);
