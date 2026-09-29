import { QueryConfig } from 'react-query';
import { SelfOrganizationDispatcherResponse, TSelfOrganizationDispatcherResponse } from './telemechanicDispatchers.types';
import { useAPI } from 'api';
import { TELEMECHANIC_DISPATCHERS_SELF } from './telemechanicDispatchers.constants';

export const TELEMECHANIC_DISPATCHERS_SELF_KEY = 'telemechanicDispatchersSelf';

declare module 'api' {
  interface Cache {
    telemechanicDispatchersSelf: {
      key: [typeof TELEMECHANIC_DISPATCHERS_SELF_KEY];
      value: TSelfOrganizationDispatcherResponse;
    };
  }
}

/** Запрос на получение данных по организации диспетчера (с проверкой возможности создавать ЭПЛ) */
export const useGetSelfOrganizationDispatcher = (config?: QueryConfig<TSelfOrganizationDispatcherResponse>) => (
  useAPI(
    [TELEMECHANIC_DISPATCHERS_SELF_KEY],
    ({ http, process }) => (
      http
        .get<TSelfOrganizationDispatcherResponse>(TELEMECHANIC_DISPATCHERS_SELF)
        .then(process.decodeResponseData(SelfOrganizationDispatcherResponse))
    ),
    {
      staleTime: 60_000,
      cacheTime: 60_000,
      ...config,
    }
  )
);
