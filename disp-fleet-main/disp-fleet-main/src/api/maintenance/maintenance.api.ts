import type { MutationResultPair, QueryConfig } from 'react-query';
import type { AxiosError } from 'axios';

import { useAPI, useAPIMutation } from 'api';
import { getErrorMessage } from 'utils/utils';
import {
  MAINTENANCE_MONITOR_URL,
  MAINTENANCE_TAKE_TO_WORK_URL,
  MAINTENANCE_REQUEST_STATUS_UPDATE_URL,
  MAINTENANCE_REPAIR_REQUEST_STATUS_UPDATE_URL,
  MaintenanceTabs
} from './maintenance.constants';
import {
  MaintenanceFilters,
  TMaintenanceMonitorResponse
} from './maintenance.types';

export const MAINTENANCE_MONITOR_KEY = 'maintenanceMonitor';

declare module 'api' {
  interface Cache {
    maintenanceMonitor: {
      key: [typeof MAINTENANCE_MONITOR_KEY, MaintenanceFilters];
      value: TMaintenanceMonitorResponse;
    };
  }
}

export const useMaintenanceMonitor = (
  filters: MaintenanceFilters,
  config?: QueryConfig<TMaintenanceMonitorResponse>
) => {
  const {
    page, size, type, ...rest
  } = filters;

  return useAPI(
    [MAINTENANCE_MONITOR_KEY, filters],
    ({ http, process }) => (
      http
        .post<TMaintenanceMonitorResponse>(MAINTENANCE_MONITOR_URL, {
          pageSetting: {
            page,
            size,
          },
          ...rest,
        }, {
          urlParams: { type },
        })
        .then(process.getResponseData)
    ),
    config
  );
};

/** Взятие заявки в работу (take_to_work) */
export const useMaintenanceTakeToWork = (
  type: MaintenanceTabs
): MutationResultPair<
  unknown,
  AxiosError<Error>,
  string,
  unknown
> => {
  const HTTP_METHOD = type === MaintenanceTabs.Registration ? 'patch' : 'post';

  return useAPIMutation(({ http, process }, requestId) => (
    http[HTTP_METHOD](MAINTENANCE_TAKE_TO_WORK_URL, {}, { urlParams: { type, requestId } })
      .then(process.getResponseData)
  ), {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([MAINTENANCE_MONITOR_KEY]);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  });
};

interface IStatusUpdateRequest {
  requestId: string;
  status: string;
}

/** Обновление статуса заявки (PATCH) */
export const useMaintenanceRequestStatusUpdate = (
  type: MaintenanceTabs
): MutationResultPair<
  unknown,
  AxiosError<Error>,
  IStatusUpdateRequest,
  unknown
> => {
  const STATUS_UPDATE_URL = type === MaintenanceTabs.Repair
    ? MAINTENANCE_REPAIR_REQUEST_STATUS_UPDATE_URL
    : MAINTENANCE_REQUEST_STATUS_UPDATE_URL;

  return useAPIMutation(({ http, process }, { requestId, status }) => (
    http
      .patch(STATUS_UPDATE_URL, { status }, { urlParams: { type, requestId } })
      .then(process.getResponseData)
  ), {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([MAINTENANCE_MONITOR_KEY]);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  });
};
