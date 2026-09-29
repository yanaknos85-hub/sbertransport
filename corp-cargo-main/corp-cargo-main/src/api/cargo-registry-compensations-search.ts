import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';
import { SearchRequestCompensations } from 'stores/CargoRegistry/CargoRegistry.interface';
import { TasksSearchResponse } from 'stores/Tasks/Tasks.interface';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import {
  CARGO_COMPENSATION_REPORT,
  CARGO_REGISTRY_COMPENSATION_DEFAULT_ATTRIBUTES,
  GET_CARGO_REGISTRY_COMPENSATION_USERS_ATTRIBUTES,
  SET_CARGO_REGISTRY_COMPENSATION_USERS_ATTRIBUTES,
  CARGO_COMPENSATION_REPORT_DETAILED,
  CREATE_COMPENSATION_TASK,
  GET_COMPENSATION_TASKS,
  GET_PAYMENT_REGISTRY_COMPENSATION,
  CANCEL_REGISTRY_COMPENSATION,
  CANCEL_COMPENSATION_TASK
} from 'constants/constants.api';
import { APIQueryResult, useAPI, useAPIMutation } from './index';
import { UsersAttributes } from './register-search';
import { CreateTaskResponse } from 'stores/BusinessReports/BusinessReports.interface';
import { CargoCompensationRegistryFilters } from 'stores/CompensationRegistry/CompensationRegistry.interface';
import {
  ColumnVisibilitySettings as ColumnCompensationsVisibilitySettings,
  Compensation,
  CompensationType,
  SearchCompensationResponse
} from '../modules/CargoCompensationsRegistry/types';
import { ignore } from 'utils';

export interface TasksSearchQuery {
  pagination: PaginationParams;
}

declare module 'api' {
  interface Cache {
    cargoRegistryCompensations: { key: ['cargoRegistryCompensations', SearchRequestCompensations]; value: SearchCompensationResponse };
    cargoRegistryCompensation: { key: ['cargoRegistryCompensation']; value: CompensationType };
    defaultCargoCompensationColumns: { key: ['defaultCargoCompensationColumns']; value: UsersAttributes };
    userCargoCompensationSettings: { key: ['userCargoCompensationSettings', string]; value: UsersAttributes };
    searchReports: { key: ['search-reports', TasksSearchQuery, string | null | undefined]; value: TasksSearchResponse };
  }
}

// Запрос списка компенсаций
export const useSearchCargoCompensations = (
  query: SearchRequestCompensations,
  config: QueryConfig<SearchCompensationResponse, unknown>,
  orgId: string | null | undefined
): APIQueryResult<SearchCompensationResponse, AxiosError | unknown> => useAPI(
  ['cargoRegistryCompensations', query],
  ({ http, process }) => orgId
    ? http
      .post<SearchCompensationResponse>(CARGO_COMPENSATION_REPORT, SearchRequestCompensations.encode(query), {
        urlParams: { orgId },
      })
      .then(process.decodeResponseData())
    : ({} as SearchCompensationResponse),
  config
);

// Запрос детального просмотра компенсации
export const useGetSearchCompensationRegistry = (
  _id: string
): APIQueryResult<CompensationType, AxiosError> => useAPI(
  ['cargoRegistryCompensation'],
  ({ http, process: { decodeResponseData } }) => http
    .get<CompensationType>(CARGO_COMPENSATION_REPORT_DETAILED).then(decodeResponseData(Compensation))
);

// Запрос настроек видимости столбцов по умолчанию для компенсаций
export const useDefaultCompensationColumnVisibilitySettings = (
  userId: string
): APIQueryResult<UsersAttributes, AxiosError> => (
  useAPI(
    ['defaultCargoCompensationColumns'], ({ http, process }) => http
      .get<UsersAttributes>(CARGO_REGISTRY_COMPENSATION_DEFAULT_ATTRIBUTES, { urlParams: { userId } })
      .then(process.decodeResponseData(UsersAttributes))
  ));

// Сохранение настроек видимости столбцов для компенсаций
export const useSaveCargoCompensationsUsersAttributes = (
  userId: string
): MutationResultPair<UsersAttributes, unknown, ColumnCompensationsVisibilitySettings, unknown> => useAPIMutation(
  ({ http, process }, settingsAttribute): Promise<UsersAttributes> => http
    .put<void>(SET_CARGO_REGISTRY_COMPENSATION_USERS_ATTRIBUTES, settingsAttribute, { urlParams: { userId } })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries(['settingsAttributes']);
    },
  }
);

// Запрос настроек видимости столбцов пользователя для компенсаций
export const useCargoCompensationUserSettings = (userId: string): APIQueryResult<UsersAttributes, AxiosError> => {
  return useAPI(
    ['userCargoCompensationSettings', userId], ({ http, process }) => http
      .get<UsersAttributes>(GET_CARGO_REGISTRY_COMPENSATION_USERS_ATTRIBUTES, { urlParams: { userId } })
      .then(process.decodeResponseData(UsersAttributes))
  );
};

// Создание задачи на реестр компенсаций
export const useCreateCompensationTask = (): MutationResultPair<
  CreateTaskResponse,
  unknown,
  CargoCompensationRegistryFilters,
  unknown
> => useAPIMutation(
  ({ http, process }, taskItem: CargoCompensationRegistryFilters) => http
    .post<CreateTaskResponse>(CREATE_COMPENSATION_TASK, { ...taskItem })
    .then(response => process.getResponseData(response)),
  {
    onSuccess: ({
      process, t, cache,
    }) => {
      process.processStatus(200, t.Forms.businessReports.taskRequestTitles.addTask);
      cache.refetchQueries(['search-reports']);
    },
  }
);

// Получение таблицы выгрузок компенсаций
export const useTasksWithParams = (
  query: TasksSearchQuery,
  orgId: string | null | undefined
): APIQueryResult<TasksSearchResponse> => {
  const { pagination } = query;
  return useAPI(
    ['search-reports', query, orgId],
    ({ http, process }) => orgId
      ? http
        .post<TasksSearchResponse>(GET_COMPENSATION_TASKS, { pageSetting: pagination }, {
          urlParams: { orgId },
        })
        .then(process.decodeResponseData(TasksSearchResponse))
      : ({} as TasksSearchResponse)
  );
};

// Выплата компенсаций
export const usePaymentCompensation = (): MutationResultPair<
  void,
  unknown,
  CargoCompensationRegistryFilters,
  unknown
> => useAPIMutation(
  ({ http, process }, filters: CargoCompensationRegistryFilters) => http
    .put<void>(GET_PAYMENT_REGISTRY_COMPENSATION, filters)
    .then(process.decodeResponseData()),
  {
    onSuccess: ({
      process, t,
    }) => {
      process.processStatus(200, t.Forms.registryCargoCompensationsMessages.payment);
    },
    onError: ({ process, t }) => {
      process.processStatus(500, t.Forms.registryCargoCompensationsMessages.paymentError);
    },
  }
);

// Отмена выплаты компенсаций
export const useCancelCompensations = (): MutationResultPair<
  void,
  unknown,
  CargoCompensationRegistryFilters,
  unknown
> => useAPIMutation(
  ({ http, process }, filters: CargoCompensationRegistryFilters) => http
    .put<void>(CANCEL_REGISTRY_COMPENSATION, filters)
    .then(process.decodeResponseData()),
  {
    onSuccess: ({
      process, t,
    }) => {
      process.processStatus(200, t.Forms.registryCargoCompensationsMessages.cancel);
    },
    onError: ({ process, t }) => {
      process.processStatus(500, t.Forms.registryCargoCompensationsMessages.cancelError);
    },
  }
);

// Отмена выгрузки по компенсации
export const useCancelTask = () => useAPIMutation(
  ({ http }, taskId: string) => http
    .delete<string>(CANCEL_COMPENSATION_TASK, { urlParams: { taskId } })
    .then(ignore),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Forms.registryCargoCompensationsReports.taskRequestTitles.cancelTask);
      cache.refetchQueries(['search-reports']);
    },
    onError: ({ t, logger }) => {
      logger.toMessage('error', t.Forms.registryCargoCompensationsReports.taskRequestTitles.cancelTaskError);
    },
  }
);
