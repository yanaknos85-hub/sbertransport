import { ReactQueryCacheProvider } from 'react-query';
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-restricted-imports, @typescript-eslint/no-empty-interface */
import { ILogger, ResponseService, IHttpService } from '@sber-sbertransport/mf-core';
import { Translation, useTranslation } from 'i18n';
import * as React from 'react';

import {
  QueryCache,
  QueryConfig,
  QueryResult,
  useQuery,
  useQueryCache,
  Query,
  useMutation,
  MutationResultPair,
  MutationConfig
} from 'react-query';
import type { FetchMoreOptions, RefetchOptions } from 'react-query/types/core/query.d';

import { useAppStore } from 'ioc';

import type { Prefixes } from './type-helpers';
import { useProfile } from './profile/profile.api';
import { useToken } from 'hooks/useToken';
import { AxiosResponse } from 'axios';
import * as t from 'io-ts';
import { PingPong, WEBSOCKET_CONNECTION_STATUS } from 'constants/app.constants';
import moment from 'moment';
import { WEBSOCKET_URL } from 'constants/api.constants';

export interface Cache { }

export type CacheItem = Cache[keyof Cache];

type APIQueryKey = CacheItem['key'];

export type TypeAtKey<K extends APIQueryKey> = Extract<CacheItem, { key: K; value: unknown }>['value'];

// в условиях Suspense можно считать, что результат выполнения запроса всегда TResult, а не TResult | undefined

// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface QueryResultOverrides<TResult, TError> {
  data: TResult;
  refetch: (options?: RefetchOptions) => Promise<TResult>;
  fetchMore: (fetchMoreVariable?: unknown, options?: FetchMoreOptions) => Promise<TResult>;
}

export type APIQueryResult<TResult, TError = unknown> = Override<
  QueryResult<TResult, TError>,
  QueryResultOverrides<TResult, TError>
>;

export const useAPI = <K extends APIQueryKey, TError = unknown>(
  key: K,
  queryFn: (services: { http: IHttpService; process: ResponseService }) => TypeAtKey<K> | Promise<TypeAtKey<K>>,
  queryConfig?: QueryConfig<TypeAtKey<K>, TError>
): APIQueryResult<TypeAtKey<K>, TError> => {
  const { http, process } = useAppStore();

  // @ts-ignore
  return useQuery(key, () => queryFn({ http, process }), { retry: false, ...queryConfig }) as APIQueryResult<
    TypeAtKey<K>,
    TError
  >;
};

type UpdaterFn<T> = (t: T) => T;

type QueryPredicate = Prefixes<APIQueryKey>;

type InvalidateQueriesOptions = Required<Parameters<QueryCache['invalidateQueries']>>[1];

interface QueryCacheOverrides {
  getQueryData: <K extends APIQueryKey>(predicate: K) => TypeAtKey<K> | undefined;
  setQueryData: <K extends APIQueryKey>(queryKey: K, update: TypeAtKey<K>) => void;
  invalidateQueries: <K extends QueryPredicate>(
    predicate: K,
    options?: InvalidateQueriesOptions,
  ) => Promise<Query<unknown, unknown>[]>;
}

export type APIQueryCache = Override<QueryCache, QueryCacheOverrides>;

export const useAPIQueryCache: () => APIQueryCache = () => useQueryCache() as APIQueryCache;

export function updateQueryCache<K extends APIQueryKey>(
  cache: APIQueryCache,
  key: K,
  updater: UpdaterFn<TypeAtKey<K>>
): void {
  const data = cache.getQueryData(key);

  if (data) {
    // @ts-ignore
    cache.setQueryData<K>(key, updater(data));
  }
}

interface SuccessFnArguments<TResult, TVariables> {
  cache: APIQueryCache;
  result: TResult;
  variables: TVariables;
  process: ResponseService;
  t: Translation;
  logger: ILogger;
}

interface ErrorFnArguments<TError, TVariables> {
  error: TError;
  variables: TVariables;
  process: ResponseService;
  t: Translation;
  logger: ILogger;
}

interface MutationConfigOverrides<TResult, TVariables, TError> {
  onSuccess: (successArgs: SuccessFnArguments<TResult, TVariables>) => Promise<unknown> | void;
  onError?: (errorArgs: ErrorFnArguments<TError, TVariables>) => Promise<unknown> | void; // TODO: make it mandatory
}

export type APIMutationConfig<TResult, TError, TVariables, TSnapshot> = Override<
  MutationConfig<TResult, TError, TVariables, TSnapshot>,
  MutationConfigOverrides<TResult, TVariables, TError>
>;

export type MutationResultPairWithProgress<TResult, TError, TVariables, TSnapshot> = [
  MutationResultPair<TResult, TError, TVariables, TSnapshot>[0],
  MutationResultPair<TResult, TError, TVariables, TSnapshot>[1],
  number
];

export function useAPIMutation<TResult, TError = any, TVariables = undefined, TSnapshot = unknown>(
  mutationFn: (services: { http: IHttpService; process: ResponseService }, variables: TVariables) => Promise<TResult>,
  config: APIMutationConfig<TResult, TError, TVariables, TSnapshot>
): MutationResultPair<TResult, TError, TVariables, TSnapshot> {
  const {
    http, process, logger,
  } = useAppStore();
  const { t } = useTranslation();
  const cache = useAPIQueryCache();

  const queryConfig: MutationConfig<TResult, TError, TVariables, TSnapshot> = {
    ...config,
    onSuccess: (result: TResult, variables: TVariables) => config.onSuccess({
      cache, result, variables, process, t, logger,
    }),
    onError: (error: TError, variables: TVariables) => config.onError && config.onError({
      error, variables, process, t, logger,
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

export const mapQuery = <Q, T>(f: (q: Q) => T) => <Args extends unknown[], E>(
  useQ: (...args: Args) => APIQueryResult<Q, E>
) => (...args: Args): APIQueryResult<T, E> => {
    const q = useQ(...args);

    const t: APIQueryResult<T, E> = React.useMemo(
      () => ({
        ...q,
        data: f(q.data),
        refetch: (options?: RefetchOptions): Promise<T> => q.refetch(options).then(f),
        // eslint-disable-next-line @stylistic/max-len
        fetchMore: (fetchMoreVariable?: unknown, options?: FetchMoreOptions): Promise<T> => q.fetchMore(fetchMoreVariable, options).then(f),
      }),
      [q]
    );

    return t;
  };

enum WEBSOCKET_CODES {
  UNMOUNT_CLOSE_CODE = 4000,
  INCORRECT_TOKEN = 4001,
  NO_PONG = 4002,
}

const MAX_RECONNECTIONS = 2;
const PING_PONG_TIME = 20000;
const PONG_WAITING_TIME = 3000;

interface LastMessage<T = unknown> {
  data: T;
  time: string;
}

export const useWebsocket = <TResponse = unknown, TSend = unknown>(
  route: string,
  type: any = t.unknown,
  { errorMessage }: { errorMessage?: string } = {}
) => {
  const [websocket, setWebsocket] = React.useState<WebSocket>();
  const [isOpened, setIsOpened] = React.useState(false);
  const [connectionCount, setConnectionCount] = React.useState(0);
  const [lastMessage, setLastMessage] = React.useState<LastMessage<TResponse>>();

  const reconnections = React.useRef(0);

  const { logger, process } = useAppStore();
  const { id } = useProfile().data;
  const token = useToken();
  const { t: labels } = useTranslation();

  const { authStore } = useAppStore();

  React.useEffect(() => {
    const url = WEBSOCKET_URL + route;

    const _websocket = new WebSocket(`${url}/${id}/`);

    setWebsocket(_websocket);

    return () => {
      _websocket.close(WEBSOCKET_CODES.UNMOUNT_CLOSE_CODE, 'Component was unmounted');
      setWebsocket(undefined);
    };
  }, [logger, route, id, connectionCount]);

  React.useEffect(() => {
    if (!websocket || !token) return;

    websocket.onopen = async () => {
      const config = await authStore.checkAuth();
      const [, newToken] = config.headers.Authorization.split(' ');
      websocket.send(newToken);
    };
  }, [websocket, token, authStore.checkAuth]);

  React.useEffect(() => {
    if (!websocket) return;

    let keepConnectionTimer: NodeJS.Timeout,
      pongAwaitingTimer: NodeJS.Timeout;

    const keepConnection = () => {
      websocket.send(PingPong.Ping);
      pongAwaitingTimer = setTimeout(() => websocket.close(WEBSOCKET_CODES.NO_PONG), PONG_WAITING_TIME);
    };

    websocket.onmessage = e => {
      switch (e.data) {
        case WEBSOCKET_CONNECTION_STATUS.SUCCESS: {
          reconnections.current = 0;
          setIsOpened(true);
          keepConnectionTimer = setTimeout(keepConnection, PING_PONG_TIME);
          break;
        }

        case WEBSOCKET_CONNECTION_STATUS.FAILED: {
          websocket.close(WEBSOCKET_CODES.INCORRECT_TOKEN);
          break;
        }

        case PingPong.Pong: {
          keepConnectionTimer = setTimeout(keepConnection, PING_PONG_TIME);
          clearTimeout(pongAwaitingTimer);
          break;
        }

        default: {
          let response: TResponse;
          try {
            response = JSON.parse(e.data);
          } catch {
            response = e.data;
          }

          response = process.decodeResponseData<TResponse>(type)({
            data: response,
          } as unknown as AxiosResponse<TResponse>);

          setLastMessage({
            data: response,
            time: moment().format(),
          });

          return response;
        }
      }
    };

    websocket.onclose = e => {
      clearTimeout(keepConnectionTimer);
      setIsOpened(false);

      if (e.code === WEBSOCKET_CODES.UNMOUNT_CLOSE_CODE) return;

      if (reconnections.current < MAX_RECONNECTIONS) {
        reconnections.current++;
        setTimeout(() => setConnectionCount(prev => prev + 1), reconnections.current * 1000);
      } else {
        logger.toNotify(
          'error',
          errorMessage ?? labels.Websockets.autorefreshUnavaliable,
          `${labels.Websockets.connectionError} [${e.code}]`
        );
      }
    };

    return () => {
      clearTimeout(keepConnectionTimer);
      clearTimeout(pongAwaitingTimer);
    };
  }, [websocket]);

  const sendMessage = React.useCallback(
    (data: TSend) => websocket?.send(typeof data === 'string' ? data : JSON.stringify(data)),
    [websocket]
  );

  return {
    isOpened,
    lastMessage,
    sendMessage,
  };
};

const queryCache = new QueryCache({
  defaultConfig: {
    queries: {
      suspense: true,
      staleTime: Infinity,
    },
    mutations: {
      throwOnError: true,
    },
  },
});

export { queryCache, ReactQueryCacheProvider };

export enum ErrorTypes {
  DRIVER_SHIFT_DATE_CONFLICT = 'DRIVER_SHIFT_DATE_CONFLICT',
}

export interface Constraint {
  type: ErrorTypes;
  value: any;
}

export interface Problem {
  field: string;
  value: string;
  constraints?: Constraint[];
}

export interface DefaultError {
  message: string;
  path: string;
  timestamp: string;
  problems: Problem[];
}
