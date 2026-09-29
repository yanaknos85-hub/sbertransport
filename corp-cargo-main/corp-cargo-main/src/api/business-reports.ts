import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { CREATE_TASK, CANCEL_TASK, GET_TASKS } from 'constants/constants.api';
import { MutationResultPair } from 'react-query';
import { CreateTaskResponse } from 'stores/BusinessReports/BusinessReports.interface';
import { CargoRegistryFilters } from 'stores/CargoRegistry/CargoRegistry.interface';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';

import { TasksSearchResponse } from 'stores/Tasks/Tasks.interface';
import { ignore } from 'utils';

export interface TasksSearchQuery {
  pagination: PaginationParams;
}
declare module 'api' {
  interface Cache {
    searchTasks: {
      key: ['search-tasks', TasksSearchQuery];
      value: TasksSearchResponse;
    };
    createTask: {
      key: ['create-task'];
    };
    cancelTask: {
      key: ['cancel-task'];
    };
  }
}

export const useTasksWithParams = (
  query: TasksSearchQuery
): APIQueryResult<TasksSearchResponse> => {
  const { pagination } = query;
  return useAPI(
    ['search-tasks', query],
    ({ http, process }) => http
      .post<TasksSearchResponse>(GET_TASKS, { pageSetting: pagination })
      .then(process.decodeResponseData(TasksSearchResponse))
  );
};

export const useCreateTask = (): MutationResultPair<
  CreateTaskResponse,
  unknown,
  CargoRegistryFilters,
  unknown
> => useAPIMutation(
  ({ http, process }, taskItem: CargoRegistryFilters) => http
    .post<CreateTaskResponse>(CREATE_TASK, { ...taskItem })
    .then(response => process.getResponseData(response)),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Forms.businessReports.taskRequestTitles.addTask);
      cache.invalidateQueries(['search-tasks']);
    },
  }
);

export const useCancelTask = () => useAPIMutation(
  ({ http }, taskId: string) => http
    .get<string>(CANCEL_TASK, { urlParams: { taskId } })
    .then(ignore),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Forms.businessReports.taskRequestTitles.cancelTask);
      cache.refetchQueries(['search-tasks']);
    },
    onError: ({ t, logger }) => {
      logger.toMessage('error', t.Forms.businessReports.taskRequestTitles.cancelTaskError);
    },
  }
);
