/* eslint-disable @stylistic/member-delimiter-style */
/* eslint-disable @typescript-eslint/consistent-type-definitions */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-restricted-imports, @typescript-eslint/no-empty-interface */

import {
  useState, useEffect, useRef, useCallback, useMemo
} from 'react';
import { ILogger, ResponseService, IHttpService } from '@sber-sbertransport/mf-core';
import {
  MutationConfig,
  MutationResultPair,
  Query,
  QueryCache,
  QueryConfig,
  QueryResult,
  useMutation,
  useQuery,
  useQueryCache
} from 'react-query';
import type { FetchMoreOptions, RefetchOptions } from 'react-query/types/core/query.d';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import type { Prefixes } from './type-helpers';
import { API_URL, WS_URL } from 'constants/constants.app';
import { ICarActualInfo } from 'stores/Trip/Trip.interface';
import { CAR_LOCATION_REQUEST } from 'constants/constants.env';

export interface Cache {}

export type CacheItem = Cache[keyof Cache];

type APIQueryKey = CacheItem['key'];

export type TypeAtKey<K extends APIQueryKey> = Extract<CacheItem, { key: K; value: unknown }>['value'];

// в условиях Suspense можно считать, что результат выполнения запроса всегда TResult, а не TResult | undefined

interface QueryResultOverrides<TResult> {
  data: TResult;
  refetch: (options?: RefetchOptions) => Promise<TResult>;
  fetchMore: (fetchMoreVariable?: unknown, options?: FetchMoreOptions) => Promise<TResult>;
}

export type APIQueryResult<TResult, TError = unknown> = Omit<
  QueryResult<TResult, TError>,
  keyof QueryResultOverrides<TResult>
> &
QueryResultOverrides<TResult>;

export const useAPI = <K extends APIQueryKey, TError = unknown>(
  key: K,
  queryFn: (services: { http: IHttpService; process: ResponseService }) => TypeAtKey<K> | Promise<TypeAtKey<K>>,
  queryConfig?: QueryConfig<TypeAtKey<K>, TError>
): APIQueryResult<TypeAtKey<K>, TError> => {
  const { http, process } = useAppStoreContext();

  return useQuery(key, () => queryFn({ http, process }), queryConfig) as APIQueryResult<TypeAtKey<K>, TError>;
};

type UpdaterFn<T> = (t: T) => T;

type QueryPredicate = Prefixes<APIQueryKey>;

type InvalidateQueriesOptions = Required<Parameters<QueryCache['invalidateQueries']>>[1];

interface QueryCacheOverrides {
  getQueryData: <K extends APIQueryKey>(predicate: K) => TypeAtKey<K> | undefined;
  setQueryData: <K extends APIQueryKey>(queryKey: K, update: TypeAtKey<K>) => void;
  invalidateQueries: <K extends QueryPredicate>(
    predicate: K,
    options?: InvalidateQueriesOptions
  ) => Promise<Query<unknown, unknown>[]>;
}

export type APIQueryCache = Omit<QueryCache, keyof QueryCacheOverrides> & QueryCacheOverrides;

export const useAPIQueryCache: () => APIQueryCache = () => useQueryCache() as APIQueryCache;

export function updateQueryCache<K extends APIQueryKey>(
  cache: APIQueryCache,
  key: K,
  updater: UpdaterFn<TypeAtKey<K>>
): void {
  const data = cache.getQueryData(key);

  if (data) {
    cache.setQueryData<K>(key, updater(data as never));
  }
}

interface SuccessFnArguments<TResult, TVariables> {
  cache: APIQueryCache;
  result: TResult;
  variables: TVariables;
  process: ResponseService;
  logger: ILogger;
}

interface ErrorFnArguments<TError, TVariables> {
  error: TError;
  variables: TVariables;
  process: ResponseService;
  logger: ILogger;
}

interface MutationConfigOverrides<TResult, TVariables, TError> {
  onSuccess?: (successArgs: SuccessFnArguments<TResult, TVariables>) => Promise<unknown> | void;
  onError?: (errorArgs: ErrorFnArguments<TError, TVariables>) => Promise<unknown> | void; // TODO: make it mandatory
}

export type APIMutationConfig<TResult, TError, TVariables, TSnapshot> = Override<
  MutationConfig<TResult, TError, TVariables, TSnapshot>,
  MutationConfigOverrides<TResult, TVariables, TError>
>;

export function useAPIMutation<TResult, TError = any, TVariables = undefined, TSnapshot = unknown>(
  mutationFn: (services: { http: IHttpService; process: ResponseService }, variables: TVariables) => Promise<TResult>,
  config?: APIMutationConfig<TResult, TError, TVariables, TSnapshot>
): MutationResultPair<TResult, TError, TVariables, TSnapshot> {
  const {
    http, process, logger,
  } = useAppStoreContext();
  const cache = useAPIQueryCache();

  const queryConfig: MutationConfig<TResult, TError, TVariables, TSnapshot> = {
    ...config,
    onSuccess: (result: TResult, variables: TVariables) => config?.onSuccess?.({
      cache, result, variables, process, logger,
    }),
    onError: (error: TError, variables: TVariables) => config?.onError?.({
      error, variables, process, logger,
    }), // TODO: make it mandatory
  };

  return useMutation((variables: TVariables) => mutationFn({ http, process }, variables), queryConfig);
}

/**
 * mapQuery: (f: A => B) => (APIQuery<A, E>) => APIQuery<B, E>
 *
 * given a function f: A => B, and an APIQuery hook useQ<A, E>, returns
 * a new APIQuery hook useQ'<B, E>
 */

export const mapQuery
  = <Q, T>(f: (q: Q) => T) => <Args extends unknown[], E>(useQ: (...args: Args) => APIQueryResult<Q, E>) => (...args: Args): APIQueryResult<T, E> => {
    const q = useQ(...args);

    return useMemo(
      () => ({
        ...q,
        data: f(q.data),
        refetch: (options?: RefetchOptions): Promise<T> => q.refetch(options).then(f),
        fetchMore: (fetchMoreVariable?: unknown, options?: FetchMoreOptions): Promise<T> => q.fetchMore(fetchMoreVariable, options).then(f),
      }),
      [q]
    );
  };

const RESPONSE_TYPES = {
  SUCCESS: 'SUCCESS',
  FAILED: 'FAILED',
  SUBSCRIBED: 'SUBSCRIBED',
  UNSUBSCRIBED: 'UNSUBSCRIBED',
};

const MESSAGE_TYPES = {
  TOKEN: 'TOKEN',
  SUBSCRIPTION: 'SESSION_SUBSCRIPTION',
  UNSUBSCRIPTION: 'UNSUBSCRIPTION',
};

export const useCarActualInfo = (
  token: string,
  requestId: string,
  canOpen: boolean
): ICarActualInfo | undefined => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [carActualInfo, setCarActualInfo] = useState<ICarActualInfo | undefined>();
  const ws = useRef<WebSocket | null>(null);
  const reconnectAttempts = useRef(0);
  const maxReconnectAttempts = 5;

  const sendMessage = useCallback((type, data = {}) => {
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      const message = JSON.stringify({ type, ...data });
      ws.current.send(message);
    }
  }, []);

  const authenticate = useCallback(() => {
    if (token) {
      sendMessage('AUTHORIZE', { token });
    }
  }, [token, sendMessage]);

  // Функция подписки на координаты машины
  const subscribeToCarLocation = useCallback(async () => {
    if (!requestId || !isAuthenticated) return;

    try {
      // запрос на подписку через WebSocket
      sendMessage(MESSAGE_TYPES.SUBSCRIPTION, {
        subscription: 'CAR_LOCATION',
        resourceId: requestId,
        actionType: 'SUBSCRIBE',
      });

      // запрос для активации отправки координатов
      const response = await fetch(`${API_URL}${CAR_LOCATION_REQUEST}${requestId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
      });
      if (!response.ok) {
        throw new Error('Failed to activate car location updates');
      }
    } catch (err) {
      console.error('Subscription error:', err);
    }
  }, [requestId, isAuthenticated, token, sendMessage]);

  const unsubscribe = useCallback(() => {
    if (isSubscribed && requestId) {
      sendMessage(MESSAGE_TYPES.UNSUBSCRIPTION, {
        subscription: 'CAR_LOCATION',
        resourceId: requestId,
        actionType: 'SUBSCRIBE',
      });
      setIsSubscribed(false);
    }
  }, [isSubscribed, requestId, sendMessage]);

  // Обработчик сообщений от WebSocket
  const handleMessage = useCallback(event => {
    try {
      const data = JSON.parse(event.data);
      setIsAuthenticated(true);
      switch (data.type) {
        case RESPONSE_TYPES.SUCCESS:
          setIsAuthenticated(true);
          reconnectAttempts.current = 0;
          break;

        case RESPONSE_TYPES.FAILED:
          setIsAuthenticated(false);
          if (reconnectAttempts.current < maxReconnectAttempts) {
            setTimeout(() => authenticate(), 1000 * (reconnectAttempts.current + 1));
            reconnectAttempts.current++;
          }
          break;

        case RESPONSE_TYPES.SUBSCRIBED:
          setIsSubscribed(true);
          break;

        case RESPONSE_TYPES.UNSUBSCRIBED:
          setIsSubscribed(false);
          break;

        case 'SESSION_SUBSCRIPTION_EVENT':
          setCarActualInfo(data.payload);
          break;

        default:
          console.log('Unknown message type:', data.type);
      }
    } catch (err) {
      console.error('Error parsing WebSocket message:', err);
    }
  }, [authenticate]);

  // управления WebSocket соединением
  useEffect(() => {
    if (!token || !canOpen) return;

    ws.current = new WebSocket(`${API_URL}${WS_URL}`);
    ws.current!.onopen = () => {
      authenticate();
    };

    ws.current!.onmessage = handleMessage;

    ws.current!.onclose = () => {
      setIsAuthenticated(false);
      setIsSubscribed(false);
    };

    ws.current!.onerror = error => {
      console.error('WebSocket error:', error);
    };

    return () => {
      if (ws.current) {
        unsubscribe();
        ws.current!.close();
      }
    };
  }, [token, canOpen]);

  // Эффект для автоматической подписки при успешной авторизации
  useEffect(() => {
    if (isAuthenticated && requestId) {
      subscribeToCarLocation();
    }
  }, [isAuthenticated, requestId, subscribeToCarLocation]);

  return carActualInfo;
};
