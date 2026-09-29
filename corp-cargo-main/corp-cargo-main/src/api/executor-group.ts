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
  SEARCH_EMPLOYEE_EXECUTOR_GROUP
} from '../constants/constants.api';

/* TODO требуется рефакторинг всего раздала ExecutorGroup */
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
  organizations: string[];
  name: string;
  service: string;
  serviceLevel: string;
  contractors: string[];
  additionalFeature: string;
}

interface IExecutorGroupRequest {
  active: boolean;
  authorId: UUID;
  customers: any[];
  departments: any[];
  executors: any[];
  geoZones: any[];
  organizationId: UUID;
  organizations: string[];
  name: string;
  service: string;
  serviceLevel: string;
  fullData: ExecutorGroup;
  contractors: string[];
  additionalFeature: string;
}

declare module 'api' {
  interface Cache {
    ExecutorGroupsRequest: { key: ['ExecutorGroupsRequest', PaginationParams] };
    ExecutorGroupRequest: { key: ['ExecutorGroupRequest', UUID | undefined] };
    GetEmployees: { key: ['GetEmployees', UUID, number, number] };
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

// todo Типизировать pagination
export const useGetExecutorGroups = (pagination?: any) => useAPI(
  ['ExecutorGroupsRequest', pagination],
  // @ts-ignore
  async ({ http }) => {
    // todo Разобраться, за что отвечает это условие.
    if (pagination?.executorDepartments) {
      pagination.executorDepartments = pagination?.executorDepartments.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    if (pagination?.executorFIO) {
      pagination.executorFIO = pagination?.executorFIO.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    if (pagination?.executorGeoZones) {
      pagination.executorGeoZones = pagination?.executorGeoZones.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '');
    }
    return await http.get<any>(`organizations/executorGroup?serviceType=CARGO_TRANSPORTATION`, {
      params: {
        ...pagination,
      },
      headers: { 'X-Paged': true },
    });
  }, {
    onSuccess: ignore,
  }
);

export const useGetExecutorGroup = (groupId: UUID | undefined) => useAPI(
  ['ExecutorGroupRequest', groupId],
  // @ts-ignore
  ({ http }) => groupId ? http.get(`organizations/executorGroup/${groupId}/`) : undefined
);

export const useCreateExecutorGroup = (): MutationResultPair<IExecutorGroupRequest, AxiosError, Omit<IExecutorGroupRequest, 'id'>, unknown> => {
  return useAPIMutation(
    ({ http, process }, executor: Omit<IExecutorGroupRequest, 'id'>) => http
      .post(`organizations/executorGroup`, {
        // todo Разобраться, что делать с этим полем, т.к. сервис делим с пассажирами
        ...executor,
        service: 'CARGO_TRANSPORTATION',
      },
      { hush: [422] }
      )
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
        let errorText = 'Ошибка создания группы исполнителей, обратитесь к администратору';
        try {
          const errorString = getErrorMessage(error);
          if (errorString) {
            // Проверяем, содержит ли ошибка коды или это текстовое сообщение
            const isErrorCode = /^\d+$/.test(errorString.replace(/[,\s[\]']/g, ''));

            if (isErrorCode) {
              const errors = errorString[0] !== '['
                ? errorString.split(', ')
                : errorString.slice(1, -1).split(', ');
              errorText = errors.map(item => ERROR_CODES[item] || item).join(', ');
            }
          }
        } catch (e) {
          // @no-empty
        }

        logger.toMessage('error', errorText);
      },
    }
  );
};

export const useUpdateExecutorGroup
  = (): MutationResultPair<void, AxiosError, IExecutorGroupRequest, unknown> => useAPIMutation(
    // @ts-ignore
    ({ http }, executor: IExecutorGroupRequest) => http.put(`organizations/executorGroup/${executor.fullData?.id}/`, {
      service: 'CARGO_TRANSPORTATION',
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
      executors: executor?.executors,
      // @ts-ignore
      organizations: executor.fullData?.organizations.map(item => item.id),
      // @ts-ignore
      departments: executor?.departments,
      // @ts-ignore
      customers: executor.fullData?.customers.map(item => item.id) ?? [],
      // @ts-ignore
      contractors: executor?.contractors,
      // @ts-ignore
      geoZones: executor?.fullData?.geoZones.map(item => item.id) ?? [],
      // @ts-ignore
      additionalFeature: executor.additionalFeature,
    }, {}).then(ignore),
    {
      onSuccess: ({
        cache, process,
      }) => {
        process.processStatus(200, 'Рабочая группа обновлена');

        cache.refetchQueries(['ExecutorGroupRequest']);
        cache.invalidateQueries(['ExecutorGroupsRequest']);
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
        .get<EmployeeResponse>(SEARCH_EMPLOYEE_EXECUTOR_GROUP, {
          urlParams: { orgId },
          params: {
            fullName: departmentName,
            page: 0,
            size: 1,
          },
        })
        .then(res => res.data.totalElements);

      return http
        .get<EmployeeResponse>(SEARCH_EMPLOYEE_EXECUTOR_GROUP, {
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

export const useDeleteExecutorGroup = (
): MutationResultPair<void, AxiosError, { executorGroupId: string | UUID }, unknown> => (
  useAPIMutation(
    ({ http }, { executorGroupId }: { executorGroupId: string | UUID }) => http
      .delete<number>(`organizations/executorGroup/${executorGroupId}`)
      .then(ignore),
    {
      onSuccess: ({
        cache, process,
      }) => {
        process.processStatus(200, 'Группа исполнителей удалена');
        cache.refetchQueries(['ExecutorGroupsRequest']);
      },
      onError: ({ error, logger }) => {
        logger.toMessage('error', getErrorMessage(error));
      },
    }
  )
);
