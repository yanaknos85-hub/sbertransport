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

export interface Cache {}

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
