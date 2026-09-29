/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/ban-types */
import { APIQueryResult, useAPI, useAPIMutation } from 'api';

import { AxiosError, AxiosResponse } from 'axios';
import { MutationResultPair, QueryConfig } from 'react-query';
import {
  RouteType,
  RouteResponseType,
  OrderType,
  OrderResponseType,
  OrdersFiltersType,
  RoutesFiltersType,
  RequestRouteType,
  UpdateRouteV2PayloadType,
  DepartmentsResponseType,
  DepartmentSearchQuery,
  RouteStatusEnum,
  DataRouteType,
  MonitorFiltersType,
  MonitorRouteResponseType,
  MonitorRouteType,
  OrderDetailedType,
  CancelRouteType
} from 'modules/Planner/types';

import {
  PLANNER_CREATE_ROUTE_MULTIPLE,
  PLANNER_SEARCH_ROUTES_MULTIPLE,
  PLANNER_GET_ROUTE_MULTIPLE,
  PLANNER_GET_ORDER_MULTIPLE,
  PLANNER_SEARCH_ORDERS_MULTIPLE,
  PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE,
  PLANNER_DELETE_ROUTE_MULTIPLE,
  PLANNER_UPDATE_ROUTE_STATUS_MULTIPLE,
  PLANNER_DELETE_ADDRESS_POINT_MULTIPLE,
  PLANNER_GET_CONTRACTORS_MULTIPLE,
  REQUESTS_JOURNAL_ROUTES_MULTIPLE,
  REQUESTS_JOURNAL_ROUTE_MULTIPLE,
  AUTO_PLANNING_MULTIPLE,
  PLANNER_SEND_TO_CONTRACTOR_MULTIPLE,
  CANCEL_ROUTE,
  REQUESTS_CHANGE_ROUTE,
  PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE_V2
} from '../constants/constants.api';

import { UUID } from '../utils/io-ts';
import { EmployeeResponse, PaginationParams } from '../stores/Employee/Employee.interface';
import { EmployeeSearchQuery } from './employee/search';
import { getErrorMessage } from '../utils';
import { RouteLoadersPayload } from '../modules/OrderExecution/interfaces/Orders.types';

declare module 'api' {
  interface Cache {
    routesMulti: {
      key: ['routesMulti', RouteType | undefined | {}];
      value: RouteResponseType;
    };
    monitorRoutesMulti: {
      key: ['monitorRoutesMulti', RouteType | undefined | {}];
      value: MonitorRouteResponseType;
    };
    ordersMulti: {
      key: ['ordersMulti', OrderType | undefined | {}];
      value: OrderResponseType;
    };
    routeMulti: {
      key: ['routeMulti', string];
      value: RouteType;
    };
    orderMulti: {
      key: ['orderMulti', string];
      value: OrderType;
    };
    filteredRoutesMulti: {
      key: ['filteredRoutesMulti', RoutesFiltersType | undefined];
      value: RouteResponseType;
    };
    filteredOrdersMulti: {
      key: ['filteredOrdersMulti', OrdersFiltersType | undefined];
      value: RouteResponseType;
    };
    createRouteMulti: {
      key: ['createRouteMulti'];
      value: { routesMulti: RouteType & RequestRouteType };
    };
    departmentsSearch: {
      key: ['departmentsSearch', DepartmentSearchQuery, PaginationParams];
      value: DepartmentsResponseType;
    };
    employees: {
      key: ['employees', UUID, EmployeeSearchQuery, PaginationParams];
      value: EmployeeResponse;
    };
    autoPlanning: {
      key: ['autoPlanning'];
      value: any;
    };
    typeVehicle: {
      key: ['typeVehicle', string | undefined, string | undefined, string | undefined];
      value: DataRouteType[];
    };
    routeCancel: { key: ['routeCancel']; value: string };
  }
}

export const useSearchRoutes = (
  query: RoutesFiltersType | {} | undefined,
  config?: QueryConfig<RouteResponseType, unknown>
) => useAPI(
  ['routesMulti', query],
  ({ http, process }) => http
  // @ts-ignore
    .post<RouteResponseType>(PLANNER_SEARCH_ROUTES_MULTIPLE, query)
    .then(process.decodeResponseData())
    .catch(() => [] as any),
  {
    keepPreviousData: true,
    ...config,
  }
);

export const useSearchOrders = (
  query: OrderResponseType | undefined | {},
  config?: QueryConfig<OrderResponseType, unknown>
) => useAPI(
  ['ordersMulti', query],
  ({ http, process }) => http
  // @ts-ignore
    .post<OrderResponseType>(PLANNER_SEARCH_ORDERS_MULTIPLE, query)
    .then(process.decodeResponseData())
    .catch(() => [] as any),
  {
    keepPreviousData: true,
    ...config,
  }
);

export const useSearchMonitorRoutes = (
  query: MonitorFiltersType | {} | undefined,
  config?: QueryConfig<MonitorRouteResponseType, unknown>
) => useAPI(
  ['monitorRoutesMulti', query],
  ({ http, process }) => http
  // @ts-ignore
    .post<MonitorRouteResponseType>(REQUESTS_JOURNAL_ROUTES_MULTIPLE, query, {})
    .then(process.decodeResponseData())
    .catch(() => [] as any),
  {
    keepPreviousData: true, cacheTime: 3000, retry: 3,
    ...config,
  }
);

export const useGetSearchMonitorRoute = (requestId: UUID): APIQueryResult<OrderDetailedType, AxiosError> => useAPI(
  ['orderMulti', requestId],
  ({ http, process: { decodeResponseData } }) => http
    .get<RouteType>(`${REQUESTS_JOURNAL_ROUTE_MULTIPLE}`, { urlParams: { requestId } })
    .then(decodeResponseData())
);

export const useAutoPlanning = (query: undefined | {}) => useAPI(
  ['autoPlanning'],
  ({ http, process }) => http
  // @ts-ignore
    .post<any>(AUTO_PLANNING_MULTIPLE, query, {})
    .then(process.decodeResponseData())
    .catch(() => [] as any),
  { enabled: false }
);

export const useGetRoute = (routeId: string): APIQueryResult<RouteType, AxiosError> => useAPI(
  ['routeMulti', routeId],
  ({ http, process: { decodeResponseData } }) => http
    .get<RouteType>(`${PLANNER_GET_ROUTE_MULTIPLE}`, { urlParams: { routeId } })
    .then(decodeResponseData()),
  { staleTime: Infinity }
);

export const useGetOrder = (orderId: string): APIQueryResult<OrderType, AxiosError> => useAPI(
  ['orderMulti', orderId],
  ({ http, process: { decodeResponseData } }) => http
    .get<RouteType>(`${PLANNER_GET_ORDER_MULTIPLE}`, { urlParams: { orderId } })
    .then(decodeResponseData())
);

export const useCreateRoute = (
): MutationResultPair<RouteType, AxiosError, RequestRouteType, unknown> => useAPIMutation(
  ({ http, process }, route: RequestRouteType) => (
    http.post<RouteType>(PLANNER_CREATE_ROUTE_MULTIPLE, route).then(process.getResponseData)
  ), {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Маршрут создан');
      cache.refetchQueries(['routesMulti']);
      cache.refetchQueries(['ordersMulti']);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error)); // TODO нужно исправить чтобы кот не появлялся, уточнить
    },
  }
);

export const useUpdateLoaders = (id: string): MutationResultPair<
  RouteLoadersPayload[],
  unknown,
  { requestId: string; loaders: number },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, loaders,
  }) => http
    .patch<RouteLoadersPayload[]>(REQUESTS_CHANGE_ROUTE, [{ field: 'LOADERS', value: loaders }], {
      urlParams: {
        requestId,
      },
    })
    .then(process.getResponseData), {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные обновлены');
      cache.refetchQueries(['orderMulti', id]);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Не удалось обновить маршрут');
    },
  }
);
export const useChangeCargoTransferTime = (): MutationResultPair<
  MonitorRouteType,
  unknown,
  { requestId: UUID; transferTime: string; status: string },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, transferTime, status,
  }) => http
    .post<MonitorRouteType>('ДОЖДАТЬСЯ АПИ ОТ БЭКА Плановая дата отправления', {}, {
      urlParams: {
        requestId, transferTime, status,
      },
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные обновлены');
      cache.refetchQueries(['monitorRoutesMulti']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Не удалось обновить маршрут');
    },
  }
);

export const useChangeCargoShipmentTime = (): MutationResultPair<
  MonitorRouteType,
  unknown,
  { requestId: UUID; shipmentTime: string; status: string },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, shipmentTime, status,
  }) => http
    .post<MonitorRouteType>('ДОЖДАТЬСЯ АПИ ОТ БЭКА Фактическая дата доставки', {}, {
      urlParams: {
        requestId, shipmentTime, status,
      },
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные обновлены');
      cache.refetchQueries(['monitorRoutesMulti']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Не удалось обновить маршрут');
    },
  }
);

export const useDeleteRoute = (): MutationResultPair<AxiosResponse<number>, unknown, string, unknown> => useAPIMutation(
  ({ http }, routeId: string) => http.delete(PLANNER_DELETE_ROUTE_MULTIPLE, { urlParams: { routeId } }),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Маршрут удалён');
      cache.refetchQueries(['routesMulti']);
      cache.refetchQueries(['ordersMulti']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Маршрут не удалён');
    },
  });

export const useSendContractorRoute = (
): MutationResultPair<AxiosResponse<number>, unknown, string, unknown> => useAPIMutation(
  ({ http }, routeId: string) => http.put(PLANNER_SEND_TO_CONTRACTOR_MULTIPLE, {}, { urlParams: { routeId } }),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Маршрут отправлен контрагенту');
      cache.refetchQueries(['routesMulti']);
      cache.refetchQueries(['ordersMulti']);
    },
  });

export const useDeleteAddressPoint = (
): MutationResultPair<AxiosResponse<RouteType>, AxiosError, string, unknown> => useAPIMutation(
  ({ http }, waypointId: string) => http.delete(PLANNER_DELETE_ADDRESS_POINT_MULTIPLE, { urlParams: { waypointId } }),
  {
    onSuccess: ({
      cache, result: route, process,
    }) => {
      process.processStatus(200, 'Адрес удалён');
      cache.refetchQueries(['routeMulti', route.data.id]);
      cache.refetchQueries(['routesMulti']);
      cache.refetchQueries(['ordersMulti']);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error));
    },
  }
);

/* для смены адресов и смены контрагента в детальном просмотре */
export const useUpdateRoute = () => useAPIMutation(
  ({ http, process }, route: RouteType | { tariffId?: string }) => http
    .post<RouteType>(PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE, route)
    .then(process.getResponseData),
  {
    onSuccess: ({
      cache, result: route, process,
    }) => {
      if (route !== null) {
        process.processStatus(200, 'Маршрут обновлён');
        cache.refetchQueries(['routeMulti', route?.id]);
        cache.refetchQueries(['routesMulti']);
        cache.refetchQueries(['ordersMulti'], {});
      }
    },
    onError: ({ error, logger }) => logger.toMessage('error', error.response.data.message),
  }
);

/* для добавления заявки в маршрут во всех вариантах */
export const useUpdateRouteV2 = () => useAPIMutation(
  ({ http }, payload: UpdateRouteV2PayloadType) => http
    .post<void>(PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE_V2, payload.requests, {
      urlParams: { routeListId: payload.routeListId },
    })
    .then(() => null),
  {
    onSuccess: ({
      cache, process, variables,
    }) => {
      process.processStatus(200, 'Маршрут обновлён');
      cache.refetchQueries(['routeMulti', variables?.routeListId]);
      cache.refetchQueries(['routesMulti']);
      cache.refetchQueries(['ordersMulti'], {});
    },
    onError: ({ error, logger }) => logger.toMessage('error', error.response.data.message),
  }
);

export const useUpdateDetailedViewRoute = (routeId: string) => useAPIMutation(
  ({ http, process }, route: RouteType | { tariffId?: string }) => http
    .post<RouteType>(PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE, route)
    .then(process.getResponseData),
  {
    onSuccess: ({
      cache, result: route, process,
    }) => {
      if (route !== null) {
        process.processStatus(200, 'Маршрут обновлён');
        // Обновляем кэш напрямую данными из ответа сервера
        cache.invalidateQueries(['routeMulti', routeId], route);
        cache.invalidateQueries(['routesMulti']);
      }
    },
    onError: ({ error, logger }) => logger.toMessage('error', error.response?.data?.message || error.message),
  }
);

export const useUpdateRouteStatus = () => useAPIMutation(
  ({ http, process }, routeData: { routelistId: string; status: RouteStatusEnum }) => http
    .post<RouteType>(PLANNER_UPDATE_ROUTE_STATUS_MULTIPLE, {}, { urlParams: routeData })
    .then(process.getResponseData),
  {
    onSuccess: ({
      cache, result: route, process,
    }) => {
      if (route?.id) {
        process.processStatus(200, 'Статус успешно изменен');
        cache.refetchQueries(['routeMulti', route.id]);
        cache.refetchQueries(['routesMulti']);
      }
    },
    onError: ({ logger }) => logger.toMessage('error', 'Статус не обновлён'),
  }
);

export const useGetContractorsTypes = (
  regionId: string | undefined,
  desiredDate: string | undefined,
  organizationId: string | undefined,
  cargoCategory: string | undefined
): APIQueryResult<DataRouteType[], AxiosError> => useAPI(['typeVehicle', regionId, desiredDate, cargoCategory],
  ({ http, process: { decodeResponseData } }) => http
    .get<DataRouteType[]>(`${PLANNER_GET_CONTRACTORS_MULTIPLE}`, {
      urlParams: {
        regionId: regionId as string,
        desiredDate: desiredDate as string,
        organizationId: organizationId as string,
        cargoCategory: cargoCategory as string,
      },
    })
    .then(decodeResponseData())
    .catch(() => [] as any)
);

export const useCancelRoute = (): MutationResultPair<
  unknown,
  unknown,
  CancelRouteType,
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, field, value, code, reason,
  }) => (
    http
      .patch<CancelRouteType>(
        CANCEL_ROUTE,
        [{
          field,
          value,
          code,
          reason,
        }],
        { urlParams: { requestId } }
      )
      .then(process.getResponseData)
  ),
  {
    onSuccess: (
      {
        process,
        t: tf,
        cache,
      }
    ) => {
      process.processStatus(200, tf.Monitor.isCancelRoute);
      cache.refetchQueries(['monitorRoutesMulti']);
      cache.refetchQueries(['ordersMulti']);
      cache.refetchQueries(['routesMulti']);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error as AxiosError));
    },
  }
);
