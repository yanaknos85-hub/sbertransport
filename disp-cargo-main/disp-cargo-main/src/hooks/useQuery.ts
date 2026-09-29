// import { PaginationParams, SortParams } from 'stores/Pagination/Pagination.interface';
import { useCallback, useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';

import useUrl from './useUrl';
import { PaginationParams, SortParams } from 'utils/io-ts/pagination';
import { setIntoUrl } from 'utils/setIntoUrl';

type Query<T> = T & Partial<SortParams> & PaginationParams;

interface Params<T> {
  notUseUrl: (keyof T)[];
}

export const defaultPagination: PaginationParams = { page: 0, size: 10 };

export const useQuery = <T>(
  defaultValues?: T,
  queryParams?: Params<T>
): {
    query: Query<T>;
    setQuery: (query: Partial<T>) => void;
    setPagination: (pagination: PaginationParams) => void;
    setSort: (sort: SortParams) => void;
    update: () => void;
  } => {
  const { replace } = useHistory();

  const {
    testMode, _updater, ...filters
  } = useUrl();
  const [query, setQuery] = useState<Query<T> & { _updater?: number }>({
    ...defaultPagination,
    ...defaultValues,
    ...filters,
  } as Query<T>);

  const handleQueryChange = useCallback(newQuery => {
    setQuery(prev => ({
      ...newQuery,
      ...defaultPagination,
      size: prev.size,
      field: prev.field,
      direction: prev.direction,
    }));
  }, []);

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
    if (
      filters.page === undefined
      && JSON.stringify(query)
      !== JSON.stringify({
        ...defaultPagination,
        ...defaultValues,
        ...filters,
      })
    ) {
      setQuery({
        ...defaultPagination,
        ...defaultValues,
        ...filters,
      } as Query<T>);
    }
  }, [filters, query, defaultValues]);

  return {
    query,
    setQuery: handleQueryChange,
    setPagination: handlePaginationChange,
    setSort: handleSortChange,
    update,
  };
};
