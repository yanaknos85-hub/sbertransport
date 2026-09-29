import { MutationResultPair, QueryConfig } from 'react-query';
import * as t from 'io-ts';
import { AxiosError } from 'axios';
import {
  SearchResponse,
  TransportType,
  SearchRequestRoutes
} from 'stores/CargoRegistry/CargoRegistry.interface';
import {
  CARGO_ROUTE_REPORT,
  GET_CARGO_TRANSPORT_TYPES,
  CARGO_REGISTRY_ROUTES_DEFAULT_ATTRIBUTES,
  REQUESTS_JOURNAL_ROUTE_MULTIPLE,
  GET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES,
  SET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES
} from 'constants/constants.api';
import { APIQueryResult, useAPI, useAPIMutation } from './index';
import { UsersAttributes } from './register-search';
import {
  ColumnVisibilitySettings as ColumnRoutesVisibilitySettings,
  MonitorRouteResponseType,
  RouteType
} from '../modules/CargoRoutesRegistry/types';

declare module 'api' {
  interface Cache {
    cargoRegistryRoutes: { key: ['cargoRegistryRoutes', SearchRequestRoutes]; value: MonitorRouteResponseType };
    cargoRegistryRoute: { key: ['cargoRegistryRoute', string]; value: RouteType };
    cargoTransportTypes: { key: ['cargoTransportTypes']; value: TransportType[] };
    defaultCargoRouteColumns: { key: ['defaultCargoRouteColumns']; value: UsersAttributes };
    userCargoRouteSettings: { key: ['userCargoRouteSettings', string]; value: UsersAttributes };
  }
}

// Запрос списка маршрутов
export const useSearchCargoRegistry = (
  query: SearchRequestRoutes,
  config: QueryConfig<MonitorRouteResponseType, unknown>
): APIQueryResult<MonitorRouteResponseType, AxiosError | unknown> => useAPI(
  ['cargoRegistryRoutes', query],
  ({ http, process }) => http
    .post<SearchResponse>(CARGO_ROUTE_REPORT, SearchRequestRoutes.encode(query))
    .then(process.decodeResponseData()),
  config
);

// Запрос маршрута
export const useGetSearchRouteRegistry = (requestId: string): APIQueryResult<RouteType, AxiosError> => useAPI(
  ['cargoRegistryRoute', requestId], ({ http, process: { decodeResponseData } }) => http
    .get<RouteType>(`${REQUESTS_JOURNAL_ROUTE_MULTIPLE}`, { urlParams: { requestId } })
    .then(decodeResponseData())
);

// Запрос типа транспорта
export const useCargoTransportTypes = (): APIQueryResult<TransportType[], AxiosError> => useAPI(
  ['cargoTransportTypes'], ({ http, process }) => http
    .get<TransportType[]>(GET_CARGO_TRANSPORT_TYPES)
    .then(process.decodeResponseData(t.array(TransportType)))
);

// Запрос настроек видимости столбцов по умолчанию
export const useDefaultColumnVisibilitySettings = (userId: string): APIQueryResult<UsersAttributes, AxiosError> => (
  useAPI(
    ['defaultCargoRouteColumns'], ({ http, process }) => http
      .get<UsersAttributes>(CARGO_REGISTRY_ROUTES_DEFAULT_ATTRIBUTES, { urlParams: { userId } })
      .then(process.decodeResponseData(UsersAttributes))
  ));

// Сохранение настроек видимости столбцов
export const useSaveCargoRoutesUsersAttributes = (
  userId: string
): MutationResultPair<UsersAttributes, unknown, ColumnRoutesVisibilitySettings, unknown> => useAPIMutation(
  ({ http, process }, settingsAttribute): Promise<UsersAttributes> => http
    .put<void>(SET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES, settingsAttribute, { urlParams: { userId } })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries(['settingsAttributes']);
    },
  }
);

// Запрос настроек видимости столбцов пользователя
export const useCargoUserSettings = (userId: string): APIQueryResult<UsersAttributes, AxiosError> => {
  return useAPI(
    ['userCargoRouteSettings', userId], ({ http, process }) => http
      .get<UsersAttributes>(GET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES, { urlParams: { userId } })
      .then(process.decodeResponseData(UsersAttributes))
  );
};
