/* eslint-disable no-restricted-imports, @typescript-eslint/no-empty-interface */
import * as React from 'react';
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
import { IHttpService, ILogger, ResponseService } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import type { Prefixes } from './type-helpers';

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
    // @ts-ignore
    cache.setQueryData<K>(key, updater(data));
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
  onSuccess: (successArgs: SuccessFnArguments<TResult, TVariables>) => Promise<unknown> | void;
  onError?: (errorArgs: ErrorFnArguments<TError, TVariables>) => Promise<unknown> | void; // TODO: make it mandatory
}

type APIMutationConfig<TResult, TError, TVariables, TSnapshot> = Override<
  MutationConfig<TResult, TError, TVariables, TSnapshot>,
  MutationConfigOverrides<TResult, TVariables, TError>
>;

export function useAPIMutation<TResult, TError = any, TVariables = undefined, TSnapshot = unknown>(
  mutationFn: (services: { http: IHttpService; process: ResponseService }, variables: TVariables) => Promise<TResult>,
  config: APIMutationConfig<TResult, TError, TVariables, TSnapshot>
): MutationResultPair<TResult, TError, TVariables, TSnapshot> {
  const {
    http, process, logger,
  } = useAppStoreContext();
  const cache = useAPIQueryCache();

  const queryConfig: MutationConfig<TResult, TError, TVariables, TSnapshot> = {
    ...config,
    onSuccess: (result: TResult, variables: TVariables) => config.onSuccess({
      cache, result, variables, process, logger,
    }),
    onError: (error: TError, variables: TVariables) => config.onError && config.onError({
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

    return React.useMemo(
      () => ({
        ...q,
        data: f(q.data),
        refetch: (options?: RefetchOptions): Promise<T> => q.refetch(options).then(f),
        fetchMore: (fetchMoreVariable?: unknown, options?: FetchMoreOptions): Promise<T> => q.fetchMore(fetchMoreVariable, options).then(f),
      }),
      [q]
    );
  };
