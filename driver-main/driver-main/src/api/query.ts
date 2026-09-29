import { QueryClient, UseBaseQueryOptions, UseSuspenseQueryOptions } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

export interface Cache { }

export type CacheItem = Cache[keyof Cache];

type APIQueryKey = CacheItem['key'];

export interface BaseQueryOptions<
  TQueryFnData = unknown,
  TError = AxiosError,
  TData = TQueryFnData,
  TQueryData = TQueryFnData
> extends Omit<UseBaseQueryOptions<TQueryFnData, TError, TData, TQueryData, APIQueryKey>, 'queryKey' | 'queryFn'> { }

export interface BaseSuspenseQueryOptions<
  TQueryFnData = unknown,
  TError = AxiosError,
  TData = TQueryFnData
> extends Omit<UseSuspenseQueryOptions<TQueryFnData, TError, TData, APIQueryKey>, 'queryKey' | 'queryFn'> { }
