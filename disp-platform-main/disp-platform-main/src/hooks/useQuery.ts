// import { PaginationParams, SortParams } from 'stores/Pagination/Pagination.interface';
import { useCallback, useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';

import useUrl from './useUrl';
import { PaginationParams, SortParams } from 'utils/io-ts/pagination';
import { setIntoUrl } from 'utils/setIntoUrl';

type Query<T> = T & Partial<SortParams> & PaginationParams;

interface Params<T> {
  notUseUrl?: (keyof T)[];
  getSort?: (sortParams: SortParams) => SortParams;
  localStorageName?: string;
  noPagination?: boolean;
}

export const defaultPagination: PaginationParams = { page: 0, size: 10 };

interface UseQueryReturnType<T> {
  query: Query<T>;
  setQuery: (query: Partial<T>) => void;
  setPagination: (pagination: PaginationParams) => void;
  setSort: (sort: SortParams) => void;
  update: () => void;
}

export const useQuery = <T>(
  defaultValues?: Omit<T, keyof PaginationParams>,
  queryParams?: Params<T>
): UseQueryReturnType<T> => {
  const { replace } = useHistory();

  const {
    testMode, _updater, ...filters
  } = useUrl();

  const initialValues = {
    ...(!queryParams?.noPagination && defaultPagination),
    ...defaultValues,
    ...JSON.parse(localStorage.getItem(queryParams?.localStorageName ?? '') || '{}'),
    ...(!queryParams?.localStorageName && filters),
    ...queryParams?.getSort?.(filters),
  } as Query<T>;

  const [query, setQuery] = useState<Query<T> & { _updater?: number }>(initialValues);

  const handleQueryChange = useCallback(newQuery => {
    setQuery(prev => ({
      ...newQuery,
      ...(!queryParams?.noPagination && {
        ...defaultPagination,
        size: prev.size,
      }),
      field: prev.field,
      direction: prev.direction,
    }));
  }, [queryParams?.noPagination]);

  const update = useCallback(() => {
    setQuery(prev => ({
      ...prev,
      _updater: (prev._updater ?? 0) + 1,
    }));
  }, []);

  const handleSortChange = useCallback(({ field, direction }: SortParams) => {
    setQuery(prev => ({
      ...prev,
      field: direction ? field : undefined,
      direction,
    }));
  }, []);

  const handlePaginationChange = useCallback(pagination => {
    setQuery(prevQuery => ({
      ...prevQuery,
      ...pagination,
    }));
  }, []);

  useEffect(() => {
    const params = new URLSearchParams();

    for (const filter in query) {
      if (!queryParams?.notUseUrl?.includes(filter as keyof T) && filter !== '_updater') {
        setIntoUrl(params, query, filter);
      }
    }

    replace({
      search: params.toString(),
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, replace, JSON.stringify(queryParams?.notUseUrl)]);

  useEffect(() => {
    if (filters.page === undefined && !queryParams?.noPagination) {
      setQuery(initialValues);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters, queryParams?.noPagination]);

  useEffect(() => {
    if (queryParams?.localStorageName) {
      localStorage.setItem(queryParams.localStorageName, JSON.stringify(query));
    }
  }, [query, queryParams?.localStorageName]);

  return {
    query,
    setQuery: handleQueryChange,
    setPagination: handlePaginationChange,
    setSort: handleSortChange,
    update,
  };
};
