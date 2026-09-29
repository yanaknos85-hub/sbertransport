/* eslint-disable @typescript-eslint/no-explicit-any */
import { useAPI, useAPIMutation } from 'api';
import { MutationResultPair } from 'react-query';
import * as t from 'io-ts';
import { UUID } from 'utils/io-ts';
import { PaginationParams } from 'stores/Contractors/Contractors.interface';
import { AxiosError } from 'axios';
import { ignore, getErrorMessage } from 'utils';
import { EmployeeResponse } from 'stores/Employee/Employee.interface';

import {
  SEARCH_EMPLOYEE
} from '../constants/constants.api';

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
export const CustomerResponse = t.array(Customer);
export type CustomerResponse = t.TypeOf<typeof CustomerResponse>;

interface ExecutorGroup {
  active: boolean;
  authorId: UUID;
  customers: any[];
  departments: any[];
  executors: any[];
  geoZones: any[];
  organizationId: UUID;
  organizations: any[];
  name: string;
  service: string;
  serviceLevel: string;
}

interface IExecutorGroupRequest {
  active: boolean;
  authorId: UUID;
  customers: any[];
  departments: any[];
  executors: any[];
  geoZones: any[];
  organizationId: UUID;
  organizations: any[];
  name: string;
  service: string;
  serviceLevel: string;
  fullData: ExecutorGroup;
}

declare module 'api' {
  interface Cache {
    ExecutorGroupsRequest: { key: ['ExecutorGroupsRequest', pagination?: PaginationParams] };
    ExecutorGroupRequest: { key: ['ExecutorGroupRequest', groupId: UUID | undefined] };
    GetEmployees: { key: ['GetEmployees', orgId: UUID, page: number, size: number] };
    PlannerAll: { key: ['PlannerAll'] };
  }
}

const ERROR_CODES = {
  23001: 'Исполнитель не принадлежит организации',
  23003: 'Департамент исполнителя не принадлежит организации',
  23006: 'Департамент заказчика не принадлежит организации',
  23009: 'Заказчик не принадлежит организации',
  23011: 'Группа исполнителей с таким именем уже существует',
  23014: 'Группа исполнителей с таким уровнем сервиса уже существует',
  23017: 'Группа исполнителей с этими заказчиками уже существует',
};

export const useGetExecutorGroups = (pagination?: any) => useAPI(
  ['ExecutorGroupsRequest', pagination],
  // @ts-ignore
  ({ http }) => http.get(`organizations/executorGroup`, {
    params: {
      ...pagination,
    },
    headers: { 'X-Paged': true },
  })
);

export const useGetExecutorGroupsDef
= (pagination?: any): MutationResultPair<any, AxiosError, any, unknown> => useAPIMutation(
  async ({ http }) => {
    let customerDepartments, customerOrganizations, executorPersonnelNumber, customerGeoZones;
    if (pagination?.customerOrganizations) {
      customerOrganizations = pagination?.customerOrganizations.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    if (pagination?.customerDepartments) {
      customerDepartments = pagination?.customerDepartments.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    if (pagination?.executorPersonnelNumber) {
      executorPersonnelNumber = pagination?.executorPersonnelNumber.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    if (pagination?.customerGeoZones) {
      customerGeoZones = pagination?.customerGeoZones.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    return await http.get(`organizations/executorGroup`, {
      params: {
        ...pagination,
        customerDepartments,
        customerOrganizations,
        executorPersonnelNumber,
        customerGeoZones,
      },
      headers: { 'X-Paged': true },
    });
  },
  {
    onSuccess: ignore,
  }
);

export const useGetExecutorGroup = (groupId: UUID | undefined) => useAPI(
  ['ExecutorGroupRequest', groupId],
  // @ts-ignore
  ({ http }) => groupId ? http.get(`organizations/executorGroup/${groupId}/`) : undefined
);

export const useGetPlannerAll = () => useAPI(
  ['PlannerAll'],
  // @ts-ignore
  ({ http }) => http.get(`request/aggregation/manager/planner/all`)
);

export const useCreateExecutorGroup = (): MutationResultPair<IExecutorGroupRequest, AxiosError, Omit<IExecutorGroupRequest, 'id'>, unknown> => {
  return useAPIMutation(
    ({ http, process }, executor: Omit<IExecutorGroupRequest, 'id'>) => http
      .post(`organizations/executorGroup`, {
        service: 'EMPLOYEE_TRANSPORTATION',
        name: executor.name,
        serviceLevel: executor?.serviceLevel ?? '',
        organizationId: executor.organizationId,
        executors: executor.executors,
        organizations: executor.organizations,
        departments: executor?.departments ?? [],
        customers: executor.customers ? [executor.customers] : [],
        geoZones: executor?.geoZones ?? [],
      })
    // @ts-ignore
      .then<IExecutorGroupRequest>(process.getResponseData),
    {
      onSuccess: ({
        cache, process,
      }) => {
        process.processStatus(200, 'Рабочая группа добавлена');

        cache.refetchQueries(['ExecutorGroupsRequest']);
      },
      onError: ({ error, logger }) => {
        const errorString = getErrorMessage(error);
        const errors = errorString[0] !== '[' ? errorString.split(', ') : errorString.slice(1, -1).split(', ');
        const errorText = errors.map(item => ERROR_CODES[item]).join(', ');

        logger.toMessage('error', errorText);
      },
    }
  );
};

export const useUpdateExecutorGroup
= (): MutationResultPair<void, AxiosError, IExecutorGroupRequest, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http }, executor: IExecutorGroupRequest) => http.put(`organizations/executorGroup/${executor.fullData?.id}/`, {
    service: 'EMPLOYEE_TRANSPORTATION',
    name: executor.name,
    // @ts-ignore
    serviceLevel: executor?.fullData?.serviceLevel ?? '',
    // @ts-ignore
    organizationId: executor.fullData?.organizationId,
    // @ts-ignore
    humanReadableId: executor.fullData?.humanReadableId,
    // @ts-ignore
    active: executor.fullData?.active,
    // @ts-ignore
    executors: executor.fullData?.executors.map(item => item.employeeId),
    // @ts-ignore
    organizations: executor.fullData?.organizations.map(item => item.id),
    // @ts-ignore
    departments: executor?.fullData?.departments.map(item => item.id) ?? [],
    // @ts-ignore
    customers: executor.fullData?.customers.map(item => item.id) ?? [],
    // @ts-ignore
    geoZones: executor?.fullData?.geoZones.map(item => item.id) ?? [],
  }, {}).then(ignore),
  {
    onSuccess: ({
      cache, process,
    }) => {
      process.processStatus(200, 'Рабочая группа обновлена');

      cache.refetchQueries(['ExecutorGroupsRequest']);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error));
    },
  }
);

export const useGetAllEmployees = (
  orgId: UUID
): MutationResultPair<EmployeeResponse, Error, { departmentName?: string }, unknown> => (
  useAPIMutation(
    async ({ http, process }, {
      departmentName,
    }) => {
      let total: number | null = null;
      total = await http
        .get<EmployeeResponse>(SEARCH_EMPLOYEE, {
          urlParams: { orgId },
          params: {
            fullName: departmentName,
            page: 0,
            size: 1,
          },
        })
        .then(res => res.data.totalElements);

      return http
        .get<EmployeeResponse>(SEARCH_EMPLOYEE, {
          urlParams: {
            orgId,
          },
          params: {
            fullName: departmentName,
            page: 0,
            size: total ? total : 1,
          },
        })
        .then(process.decodeResponseData(EmployeeResponse));
    },
    {
      onSuccess: ignore,
    }
  )
);

export const useGetEmployeesByOrgAndDep = ():
MutationResultPair<CustomerResponse, Error, { organizations: UUID[]; departments?: UUID[] }, unknown> => (
  useAPIMutation(
    ({ http, process }, {
      organizations, departments,
    }) => {
      return http
        .get<CustomerResponse>('organizations/employees/search_eg', {
          params: {
            organizations: organizations?.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), ''),
            departments: departments?.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), ''),
          },
        })
        .then(process.decodeResponseData(CustomerResponse));
    },
    {
      onSuccess: ignore,
    }
  )
);
